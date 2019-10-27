import {Component, OnInit} from '@angular/core';
import {ApiService} from '../api.service';
import {Observable} from 'rxjs';
import { PrenotazioneRequest } from '../line-details';

@Component({
  selector: 'app-presenze',
  templateUrl: './presenze.component.html',
  styleUrls: ['./presenze.component.scss']
})
export class PresenzeComponent implements OnInit {

  // refactored
  linee$: Observable<string[]>;
  prenotazione: PrenotazioneRequest;

  constructor(private mongoService: ApiService) {
    this.linee$ = this.mongoService.getLinee();
  }

  updatePrenotazione(newPrenotazione: PrenotazioneRequest) {
    this.prenotazione = newPrenotazione;
  }

  ngOnInit() {
  }
}
