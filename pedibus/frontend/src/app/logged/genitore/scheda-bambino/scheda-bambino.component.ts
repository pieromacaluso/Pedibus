import { Component, OnInit, Input } from '@angular/core';
import { ChildrenDTO, FermataDTO } from '../dtos';
import { PrenotazioneRequest, Fermata } from '../../line-details';
import { SyncService } from '../../presenze/sync.service';
import { Observable } from 'rxjs';
import { BambinoService } from './bambino.service';
import { MatDialog, MatDialogRef } from '@angular/material';
import { DialogAnagraficaComponent } from '../dialog-anagrafica/dialog-anagrafica.component';

@Component({
  selector: 'app-scheda-bambino',
  templateUrl: './scheda-bambino.component.html',
  styleUrls: ['./scheda-bambino.component.css']
})
export class SchedaBambinoComponent implements OnInit {

  @Input() bambino: ChildrenDTO;
  @Input() linee: string[];
  request: PrenotazioneRequest;
  defaultAndata: Observable<Fermata>;
  defaultRitorno: Observable<Fermata>;
  status = 'Non prenotato';
  anagraficaDialog: MatDialogRef<DialogAnagraficaComponent>;

  constructor(private syncService: SyncService, private bambinoService: BambinoService, private dialog: MatDialog) {
  }

  ngOnInit() {
    this.syncService.prenotazioneObs$.subscribe((data) => {
      this.request = data;
      // todo: inserire logica di reazione a richiesta qui
      if (this.bambino && this.request) {
        this.bambinoService.getStatus(this.bambino, this.request.data, this.request.verso).subscribe((reservation) => {
          if (!reservation.presoInCarico) {
            this.status = 'Non ancora preso in carico';
          }
          if (reservation.presoInCarico) {
            this.status = 'Preso in carico';
          }
          if (reservation.arrivatoScuola) {
            this.status = 'Arrivato a scuola';
          }
        }, (error) => console.log(error));
      }

    });
    this.defaultAndata = this.bambinoService.getFertmata(this.bambino.idFermataAndata);
    this.defaultRitorno = this.bambinoService.getFertmata(this.bambino.idFermataRitorno);
  }

  showAnagraficaDialog() {
    this.bambinoService.openDialog(this.bambino, this.linee).subscribe((data) => {
      this.bambino = data.data.child;
      this.defaultAndata = this.bambinoService.getFertmata(this.bambino.idFermataAndata);
      this.defaultRitorno = this.bambinoService.getFertmata(this.bambino.idFermataRitorno);
      this.bambinoService.updateFermate(this.bambino).subscribe((data) => console.log(data), (error) => console.log(error));
    });
  }

  showPrenotazioneDialog() {

  }

}
