import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {PrenotazioneRequest} from '../line-details';

@Injectable({
  providedIn: 'root'
})
export class SyncService {

  private prenotazioneRequest = new BehaviorSubject<PrenotazioneRequest>({linea: 'linea1', verso: 'Andata', data: new Date()});
  prenotazioneObs$ = this.prenotazioneRequest.asObservable();

  constructor() {
  }

  updatePrenotazione(prenotazione: PrenotazioneRequest) {
    this.prenotazioneRequest.next(prenotazione);
  }

}
