import {Component, OnInit} from '@angular/core';
import {SyncService} from '../../presenze/sync.service';
import {ApiService} from '../../api.service';
import {ApiDispService, DispAllResource} from '../../api-disp.service';
import {AuthService} from '../../../registration/auth.service';
import {MatDialog, MatListOption, MatSelectionList, MatSnackBar} from '@angular/material';
import {RxStompService} from '@stomp/ng2-stompjs';
import {DatePipe} from '@angular/common';
import {debounceTime, finalize, first, mergeMap, switchMap, tap} from 'rxjs/operators';
import {defer, forkJoin, Observable, Subject} from 'rxjs';
import {PrenotazioneRequest, StopsByLine} from '../../line-details';
import {ApiTurniService, MapDisp, TurnoDispResource, TurnoResource} from '../../api-turni.service';
import {variable} from '@angular/compiler/src/output/output_ast';

@Component({
  selector: 'app-elenco-disp',
  templateUrl: './elenco-disp.component.html',
  styleUrls: ['./elenco-disp.component.scss']
})
export class ElencoDispComponent implements OnInit {

  idFermata: string;
  prenotazione$: Observable<PrenotazioneRequest>;
  turno$: Observable<TurnoDispResource>;
  private loading: boolean;
  private p: PrenotazioneRequest;
  private stops$: Observable<StopsByLine>;
  private changeDisp = new Subject<MapDisp>();
  private changeTurno = new Subject<TurnoResource>();

  linea: StopsByLine;
  private turno: TurnoResource;
  private listDisp: MapDisp;
  selectedDisp: any;
  cross: any = '../assets/svg/cross.svg';


  constructor(private syncService: SyncService, private apiService: ApiService, private apiTurniService: ApiTurniService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe) {

    this.syncService.prenotazioneObs$.pipe(
      debounceTime(1000),
      tap(() => this.loading = true),
      switchMap(
        pren => {
          this.p = pren;
          const stops = this.apiService.getStopsByLine(pren.linea);
          const turno = this.apiTurniService.getTurno(pren.linea, pren.verso, pren.data);
          return forkJoin([stops, turno]);
        }
      ),
      tap(() => this.loading = false)
    ).subscribe(
      res => {
        console.log(res);
        this.linea = res[0];
        res[1].turno.opening = false;
        res[1].turno.closing = false;
        this.changeTurno.next(res[1].turno);
        this.changeDisp.next(res[1].listDisp);
      },
      err => {
        // TODO: Errore
      }
    );

    // change Disp
    this.changeDisp.asObservable().subscribe(
      res => {
        this.listDisp = res;
        this.loading = false;
      }
    );

    // change Turno
    this.changeTurno.asObservable().subscribe(
      res => {
        console.log(res);
        res.opening = false;
        res.closing = false;
        this.turno = res;
      }
    );

    // WebSocket Turno
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

    // WebSocket Disponibilità add
    this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(pren => {
        return this.rxStompService.watch('/dispws-add' + '/' + this.pathSub(pren));
      }),
      tap(() => this.loading = false),
    ).subscribe(message => {
        const res = JSON.parse(message.body);
        if (!this.listDisp[res.nomeFermata]) {
          this.listDisp[res.nomeFermata] = [];
        }
        this.listDisp[res.nomeFermata].push(res);
      }
    );

    // WebSocket Disponibilità deleted
    this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(pren => {
        return this.rxStompService.watch('/dispws-del' + '/' + this.pathSub(pren));
      }),
      tap(() => this.loading = false),
    ).subscribe(message => {
        const res = JSON.parse(message.body);
        this.listDisp[res.nomeFermata].forEach(
          (disp, iDisp, disps) => {
            if (disp.guideUsername === res.guideUsername && iDisp > -1) {
              this.listDisp[res.nomeFermata].splice(iDisp, 1);
            }
          }
        );
      }
    );

    // WebSocket Disponibilità status
    this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(pren => {
        this.p = pren;
        return this.rxStompService.watch('/dispws-status' + '/' + this.pathSub(pren));
      }),
      tap(() => this.loading = false),
    ).subscribe(message => {
        const res = JSON.parse(message.body);
        this.listDisp[res.nomeFermata].forEach(
          (disp, iDisp, disps) => {
            if (disp.guideUsername === res.guideUsername) {
              console.log('change', this.listDisp[res.nomeFermata][iDisp], res);
              this.listDisp[res.nomeFermata][iDisp] = res;
            }
          }
        );
      }
    );
  }

  showLoading() {
    return this.loading;
  }

  ngOnInit() {
  }

  openTurno(turno: TurnoResource) {
    this.turno.opening = true;
    this.apiTurniService.setStateTurno(this.p.linea, this.p.verso, this.p.data, true).pipe(first()).subscribe(response => {
      turno.isOpen = true;
      this.changeTurno.next(turno);
    }, (error) => {
      // TODO: errore
    });
  }

  closeTurno(turno: TurnoResource) {
    this.turno.closing = true;
    this.apiTurniService.setStateTurno(this.p.linea, this.p.verso, this.p.data, false).pipe(first()).subscribe(response => {
      turno.isOpen = false;
      this.changeTurno.next(this.turno);
    }, (error) => {
      // TODO: errore
    });
  }

  statusTurno(checked: boolean) {
    console.log(this.turno);
    this.turno.opening = true;
    this.apiTurniService.setStateTurno(this.p.linea, this.p.verso, this.p.data, checked).pipe(first()).subscribe(response => {
      this.turno.isOpen = checked;
      this.changeTurno.next(this.turno);
    }, (error) => {
      // TODO: errore
    });
  }

  confermaDisp(disp: DispAllResource) {
    disp.isConfirmed = true;
    console.log(disp);
    this.apiTurniService.confirmDisp(this.p.linea, this.p.verso, this.p.data, disp).pipe(first()).subscribe(response => {
      this.changeDisp.next(this.listDisp);
    }, (error) => {
      // TODO: errore
    });

  }


  private pathSub(prenotazione: PrenotazioneRequest) {
    return '/' + this.datePipe.transform(
      prenotazione.data, 'yyyy-MM-dd') + '/' + prenotazione.linea + '/' + this.apiService.versoToInt(prenotazione.verso);
  }
}
