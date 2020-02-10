import {Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges} from '@angular/core';
import {SyncService} from '../../presenze/sync.service';
import {ApiService} from '../../api.service';
import {AuthService} from '../../../auth/auth.service';
import {MatDialog, MatSnackBar} from '@angular/material';
import {RxStompService} from '@stomp/ng2-stompjs';
import {DatePipe} from '@angular/common';
import {Alunno, Fermata, PrenotazioneRequest, StopsByLine} from '../../line-details';
import {concat, defer, EMPTY, forkJoin, Observable, Subject, Subscription, timer} from 'rxjs';
import {
  catchError,
  first,
  mergeMap,
  switchMap,
  tap
} from 'rxjs/operators';
import {ApiDispService, DispAllResource, DispTurnoResource} from '../api-disp.service';
import {ApiTurniService, TurnoDispResource, TurnoResource} from '../../turni/api-turni.service';
import {Point} from 'geojson';
import {FormBuilder, Validators} from '@angular/forms';

@Component({
  selector: 'app-aggiunta-disp',
  templateUrl: './aggiunta-disp.component.html',
  styleUrls: ['./aggiunta-disp.component.scss'],
})

export class AggiuntaDispComponent implements OnInit, OnDestroy {

  @Input() linee: string[];
  private loading;
  private p: PrenotazioneRequest;
  private all$: Observable<any[]>;
  private all: any;
  private linea: StopsByLine;
  disp: DispAllResource;
  private turno: TurnoResource;
  private changeDisp = new Subject<DispAllResource>();
  private changeTurno = new Subject<TurnoResource>();
  private changeLinea = new Subject<string>();

  emptyDisp: DispAllResource = {
    id: null,
    guideUsername: null,
    orario: null,
    idLinea: null,
    nomeLinea: null,
    idFermata: null,
    isAck: null,
    isConfirmed: null,
    nomeFermata: null,
    add: false,
    delete: false,
    ack: false
  };
  private dispCreateDelSub: Subscription;
  private dispStatusSub: Subscription;
  private turnoStatusSub: Subscription;
  private stops: Point[] = [];
  private stopsDesc: string[] = [];
  selectedFermata: Fermata;
  selectedFermataCheck: any;
  dispForm = this.fb.group({
    stopSelect: ['', [Validators.required]]
  });
  private fermataDisp: Observable<Fermata>;
  private mainSub: Subscription;
  selectedLine: string;

  constructor(private syncService: SyncService, private apiService: ApiService, private apiDispService: ApiDispService,
              private apiTurniService: ApiTurniService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe, private fb: FormBuilder
  ) {
    // Main Observable
    this.mainSub = this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(pren => {
        this.p = pren;
        this.selectedLine = pren.linea;
        this.p.linea = pren.linea;
        return this.apiDispService.getDisp(pren.verso, pren.data).pipe(
          catchError((err, caught) => {
            this.changeDisp.next(null);
            this.changeTurno.next(null);
            this.loading = false;
            return null;
          })
        );
      }),
      tap(() => this.loading = false),
    ).subscribe(
      res => {
        if (!res) {
          this.changeDisp.next(this.emptyDisp);
          this.changeTurno.next(null);
          this.changeLinea.next(this.selectedLine);
        } else {
          if ('turno' in res) {
            this.changeDisp.next(res.disp);
            this.changeTurno.next(res.turno);
            this.p.linea = res.disp.idLinea;
            this.changeLinea.next(res.disp.idLinea);
          }
        }
      });

    // change Disp
    this.changeDisp.asObservable().subscribe(
      res => {
        this.disp = res;
        if (this.disp) {
          this.disp.add = false;
          this.disp.ack = false;
          this.disp.delete = false;
          this.fermataDisp = this.apiService.getFermata(this.disp.idFermata);
        }
      }
    );

    // change Turno
    this.changeTurno.asObservable().subscribe(
      res => {
        this.turno = res;
        this.dispForm.markAsUntouched();
        if (!this.turno || !this.turno.isOpen || this.turno.isExpired) {
          this.dispForm.patchValue({
            stopSelect: {value: '', disabled: true}
          });
        }
        this.dispForm.patchValue({
          stopSelect: ''
        });
      }
    );
    // WebSocket Disponibilità creazione/eliminazione
    this.dispCreateDelSub = this.changeLinea.asObservable().pipe(
      switchMap(linea => {
        return this.rxStompService.watch('/user/dispws' + this.pathSub(this.p));
      }),
    ).subscribe(message => {
        const res = JSON.parse(message.body);
        if (res.disp) {
          this.changeDisp.next(res.disp);
          this.changeTurno.next(res.turno);
        } else {
          this.changeDisp.next(this.emptyDisp);
          this.changeTurno.next(null);
        }
      }
    );

    // WebSocket Disponibilità status
    this.dispStatusSub = this.changeLinea.asObservable().pipe(
      switchMap(linea => {
        return this.rxStompService.watch('/user/dispws/status' + this.pathSub(this.p));
      }),
    ).subscribe(message => {
        const res = JSON.parse(message.body);
        this.disp.isConfirmed = res.isConfirmed;
        this.disp.isAck = res.isAck;
        this.changeDisp.next(this.disp);
      }
    );

    // WebSocket Turno cambio stato
    this.turnoStatusSub = this.changeLinea.asObservable().pipe(
      switchMap(
        linea => {
          return this.rxStompService.watch('/turnows' + this.pathSub(this.p));
        }
      ),
    ).subscribe(message => {
      const res = JSON.parse(message.body);
      this.changeTurno.next(res);
    });

    this.changeLinea.asObservable().pipe(
      mergeMap(linea => {
          return this.apiService.getStopsByLine(linea).pipe(first());
        }
      )
    ).subscribe(
      res => {
        this.linea = res;
        this.stops = [];
        this.stopsDesc = [];
        if (this.p.verso === 'Andata') {
          this.linea.andata.forEach((value, index) => {
            this.stops.push(value.location);
            this.stopsDesc.push(value.nome);
          });
        } else {
          this.linea.ritorno.forEach((value, index) => {
            this.stops.push(value.location);
            this.stopsDesc.push(value.nome);
          });
        }
      }
    );

    this.changeLinea.asObservable().pipe(
      mergeMap(linea => {
          return this.apiTurniService.getTurnoState(linea, this.p.verso, this.p.data).pipe(first());
        }
      )
    ).subscribe(
      res => {
        this.changeTurno.next(res);

      }
    );
  }

  ngOnInit() {
  }

  ngOnDestroy() {
    this.dispCreateDelSub.unsubscribe();
    this.dispStatusSub.unsubscribe();
    this.turnoStatusSub.unsubscribe();
    this.mainSub.unsubscribe();
  }

  showLoading() {
    return this.loading;
  }

  addDisp(idFermata: string) {
    this.disp.add = true;
    this.apiDispService.postDisp(this.p.linea, idFermata, this.p.verso, this.p.data).subscribe(response => {
      this.changeDisp.next(response);
    }, (error) => {
      this.disp.add = true;
      this.snackBar.open(
        'Errore Aggiunta Disponibilità, riprova più tardi o contatta l\'amministratore di sistema', '', {
          duration: 5000,
        });
    });
  }

  delDisp() {
    this.disp.delete = true;
    this.apiDispService.delDisp(this.p.linea, this.disp.idFermata.toString(), this.p.verso, this.p.data).subscribe(response => {
      this.changeDisp.next(this.emptyDisp);
      this.changeLinea.next(this.selectedLine);
      this.changeTurno.next(null);
    }, (error) => {
      // TODO: errore aggiunta disponibilità
    });
  }

  ackDisp() {
    this.disp.ack = true;
    this.apiDispService.ackDisp(this.p.linea, this.p.verso, this.p.data).subscribe(response => {
      this.disp.isAck = true;
      this.changeDisp.next(this.disp);
    }, (error) => {
      // TODO: errore aggiunta disponibilità
    });
  }

  getTurno(value: any) {

    this.p.linea = value;
    this.changeLinea.next(value);
  }

  private pathSub(prenotazione: PrenotazioneRequest) {
    return '/' + this.datePipe.transform(
      prenotazione.data, 'yyyy-MM-dd') + '/' + prenotazione.linea + '/' + this.apiService.versoToInt(prenotazione.verso);
  }

  selectFermata($event: number) {
    if (!this.turno.isOpen || this.turno.isExpired) {
      return;
    }
    this.selectedFermata = this.p.verso === 'Andata' ?
      this.linea.andata.find((el) => el.id === $event) :
      this.linea.ritorno.find((el) => el.id === $event);
    this.selectedFermataCheck = this.selectedFermata.id;
    this.dispForm.patchValue({
        stopSelect: this.selectedFermataCheck
      }
    );
  }

  selectFermataIndex(index: number) {
    if (!this.turno.isOpen || this.turno.isExpired) {
      return;
    }
    this.selectedFermata = this.p.verso === 'Andata' ?
      this.linea.andata[index] :
      this.linea.ritorno[index];
    this.selectedFermataCheck = this.selectedFermata.id;
    this.dispForm.patchValue({
        stopSelect: this.selectedFermataCheck
      }
    );
  }
}

