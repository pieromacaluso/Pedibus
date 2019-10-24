import {Component, OnInit} from '@angular/core';
import {SyncService} from '../../presenze/sync.service';
import {ApiService} from '../../api.service';
import {AuthService} from '../../../registration/auth.service';
import {MatDialog, MatSnackBar} from '@angular/material';
import {RxStompService} from '@stomp/ng2-stompjs';
import {DatePipe} from '@angular/common';
import {PrenotazioneRequest, StopsByLine} from '../../line-details';
import {concat, defer, EMPTY, forkJoin, Observable, Subject, timer} from 'rxjs';
import {defaultIfEmpty, delay, distinctUntilChanged, finalize, flatMap, map, mergeMap, retry, tap} from 'rxjs/operators';
import {fadeAnimation} from '../../../route-animations';
import {ApiDispService, DispAllResource, DispTurnoResource} from '../../api-disp.service';
import {TurnoDispResource, TurnoResource} from '../../api-turni.service';

@Component({
  selector: 'app-aggiunta-disp',
  templateUrl: './aggiunta-disp.component.html',
  styleUrls: ['./aggiunta-disp.component.scss'],
})
export class AggiuntaDispComponent implements OnInit {

  idFermata: string;
  private loading;
  private p: PrenotazioneRequest;
  private all$: Observable<any[]>;
  private all: any;
  private linea: StopsByLine;
  private disp: DispAllResource;
  private turno: TurnoResource;
  private changeDisp = new Subject<DispAllResource>();
  private changeTurno = new Subject<TurnoResource>();

  constructor(private syncService: SyncService, private apiService: ApiService, private apiDispService: ApiDispService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe) {

    // Main Observable
    this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      mergeMap(pren => {
        this.p = pren;
        const stops = this.apiService.getStopsByLine(pren.linea);
        const disp = this.apiDispService.getDisp(pren.linea, pren.verso, pren.data);
        return forkJoin([stops, disp]);
      }),
      tap(() => this.loading = false),
    ).subscribe(
      res => {
        this.linea = res[0];
        if (!res[1].disp) {
          const variable: DispAllResource = {
            guideUsername: null,
            idFermata: null,
            isAck: null,
            isConfirmed: null,
            nomeFermata: null,
            add: false,
            delete: false,
            ack: false
          };
          this.changeDisp.next(variable);
        } else {
          this.changeDisp.next(res[1].disp);
        }
        this.changeTurno.next(res[1].turno);

      },
      err => {
        // TODO: errore
      }
    );

    // change Disp
    this.changeDisp.asObservable().subscribe(
      res => {
        console.log('disp', res);
        this.disp = res;
        this.disp.add = false;
        this.disp.ack = false;
        this.disp.delete = false;
      }
    );

    // change Turno
    this.changeTurno.asObservable().subscribe(
      res => {
        this.turno = res;
      }
    );
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

  delDisp(idFermata: any) {
    this.disp.delete = true;
    this.apiDispService.delDisp(this.p.linea, idFermata, this.p.verso, this.p.data).subscribe(response => {
      const variable: DispAllResource = {
        guideUsername: null,
        idFermata: null,
        isAck: null,
        isConfirmed: null,
        nomeFermata: null,
        add: true,
        delete: true,
        ack: true
      };
      this.changeDisp.next(variable);
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
}

