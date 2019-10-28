import {Component, Input, OnInit} from '@angular/core';
import {SyncService} from '../../presenze/sync.service';
import {ApiService} from '../../api.service';
import {AuthService} from '../../../registration/auth.service';
import {MatDialog, MatSnackBar} from '@angular/material';
import {RxStompService} from '@stomp/ng2-stompjs';
import {DatePipe} from '@angular/common';
import {Alunno, PrenotazioneRequest, StopsByLine} from '../../line-details';
import {concat, defer, EMPTY, forkJoin, Observable, Subject, timer} from 'rxjs';
import {defaultIfEmpty, delay, distinctUntilChanged, finalize, flatMap, map, mergeMap, retry, switchMap, tap} from 'rxjs/operators';
import {fadeAnimation} from '../../../route-animations';
import {ApiDispService, DispAllResource, DispTurnoResource} from '../../api-disp.service';
import {ApiTurniService, TurnoDispResource, TurnoResource} from '../../api-turni.service';

@Component({
  selector: 'app-aggiunta-disp',
  templateUrl: './aggiunta-disp.component.html',
  styleUrls: ['./aggiunta-disp.component.scss'],
})
export class AggiuntaDispComponent implements OnInit {

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
  emptyDisp: DispAllResource = {
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

  constructor(private syncService: SyncService, private apiService: ApiService, private apiDispService: ApiDispService,
              private apiTurniService: ApiTurniService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe) {

    // Main Observable
    this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      mergeMap(pren => {
        this.p = pren;
        return this.apiDispService.getDisp(pren.verso, pren.data);
      }),
      tap(() => this.loading = false),
    ).subscribe(
      res => {
        console.log(res);
        if (!res) {
          this.changeDisp.next(this.emptyDisp);
          this.changeTurno.next(null);
        } else {
          this.changeDisp.next(res.disp);
          this.changeTurno.next(res.turno);
        }
      },
      err => {
        // TODO: errore
      }
    );

    // change Disp
    this.changeDisp.asObservable().subscribe(
      res => {
        if (res) {
          console.log('disp', res);
          this.disp = res;
          this.disp.add = false;
          this.disp.ack = false;
          this.disp.delete = false;
        }
      }
    );

    // change Turno
    this.changeTurno.asObservable().subscribe(
      res => {
        this.turno = res;
      }
    );
    // WebSocket Disponibilità creazione/eliminazione
    this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      mergeMap(pren => {
        this.p = pren;
        return this.rxStompService.watch('/dispws' + '/' + this.authService.getUsername() + '/' + this.pathSub(pren));
      }),
      tap(() => this.loading = false),
    ).subscribe(message => {
        const res = JSON.parse(message.body);
        console.log('DISP', res);
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
    this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      mergeMap(pren => {
        this.p = pren;
        return this.rxStompService.watch('/dispws-status' + '/' + this.authService.getUsername() + '/' + this.pathSub(pren));
      }),
      tap(() => this.loading = false),
    ).subscribe(message => {
        const res = JSON.parse(message.body);
        console.log('DISP', res);
        this.disp.isConfirmed = res.isConfirmed;
        this.disp.isAck = res.isAck;
        this.changeDisp.next(this.disp);
      }
    );

    // WebSocket Turno cambio stato
    this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(
        pren => {
          return this.rxStompService.watch('/turnows' + this.pathSub(pren));
        }
      ),
      tap(() => this.loading = false)
    ).subscribe(message => {
      const res = JSON.parse(message.body);
      this.changeTurno.next(res);
    });
  }

  ngOnInit() {
  }

  showLoading() {
    return this.loading;
  }

  addDisp(idFermata: string) {
    this.disp.add = true;
    this.apiDispService.postDisp(this.p.linea, idFermata, this.p.verso, this.p.data).subscribe(response => {
      this.changeDisp.next(response);
    }, (error) => {
      // TODO: errore aggiunta disponibilità
    });
  }

  delDisp() {
    this.disp.delete = true;
    this.apiDispService.delDisp(this.p.linea, this.disp.idFermata.toString(), this.p.verso, this.p.data).subscribe(response => {
      this.changeDisp.next(this.emptyDisp);
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
    this.apiService.getStopsByLine(value).subscribe(
      res => {
        this.linea = res;
      }
    );
    this.apiTurniService.getTurnoState(value, this.p.verso, this.p.data).subscribe(
      res => {
        this.changeTurno.next(res);
      }
    );
  }

  private pathSub(prenotazione: PrenotazioneRequest) {
    return '/' + this.datePipe.transform(
      prenotazione.data, 'yyyy-MM-dd') + '/' + prenotazione.linea + '/' + this.apiService.versoToInt(prenotazione.verso);
  }
}

