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
@Component({
  selector: 'app-aggiunta-disp',
  templateUrl: './aggiunta-disp.component.html',
  styleUrls: ['./aggiunta-disp.component.scss']
})
export class AggiuntaDispComponent implements OnInit {

  prenotazione: PrenotazioneRequest;
  countLoading = 0;
  stops$: Observable<StopsByLine>;
  private loading: boolean;


  constructor(private syncService: SyncService, private apiService: ApiService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe) {

    this.stops$ = this.syncService.prenotazioneObs$.pipe(
      flatMap((result) => {
        return defer(() => {
          this.loading = true;
          return this.apiService.getStopsByLine(result.linea).pipe(
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
}

