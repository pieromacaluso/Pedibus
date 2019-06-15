import {Component, Input, OnInit, Output} from '@angular/core';
import {PrenotazioneRequest} from '../../line-details';
import {SyncService} from '../sync.service';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss']
})
export class ToolbarComponent implements OnInit {

  @Input() linee;
  @Input() lineeNomi;
  richiesta: PrenotazioneRequest;


  constructor(private syncService: SyncService) {
    this.richiesta = {linea: 'linea1', verso: 'Andata', data: new Date()};
    this.richiesta.linea.split('linea')
    this.emitRequest();
  }

  ngOnInit() {
  }

  emitRequest() {
    if (this.richiesta.linea !== '' && this.richiesta.verso !== '') {
      this.syncService.updatePrenotazione(this.richiesta);
    }
  }

  modifyDate(days: number) {
    const nextDate = new Date(this.richiesta.data);
    nextDate.setDate(nextDate.getDate() + days);
    this.richiesta.data = nextDate;
    this.emitRequest();
  }

}
