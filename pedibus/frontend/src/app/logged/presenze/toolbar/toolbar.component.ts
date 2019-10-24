import {Component, Input, OnChanges, OnInit, Output, SimpleChanges, EventEmitter} from '@angular/core';
import {PrenotazioneRequest} from '../../line-details';
import {SyncService} from '../sync.service';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss']
})
export class ToolbarComponent implements OnChanges {

  @Input() linee: string[];
  @Input() showVerso: boolean;
  @Input() showLinea: boolean;
  richiesta: PrenotazioneRequest;


  constructor(private syncService: SyncService) {
  }

  ngOnChanges(changes: SimpleChanges) {
    console.log(changes);
    if (changes.linee && this.linee != null) {
      this.richiesta = {linea: this.linee[0], verso: 'Andata', data: new Date()};
      this.richiesta.linea.split('linea');
      this.emitRequest();
    }
  }

  emitRequest() {
    if ((this.richiesta.linea !== '' || !this.showLinea) && (this.richiesta.verso !== '' || !this.showVerso)) {
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
