import {Component, Input, OnInit, Output} from '@angular/core';
import {PrenotazioneRequest} from '../../line-details';
import {SyncService} from '../sync.service';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.css']
})
export class ToolbarComponent implements OnInit {

  @Input() linee;
  richiesta: PrenotazioneRequest;


  constructor(private syncService: SyncService) {
    this.richiesta = {linea: '', verso: '', data: new Date()};
  }

  ngOnInit() {
  }

  emitRequest() {
    this.syncService.updatePrenotazione(this.richiesta);
  }

  modifyDate(days: number) {
    const nextDate = new Date(this.richiesta.data);
    nextDate.setDate(nextDate.getDate() + days);
    this.richiesta.data = nextDate;
    this.emitRequest();
  }

}
