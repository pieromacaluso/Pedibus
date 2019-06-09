import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {LineReservation} from './line-details';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  baseURL = environment.baseURL;

  constructor(private httpClient: HttpClient) {
  }

  getLinee() {
    console.log('invio richiesta getLinee');
    return this.httpClient.get<string[]>(this.baseURL + 'lines');
  }

  getPrenotazioneByLineaAndDateAndVerso(selectedLinea: string, date: Date) {
    console.log('invio richiesta getPrenotazioneByLineaAndDateAndVerso ' + date.toLocaleDateString());
    if (selectedLinea && date) {
      return this.httpClient.get<LineReservation>(this.baseURL + 'reservations/' + selectedLinea + '/2019-01-01');
    }
  }

}
