import { Component, OnInit, Input } from '@angular/core';
import { ChildrenDTO, FermataDTO } from '../dtos';
import { PrenotazioneRequest } from '../../line-details';
import { SyncService } from '../../presenze/sync.service';
import { Observable } from 'rxjs';
import { BambinoService } from './bambino.service';

@Component({
  selector: 'app-scheda-bambino',
  templateUrl: './scheda-bambino.component.html',
  styleUrls: ['./scheda-bambino.component.css']
})
export class SchedaBambinoComponent implements OnInit {

  @Input() bambino: ChildrenDTO;
  request: PrenotazioneRequest;
  defaultAndata: Observable<FermataDTO>;
  defaultRitorno: Observable<FermataDTO>;

  constructor(private syncService: SyncService, private bambinoService: BambinoService) {

    this.syncService.prenotazioneObs$.subscribe((data) => {
      this.request = data;
      // todo: inserire logica di reazione a richiesta qui
    })
   }

  ngOnInit() {
    this.defaultAndata = this.bambinoService.getFertmata(this.bambino.idFermataAndata);
    this.defaultRitorno = this.bambinoService.getFertmata(this.bambino.idFermataRitorno);
  }

}
