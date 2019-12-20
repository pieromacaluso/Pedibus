import { Component, OnInit, Input } from '@angular/core';
import { ChildrenDTO } from '../dtos';
import { PrenotazioneRequest } from '../../line-details';
import { SyncService } from '../../presenze/sync.service';

@Component({
  selector: 'app-scheda-bambino',
  templateUrl: './scheda-bambino.component.html',
  styleUrls: ['./scheda-bambino.component.css']
})
export class SchedaBambinoComponent implements OnInit {

  @Input() bambino: ChildrenDTO;
  request: PrenotazioneRequest;

  constructor(private syncService: SyncService) {
    this.syncService.prenotazioneObs$.subscribe((data) => {
      this.request = data;
      console.log("request:", this.request);
      // todo: inserire logica di reazione a richiesta qui
    })
   }

  ngOnInit() {
  }

}
