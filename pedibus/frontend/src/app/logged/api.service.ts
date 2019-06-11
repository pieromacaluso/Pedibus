import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Alunno, LineReservation, NotReservation, PrenotazioneRequest} from './line-details';
import {DatePipe} from '@angular/common';
import {privateEncrypt} from 'crypto';

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

  /**
   * Segnala la presoInCarico di un alunno
   */
  postPresenza(alunno: Alunno, presenza: PrenotazioneRequest, choice: boolean) {
    const idVerso = this.versoToInt(presenza.verso);
    console.log(this.baseURL + 'reservations/handled/' + presenza.linea + '/' + idVerso + '/' + this.datePipe
      .transform(presenza.data, 'yyyy-MM-dd') + '/' + choice, alunno.codiceFiscale);
    return this.httpClient
      .post(this.baseURL + 'reservations/handled/' + presenza.linea + '/' + idVerso + '/' + this.datePipe
        .transform(presenza.data, 'yyyy-MM-dd') + '/' + choice, alunno.codiceFiscale);
  }

  getNonPrenotati(date: Date, verso: string) {
    const idVerso = this.versoToInt(verso);
    console.log('invio richiesta getNonPrenotati ' + date.toLocaleDateString());
    if (verso && date) {
      return this.httpClient.get<NotReservation>(
        this.baseURL + 'notreservations/' + this.datePipe.transform(date, 'yyyy-MM-dd') + '/' + idVerso);
    }
  }

  versoToInt(verso: string) {
    if (verso === 'Andata') {
      return 1;
    } else {
      return 0;
    }
  }
}
