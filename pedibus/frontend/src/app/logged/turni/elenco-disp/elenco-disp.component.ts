import {Component, OnInit} from '@angular/core';
import {SyncService} from '../../presenze/sync.service';
import {ApiService} from '../../api.service';
import {ApiDispService} from '../../api-disp.service';
import {AuthService} from '../../../registration/auth.service';
import {MatDialog, MatSnackBar} from '@angular/material';
import {RxStompService} from '@stomp/ng2-stompjs';
import {DatePipe} from '@angular/common';
import {finalize, tap} from 'rxjs/operators';
import {defer, Observable} from 'rxjs';
import {PrenotazioneRequest, StopsByLine} from '../../line-details';
import {ApiTurniService, DispAllResource} from '../../api-turni.service';

@Component({
  selector: 'app-elenco-disp',
  templateUrl: './elenco-disp.component.html',
  styleUrls: ['./elenco-disp.component.scss']
})
export class ElencoDispComponent implements OnInit {

  idFermata: string;
  prenotazione$: Observable<PrenotazioneRequest>;
  disp$: Observable<DispAllResource[]>;
  private loading: boolean;
  private p: PrenotazioneRequest;

  constructor(private syncService: SyncService, private apiService: ApiService, private apiTurniService: ApiTurniService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe) {

    this.prenotazione$ = this.syncService.prenotazioneObs$.pipe(
      tap((result) => {
        this.p = result;
      }),
      tap(() => {
        this.disp$ = defer(() => {
          this.loading = true;
          return this.apiTurniService.getTurno(this.p.linea, this.p.verso, this.p.data).pipe(
            finalize(() => this.loading = false),
          );
        });
      }));
  }

  showLoading() {
    return this.loading;
  }

  ngOnInit() {
  }

}
