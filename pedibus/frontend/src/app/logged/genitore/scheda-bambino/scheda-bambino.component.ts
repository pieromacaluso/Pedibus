import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ChildrenDTO, ReservationDTO} from '../dtos';
import {Fermata, PrenotazioneRequest} from '../../line-details';
import {SyncService} from '../../presenze/sync.service';
import {Observable, Subscription} from 'rxjs';
import {BambinoService} from './bambino.service';
import {MatDialog, MatDialogRef} from '@angular/material';
import {DialogAnagraficaComponent} from '../dialog-anagrafica/dialog-anagrafica.component';

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
  private schoolClosed: boolean;
  private bambinoSub: Subscription;
  private bambinoRes: Subscription;
  private andataStop: Observable<Fermata>;
  private ritornoStop: Observable<Fermata>;

  constructor(private syncService: SyncService, private bambinoService: BambinoService, private dialog: MatDialog) {
  }

  ngOnInit() {
    this.syncService.prenotazioneObs$.subscribe((data) => {
      this.request = data;
      this.andata = null;
      this.ritorno = null;
      // todo: inserire logica di reazione a richiesta qui
      if (this.bambino && this.request) {
        this.bambinoService.getStatus(this.bambino, this.request.data).subscribe((reservation) => {
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
          console.log(reservation);
        }, (error) => {
          this.schoolClosed = true;
          console.log(error);
        });

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
        this.bambinoRes = this.bambinoService.watchChildReservation(this.bambino.codiceFiscale, this.request.data).subscribe(
          (message) => {
            const reservation: ReservationDTO = JSON.parse(message.body);
            if (reservation.verso) {
              this.andata = reservation;
              this.andataStop = this.bambinoService.getFermata(this.andata.idFermata);
            } else {
              this.ritorno = reservation;
              this.ritornoStop = this.bambinoService.getFermata(this.ritorno.idFermata);

            }
          }
        );
        this.defaultAndata = this.bambinoService.getFermata(this.bambino.idFermataAndata);
        this.defaultRitorno = this.bambinoService.getFermata(this.bambino.idFermataRitorno);
      }

    });

  }

  showAnagraficaDialog() {
    this.bambinoService.openDialog(this.bambino, this.linee, this.defaultAndata, this.defaultRitorno).subscribe((data) => {
      if (data) {
        // this.bambino = data.data.child;
        // this.defaultAndata = this.bambinoService.getFermata(this.bambino.idFermataAndata);
        // this.defaultRitorno = this.bambinoService.getFermata(this.bambino.idFermataRitorno);
        this.bambinoService.updateFermate(data.data.child).subscribe((d) => console.log(d), (error) => console.log(error));
      }
    });
  }

  showPrenotazioneDialog() {

  }

  ngOnDestroy(): void {
    this.bambinoSub.unsubscribe();
  }

  addAndata() {
    this.bambinoService.openDialogReservation(this.bambino, true, this.request.data, this.linee, true, this.andata)
      .subscribe((data) => {
        if (data) {
          this.bambinoService.createReservation(data.line, data.data, data.childId, data.stopId, data.verso)
            .subscribe((d) => console.log(d), (error) => console.log(error));
        }
      });
  }

  addRitorno() {
    this.bambinoService.openDialogReservation(this.bambino, false, this.request.data, this.linee, true, this.ritorno)
      .subscribe((data) => {
        if (data) {
          this.bambinoService.createReservation(data.line, data.data, data.childId, data.stopId, data.verso)
            .subscribe((d) => console.log(d), (error) => console.log(error));
        }
      });
  }

  editAndata() {
    this.bambinoService.openDialogReservation(this.bambino, true, this.request.data, this.linee, false, this.andata)
      .subscribe((data) => {
        if (data) {
          this.bambinoService.updateReservation(data.line, data.data, data.childId, data.stopId, data.verso, this.andata.id)
            .subscribe((d) => console.log(d), (error) => console.log(error));
        }
      });
  }

  editRitorno() {
    this.bambinoService.openDialogReservation(this.bambino, false, this.request.data, this.linee, false, this.ritorno)
      .subscribe((data) => {
        if (data) {
          this.bambinoService.updateReservation(data.line, data.data, data.childId, data.stopId, data.verso, this.ritorno.id)
            .subscribe((d) => console.log(d), (error) => console.log(error));
        }
      });
  }
}
