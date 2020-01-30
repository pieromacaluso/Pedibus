import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ChildrenDTO, ReservationDTO} from '../dtos';
import {Fermata, PrenotazioneRequest} from '../../line-details';
import {SyncService} from '../../presenze/sync.service';
import {Observable, Subscription} from 'rxjs';
import {BambinoService} from './bambino.service';
import {MatDialog, MatDialogRef} from '@angular/material';
import {DialogAnagraficaComponent} from '../dialog-anagrafica/dialog-anagrafica.component';
import {environment} from '../../../../environments/environment';
import * as moment from 'moment';

@Component({
  selector: 'app-scheda-bambino',
  templateUrl: './scheda-bambino.component.html',
  styleUrls: ['./scheda-bambino.component.scss']
})
export class SchedaBambinoComponent implements OnInit, OnDestroy {

  @Input() bambino: ChildrenDTO;
  @Input() linee: string[];
  request: PrenotazioneRequest;
  defaultAndata: Observable<Fermata>;
  defaultRitorno: Observable<Fermata>;
  andata: ReservationDTO;
  ritorno: ReservationDTO;
  status = 'Non prenotato';
  anagraficaDialog: MatDialogRef<DialogAnagraficaComponent>;
  schoolClosed: boolean;
  bambinoSub: Subscription;
  bambinoRes: Subscription;
  andataStop: Observable<Fermata>;
  ritornoStop: Observable<Fermata>;
  subRequest: Subscription;
  limitAndata = environment.limitAndata;
  limitRitorno = environment.limitRitorno;
  date: Date;

  constructor(private syncService: SyncService, private bambinoService: BambinoService, private dialog: MatDialog) {
  }

  ngOnInit() {
    this.changeDate(new Date());

    if (this.bambinoSub) {
      this.bambinoSub.unsubscribe();
    }
    this.bambinoSub = this.bambinoService.watchChild(this.bambino.codiceFiscale).subscribe(
      (message) => {
        this.bambino = JSON.parse(message.body);
        this.defaultAndata = this.bambinoService.getFermata(this.bambino.idFermataAndata);
        this.defaultRitorno = this.bambinoService.getFermata(this.bambino.idFermataRitorno);
      }
    );
    this.defaultAndata = this.bambinoService.getFermata(this.bambino.idFermataAndata);
    this.defaultRitorno = this.bambinoService.getFermata(this.bambino.idFermataRitorno);
  }

  showAnagraficaDialog() {
    this.bambinoService.openDialog(this.bambino, this.linee, this.defaultAndata, this.defaultRitorno).subscribe((data) => {
      if (data) {
        const tomorrow = moment().add(1, 'days');
        // this.bambino = data.data.child;
        // this.defaultAndata = this.bambinoService.getFermata(this.bambino.idFermataAndata);
        // this.defaultRitorno = this.bambinoService.getFermata(this.bambino.idFermataRitorno);
        this.bambinoService.updateFermate(data.data.child, tomorrow.toDate()).subscribe((d) => {
        }, (error) => {
        });
      }
    });
  }

  showPrenotazioneDialog() {

  }

  ngOnDestroy(): void {
    this.bambinoSub.unsubscribe();
  }

  addAndata() {
    this.bambinoService.openDialogReservation(this.bambino, true, this.date, this.linee, true, this.andata)
      .subscribe((data) => {
        if (data) {
          this.bambinoService.createReservation(data.line, data.data, data.childId, data.stopId, data.verso)
            .subscribe((d) => {
            }, (error) => {
            });
        }
      });
  }

  addRitorno() {
    this.bambinoService.openDialogReservation(this.bambino, false, this.date, this.linee, true, this.ritorno)
      .subscribe((data) => {
        if (data) {
          this.bambinoService.createReservation(data.line, data.data, data.childId, data.stopId, data.verso)
            .subscribe((d) => {
            }, (error) => {
            });
        }
      });
  }

  editAndata() {
    this.bambinoService.openDialogReservation(this.bambino, true, this.date, this.linee, false, this.andata)
      .subscribe((data) => {
        if (data) {
          this.bambinoService.updateReservation(data.line, data.data, data.childId, data.stopId, data.verso, this.andata.id)
            .subscribe((d) => {
            }, (error) => {
            });
        }
      });
  }

  editRitorno() {
    this.bambinoService.openDialogReservation(this.bambino, false, this.date, this.linee, false, this.ritorno)
      .subscribe((data) => {
        if (data) {
          this.bambinoService.updateReservation(data.line, data.data, data.childId, data.stopId, data.verso, this.ritorno.id)
            .subscribe((d) => {
            }, (error) => {
            });
        }
      });
  }

  deleteAndata() {
    this.bambinoService.deleteReservation(this.andata.idLinea, this.date, this.andata.id)
      .subscribe((d) => {
      }, (error) => {
      });
  }

  deleteRitorno() {
    this.bambinoService.deleteReservation(this.ritorno.idLinea, this.date, this.ritorno.id)
      .subscribe((d) => {
      }, (error) => {
      });
  }

  isPresentOrFuture() {
    const data = new Date();
    const reqData = this.date;
    data.setHours(0, 0, 0, 0);
    reqData.setHours(0, 0, 0, 0);
    return reqData >= data;
  }

  isOnTime(orario: string) {
    const date = new Date();
    const reqData = this.date;
    const ora = +orario.split(':')[0];
    const min = +orario.split(':')[1];
    reqData.setHours(ora, min, 0, 0);
    return date < reqData;
  }

  changeDate(date: any) {
    this.date = date;
    if (this.bambino) {
      this.bambinoService.getStatus(this.bambino, date).subscribe((reservation) => {
        this.andata = undefined;
        this.ritorno = undefined;
        this.andataStop = undefined;
        this.ritornoStop = undefined;
        this.schoolClosed = false;
        for (const res of reservation) {
          if (res.verso) {
            this.andata = res;
            this.andataStop = this.bambinoService.getFermata(this.andata.idFermata);
          } else {
            this.ritorno = res;
            this.ritornoStop = this.bambinoService.getFermata(this.ritorno.idFermata);
          }
        }
      }, (error) => {
        this.schoolClosed = true;
      });

      if (this.bambinoRes) {
        this.bambinoRes.unsubscribe();
      }
      this.bambinoRes = this.bambinoService.watchChildReservation(this.bambino.codiceFiscale, date).subscribe(
        (message) => {
          const reservation: ReservationDTO = JSON.parse(message.body);
          if (reservation.idFermata === null) {
            if (reservation.verso) {
              this.andata = null;
            } else {
              this.ritorno = null;

            }
            return;
          }
          if (reservation.verso) {
            this.andata = reservation;
            this.andataStop = this.bambinoService.getFermata(this.andata.idFermata);
          } else {
            this.ritorno = reservation;
            this.ritornoStop = this.bambinoService.getFermata(this.ritorno.idFermata);

          }
        }
      );
    }
  }
}
