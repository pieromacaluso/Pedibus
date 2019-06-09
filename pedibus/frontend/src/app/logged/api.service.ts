import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {LineReservation, NotReservation} from './line-details';
import {DatePipe} from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  baseURL = environment.baseURL;

  constructor(private httpClient: HttpClient, private datePipe: DatePipe) {
  }

  getLinee() {
    console.log('invio richiesta getLinee');
    return this.httpClient.get<string[]>(this.baseURL + 'lines');
  }

  getPrenotazioneByLineaAndDateAndVerso(selectedLinea: string, date: Date) {
    console.log('invio richiesta getPrenotazioneByLineaAndDateAndVerso ' + date.toLocaleDateString());
    if (selectedLinea && date) {
      return this.httpClient.get<LineReservation>(this.baseURL + 'reservations/' + selectedLinea + '/' +
        this.datePipe.transform(date, 'yyyy-MM-dd'));
    }
  }

  getNonPrenotati(date: Date, verso: string) {
    let idVerso;
    if (verso === 'Andata') {
      idVerso = 0;
    } else {
      idVerso = 1;
    }
    console.log('invio richiesta getNonPrenotati ' + date.toLocaleDateString());
    if (verso && date) {
      return this.httpClient.get<NotReservation>(
        this.baseURL + 'notreservations/' + this.datePipe.transform(date, 'yyyy-MM-dd') + '/' + idVerso);
    }
  }
}
