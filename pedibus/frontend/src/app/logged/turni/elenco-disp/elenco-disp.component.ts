import {Component, OnInit} from '@angular/core';
import {SyncService} from '../../presenze/sync.service';
import {ApiService} from '../../api.service';
import {ApiDispService, DispAllResource} from '../../api-disp.service';
import {AuthService} from '../../../registration/auth.service';
import {MatDialog, MatSnackBar} from '@angular/material';
import {RxStompService} from '@stomp/ng2-stompjs';
import {DatePipe} from '@angular/common';
import {finalize, tap} from 'rxjs/operators';
import {defer, Observable} from 'rxjs';
import {PrenotazioneRequest, StopsByLine} from '../../line-details';
import {ApiTurniService, TurnoDispResource} from '../../api-turni.service';

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

  constructor(private syncService: SyncService, private apiService: ApiService, private apiTurniService: ApiTurniService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe) {

    this.prenotazione$ = this.syncService.prenotazioneObs$.pipe(
      tap((result) => {
        this.loading = true;
        this.p = result;
      }),
      tap(() => {
        this.stops$ = this.apiService.getStopsByLine(this.p.linea).pipe(
          tap(res => {
            console.log(res);
          }),
        );
      }),
      tap(() => {
        this.turno$ = this.apiTurniService.getTurno(this.p.linea, this.p.verso, this.p.data).pipe(
          tap(res => {
            console.log(res);
          }),
        finalize(() => this.loading = false),
      )
        ;
      }));
  }

  showLoading() {
    return this.loading;
  }

  ngOnInit() {
  }

  openTurno(turno: TurnoDispResource) {
    this.apiTurniService.setStateTurno(this.p.linea, this.p.verso, this.p.data, true).subscribe(response => {
      this.loading = true;
      this.turno$ = this.apiTurniService.getTurno(this.p.linea, this.p.verso, this.p.data).pipe(
        tap(res => {
          console.log(res);
        }),
        finalize(() => this.loading = false),
      )
      ;
    }, (error) => {
      // TODO: errore
    });
  }

  closeTurno(turno: TurnoDispResource) {
    this.apiTurniService.setStateTurno(this.p.linea, this.p.verso, this.p.data, false).subscribe(response => {
      this.loading = true;
      this.turno$ = this.apiTurniService.getTurno(this.p.linea, this.p.verso, this.p.data).pipe(
        tap(res => {
          console.log(res);
        }),
        finalize(() => this.loading = false),
      )
      ;
    }, (error) => {
      // TODO: errore
    });
  }
}
