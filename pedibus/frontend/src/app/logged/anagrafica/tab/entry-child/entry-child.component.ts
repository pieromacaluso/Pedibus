import {Component, Input, OnInit} from '@angular/core';
import {ChildrenDTO} from '../../../genitore/dtos';
import {AnagraficaService} from '../../anagrafica.service';

@Component({
  selector: 'app-entry-child',
  templateUrl: './entry-child.component.html',
  styleUrls: ['./entry-child.component.scss']
})
export class EntryChildComponent implements OnInit {

  @Input() child: ChildrenDTO;

  constructor(private anagraficaService: AnagraficaService,) {
  }

  ngOnInit() {
  }

  openDialog() {
    this.anagraficaService.openDialogChild(this.child);
  }

  deleteChild() {
    this.anagraficaService.openConfirmationDialog('Sei sicuro di voler eliminare dell\'elenco '
      + this.child.name + ' ' + this.child.surname + '?')
      .subscribe((response) => {
        if (response) {
          this.anagraficaService.deleteChild(this.child.codiceFiscale).subscribe((res) => {
            // TODO: gestisci successo
          }, error => {
            // TODO: gestisci insuccesso.
          });
        }
      });

  }
}
