import {Component, OnInit, Input} from '@angular/core';
import {ChildrenDTO, FermataDTO, ReservationDTO} from '../dtos';
import {PrenotazioneRequest, Fermata} from '../../line-details';
import {SyncService} from '../../presenze/sync.service';
import {Observable} from 'rxjs';
import {BambinoService} from './bambino.service';
import {MatDialog, MatDialogRef} from '@angular/material';
import {DialogAnagraficaComponent} from '../dialog-anagrafica/dialog-anagrafica.component';

@Component({
  selector: 'app-scheda-bambino',
  templateUrl: './scheda-bambino.component.html',
  styleUrls: ['./scheda-bambino.component.scss']
})
export class SchedaBambinoComponent implements OnInit {

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

  constructor(private syncService: SyncService, private bambinoService: BambinoService, private dialog: MatDialog) {
  }

  ngOnInit() {
    this.syncService.prenotazioneObs$.subscribe((data) => {
      this.request = data;
      // todo: inserire logica di reazione a richiesta qui
      if (this.bambino && this.request) {
        this.bambinoService.getStatus(this.bambino, this.request.data).subscribe((reservation) => {
          this.schoolClosed = false;
          for (const res of reservation) {
            if (res.verso) {
              this.andata = res;
            } else {
              this.ritorno = res;
            }
          }
          console.log(reservation);
        }, (error) => {
          this.schoolClosed = true;
          console.log(error);
        });
      }

    });
    this.defaultAndata = this.bambinoService.getFermata(this.bambino.idFermataAndata);
    this.defaultRitorno = this.bambinoService.getFermata(this.bambino.idFermataRitorno);
  }

  showAnagraficaDialog() {
    this.bambinoService.openDialog(this.bambino, this.linee).subscribe((data) => {
      this.bambino = data.data.child;
      this.defaultAndata = this.bambinoService.getFermata(this.bambino.idFermataAndata);
      this.defaultRitorno = this.bambinoService.getFermata(this.bambino.idFermataRitorno);
      this.bambinoService.updateFermate(this.bambino).subscribe((d) => console.log(d), (error) => console.log(error));
    });
  }

  showPrenotazioneDialog() {

  }

}
