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
    return this.httpClient.get<string[]>(this.baseURL + 'lines');
  }

  getPrenotazioneByLineaAndDateAndVerso(selectedLinea: string, date: Date) {
      return this.httpClient.get<LineReservation>(this.baseURL + 'reservations' + '/2019-01-01');
  }

}
