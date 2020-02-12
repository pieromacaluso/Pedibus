import {Component, OnDestroy, OnInit} from '@angular/core';
import {SyncService} from '../../presenze/sync.service';
import {ApiService} from '../../api.service';
import {ApiDispService, DispAllResource} from '../../disponibilita/api-disp.service';
import {AuthService} from '../../../auth/auth.service';
import {MatDialog, MatDialogRef, MatListOption, MatSelectionList, MatSnackBar} from '@angular/material';
import {RxStompService} from '@stomp/ng2-stompjs';
import {DatePipe} from '@angular/common';
import {catchError, debounceTime, finalize, first, mergeMap, switchMap, tap} from 'rxjs/operators';
import {defer, forkJoin, Observable, Subject, Subscription} from 'rxjs';
import {PrenotazioneRequest, StopsByLine} from '../../line-details';
import {ApiTurniService, MapDisp, TurnoDispResource, TurnoResource} from '../api-turni.service';
import {variable} from '@angular/compiler/src/output/output_ast';
import {DeleteDialogComponent} from '../../presenze/lista-prenotazioni/delete-dialog/delete-dialog.component';
import {UpdateDispDialogComponent} from './update-disp-dialog/update-disp-dialog.component';
import {MapService} from '../../../utilities/map.service';

@Component({
  selector: 'app-elenco-disp',
  templateUrl: './elenco-disp.component.html',
  styleUrls: ['./elenco-disp.component.scss']
})
export class ElencoDispComponent implements OnInit, OnDestroy {

  idFermata: string;
  prenotazione$: Observable<PrenotazioneRequest>;
  turno$: Observable<TurnoDispResource>;
  private loading: boolean;
  private p: PrenotazioneRequest;
  private stops$: Observable<StopsByLine>;
  private changeDisp = new Subject<MapDisp>();
  private changeTurno = new Subject<TurnoResource>();

  linea: StopsByLine;
  turno: TurnoResource;
  listDisp: MapDisp;
  selectedDisp: any;
  cross: any = '../assets/svg/cross.svg';
  tick: any = '../assets/svg/tick.svg';
  doubletick: any = '../assets/svg/double_tick.svg';
  private dispStatusSub: Subscription;
  private dispDelSub: Subscription;
  private dispAddSub: Subscription;
  private turnoStatusSub: Subscription;
  private dispUpSub: Subscription;
  private elencoDispSub: Subscription;
  private updateDispDialog: MatDialogRef<UpdateDispDialogComponent, any>;


  constructor(private syncService: SyncService, private apiService: ApiService, private apiTurniService: ApiTurniService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe, private mapService: MapService) {
    /**
     * Richiesta per ottenere le disponibilità di quel turno
     */
    this.elencoDispSub = this.syncService.prenotazioneObs$.pipe(
      debounceTime(1000),
      tap(() => this.loading = true),
      switchMap(
        pren => {
          this.p = pren;
          this.stops$ = this.apiService.getStopsByLine(pren.linea);
          const turno = this.apiTurniService.getTurno(pren.linea, pren.verso, pren.data).pipe(
            catchError((err, caught) => {
              this.changeTurno.next(null);
              this.changeDisp.next(null);
              return null;
            })
          );
          return forkJoin([this.stops$, turno]);
        }
      ),
      tap(() => this.loading = false)
    ).subscribe(
      res => {
        this.linea = res[0];
        if (res[1] != null && 'turno' in res[1] && 'listDisp' in res[1]) {
          res[1].turno.opening = false;
          res[1].turno.closing = false;
          this.changeTurno.next(res[1].turno);
          this.changeDisp.next(res[1].listDisp);
        }
      }
    );

    /**
     * Observable che si occupa di cambiare lato frontend l'elenco delle disponibilità
     */
    this.changeDisp.asObservable().subscribe(
      res => {
        this.listDisp = res;
        this.loading = false;
      }
    );

    /**
     * Observable che si occupa di cambiare lato frontend il turno
     */
    this.changeTurno.asObservable().subscribe(
      res => {
        if (!res) {
          this.turno = res;
        } else {
          res.opening = false;
          res.closing = false;
          this.turno = res;
        }
      }
    );

    /**
     * Websocket che si occupa di cambiare lo stato del turno
     */
    this.turnoStatusSub = this.syncService.prenotazioneObs$.pipe(
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

    /**
     * Websocket che si occupa di aggiungere una nuova disponibilità
     */
    this.dispAddSub = this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(pren => {
        return this.rxStompService.watch('/dispws/add' + '/' + this.pathSub(pren));
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

    /**
     * Websocket che si occupa di eliminare una disponibilità eliminata
     */
    this.dispDelSub = this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(pren => {
        return this.rxStompService.watch('/dispws/del' + '/' + this.pathSub(pren));
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

    /**
     * Websocket che si occupa di aggiornare i dati di una disponibilità
     */
    this.dispUpSub = this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(pren => {
        return this.rxStompService.watch('/dispws/up' + '/' + this.pathSub(pren));
      }),
      tap(() => this.loading = false),
    ).subscribe(message => {
        const res = JSON.parse(message.body);
        const fermate = Object.keys(this.listDisp).map(key => this.listDisp[key]);

        fermate.forEach((disps, iFerm) => {
          if (!disps) {
            return;
          }
          disps.forEach(
            (disp, iDisp) => {
              if (disp.guideUsername === res.guideUsername && iDisp > -1) {
                this.listDisp[disp.nomeFermata].splice(iDisp, 1);
              }
            });
        });
        if (!this.listDisp[res.nomeFermata]) {
          this.listDisp[res.nomeFermata] = [];
        }
        this.listDisp[res.nomeFermata].push(res);
      }
    );

    /**
     * Websocket che si occupa di modificare lo stato di una disponibilità
     */
    this.dispStatusSub = this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(pren => {
        this.p = pren;
        return this.rxStompService.watch('/dispws/status' + '/' + this.pathSub(pren));
      }),
      tap(() => this.loading = false),
    ).subscribe(message => {
        const res = JSON.parse(message.body);
        this.listDisp[res.nomeFermata].forEach(
          (disp, iDisp, disps) => {
            if (disp.guideUsername === res.guideUsername) {
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

  ngOnDestroy() {
    this.dispAddSub.unsubscribe();
    this.dispDelSub.unsubscribe();
    this.dispUpSub.unsubscribe();
    this.dispStatusSub.unsubscribe();
    this.turnoStatusSub.unsubscribe();
    this.elencoDispSub.unsubscribe();
  }

  /**
   * Cambio stato turno
   * @param checked true se aperto, falso altrimenti
   */
  statusTurno(checked: boolean) {
    this.turno.opening = true;
    this.apiTurniService.setStateTurno(this.p.linea, this.p.verso, this.p.data, checked).pipe(first()).subscribe(response => {
      this.turno.isOpen = checked;
      this.changeTurno.next(this.turno);
    }, (error) => {
      // Gestito da Interceptor
    });
  }

  /**
   * Conferma Disponibilità
   * @param disp struttura disponibilità
   */
  confermaDisp(disp: DispAllResource) {
    disp.isConfirmed = true;
    this.apiTurniService.confirmDisp(this.p.linea, this.p.verso, this.p.data, disp).pipe(first()).subscribe(response => {
      this.changeDisp.next(this.listDisp);
    }, (error) => {
      // Gestito da Interceptor
    });

  }

  /**
   * Da prenotazione a Path per richiesta
   * @param prenotazione prenotazione
   */
  private pathSub(prenotazione: PrenotazioneRequest) {
    return '/' + this.datePipe.transform(
      prenotazione.data, 'yyyy-MM-dd') + '/' + prenotazione.linea + '/' + this.apiService.versoToInt(prenotazione.verso);
  }

  /**
   * Aggiorna disponibilità
   * @param disp struttura disponibilità
   */
  updateDisp(disp: DispAllResource) {

    this.dialog.closeAll();

    this.updateDispDialog = this.dialog.open(UpdateDispDialogComponent, {
      hasBackdrop: true,
      data: {
        disps: disp,
        turno: this.turno,
        linea: (this.turno.verso ? this.linea.andata : this.linea.ritorno)
      }
    });
  }

  /**
   * Apri dialog con mappa della fermata
   * @param idFermata id della fermata
   */
  openMapDialog(idFermata: number) {
    this.mapService.openMapDialog(idFermata).subscribe();
  }

  /**
   * Cancellazione della disponibilità
   * @param disp struttura disponibilità
   */
  deleteDisp(disp: DispAllResource) {
    this.apiService.openConfirmationDialog('Sei sicuro di voler eliminare la disponibilità di ' + disp.guideUsername)
      .subscribe((response) => {
        if (response) {
          this.apiTurniService.deleteDisp(disp.id).subscribe((res) => {
          }, error => {
            // Gestito da Interceptor
          });
        }
      });
  }
}
