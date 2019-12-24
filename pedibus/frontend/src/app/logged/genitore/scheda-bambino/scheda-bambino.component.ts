import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ChildrenDTO, FermataDTO } from '../dtos';
import { PrenotazioneRequest } from '../../line-details';
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
export class SchedaBambinoComponent implements OnChanges {

  @Input() bambino: ChildrenDTO;
  @Input() linee: string[];
  request: PrenotazioneRequest;
  defaultAndata: Observable<FermataDTO>;
  defaultRitorno: Observable<FermataDTO>;
  anagraficaDialog: MatDialogRef<DialogAnagraficaComponent>

  constructor(private syncService: SyncService, private bambinoService: BambinoService, private dialog: MatDialog) {
    this.syncService.prenotazioneObs$.subscribe((data) => {
      this.request = data;
      // todo: inserire logica di reazione a richiesta qui
    })
  }

  ngOnChanges(changes: SimpleChanges) {
    // primo caricamento o refresh
    if (!this.bambinoService.defaultAndata) {
      this.bambinoService.getFertmata(this.bambino.idFermataAndata, true);
      this.bambinoService.getFertmata(this.bambino.idFermataRitorno, false);
    }
    this.defaultAndata = this.bambinoService.defaultAndata;
    this.defaultRitorno = this.bambinoService.defaultRitorno;
  }

  showAnagraficaDialog() {
    this.dialog.closeAll();
    this.anagraficaDialog = this.dialog.open(DialogAnagraficaComponent, {
      hasBackdrop: true,
      data: { child: this.bambino, linee: this.linee }
    });
  }

  showPrenotazioneDialog() {

  }

}
