import {Component, OnInit} from '@angular/core';
import {SyncService} from '../../presenze/sync.service';
import {ApiService} from '../../api.service';
import {AuthService} from '../../../registration/auth.service';
import {MatDialog, MatSnackBar} from '@angular/material';
import {RxStompService} from '@stomp/ng2-stompjs';
import {DatePipe} from '@angular/common';
import {PrenotazioneRequest, StopsByLine} from '../../line-details';
import {concat, defer, EMPTY, Observable, timer} from 'rxjs';
import {defaultIfEmpty, delay, distinctUntilChanged, finalize, flatMap, map, retry, tap} from 'rxjs/operators';
import {fadeAnimation} from '../../../route-animations';
import {ApiDispService} from '../../api-disp.service';

@Component({
  selector: 'app-aggiunta-disp',
  templateUrl: './aggiunta-disp.component.html',
  styleUrls: ['./aggiunta-disp.component.scss'],
})
export class AggiuntaDispComponent implements OnInit {

  idFermata: string;
  prenotazione$: Observable<PrenotazioneRequest>;
  countLoading = 0;
  stops$: Observable<StopsByLine>;
  private loading: boolean;
  selectedStop: any;
  private p: PrenotazioneRequest;


  constructor(private syncService: SyncService, private apiService: ApiService, private apiDispService: ApiDispService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe) {

    this.prenotazione$ = this.syncService.prenotazioneObs$.pipe(
      tap((result) => {
        this.p = result;
      }),
      tap(() => {
        this.stops$ = defer(() => {
          this.loading = true;
          return this.apiService.getStopsByLine(this.p.linea).pipe(
            finalize(() => this.loading = false)
          );
        });
      }));
  }

  ngOnInit() {
  }

  showLoading() {
    return this.loading;
  }

  addDisp(idFermata: string) {
    console.log(idFermata);
    this.apiDispService.postDisp(this.p.linea, idFermata, this.p.verso, this.p.data).subscribe(response => {
      // TODO: disponibilità aggiunta
    }, (error) => {
      // TODO: errore aggiunta disponibilità
    });
  }
}

