import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Alunno, AlunnoNotReserved, LineReservationVerso, NuovaPrenotazione, PrenotazioneRequest, StopsByLine} from './line-details';
import {DatePipe} from '@angular/common';
import {DialogData} from './presenze/lista-prenotazioni/admin-book-dialog/admin-book-dialog.component';
import {Observable} from 'rxjs';
import { Notifica } from './notifiche/dtos';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  baseURL = environment.baseURL;

  constructor(private httpClient: HttpClient, private datePipe: DatePipe) {
  }

  getNotificheNonLette(username: string): Observable<Notifica[]> {
    return this.httpClient.get<Notifica[]>(this.baseURL + 'notifiche/' + username);
  }

  deleteNotifica(idNotifica: string): Observable<any[]> {
    return this.httpClient.delete<any[]>(this.baseURL + 'notifiche/' + idNotifica);
  }

  getChildren(): Observable<any[]> {
    return this.httpClient.get<any[]>(this.baseURL + 'children');
  }

  getLinee(): Observable<string[]> {
    return this.httpClient.get<string[]>(this.baseURL + 'lines');
  }

  getStopsByLine(idLinea: string): Observable<StopsByLine> {
    return this.httpClient.get<StopsByLine>(this.baseURL + 'lines/' + idLinea);
  }

  getLineeNomi(): Observable<string[]> {
    return this.httpClient.get<string[]>(this.baseURL + 'lines/name');
  }

  getPrenotazioneByLineaAndDateAndVerso(p: PrenotazioneRequest) {
    const idVerso = this.versoToInt(p.verso);
    if (p.linea && p.data) {
      return this.httpClient.get<LineReservationVerso>(this.baseURL + 'reservations/verso/' + p.linea + '/' +
        this.datePipe.transform(p.data, 'yyyy-MM-dd') + '/' + idVerso);
    }
  }

  /**
   * Segnala la presoInCarico di un alunno
   */
  postPresenza(alunno: Alunno, presenza: PrenotazioneRequest, choice: boolean) {
    const idVerso = this.versoToInt(presenza.verso);
    return this.httpClient
      .post(this.baseURL + 'reservations/handled/' + presenza.linea + '/' + idVerso + '/' + this.datePipe
        .transform(presenza.data, 'yyyy-MM-dd') + '/' + choice, alunno.codiceFiscale);
  }

  getNonPrenotati(date: Date, verso: string) {
    const idVerso = this.versoToInt(verso);
    if (verso && date) {
      return this.httpClient.get<AlunnoNotReserved[]>(
        this.baseURL + 'notreservations/' + this.datePipe.transform(date, 'yyyy-MM-dd') + '/' + idVerso);
    }
  }

  versoToInt(verso: string) {
    return verso === 'Andata' ? 1 : 0;
  }

  versoToBool(verso: string) {
    return verso === 'Andata' ? true : false;
  }

  dateToString(date: Date) {
    return this.datePipe.transform(date, 'yyyy-MM-dd');
  }

  postPrenotazioneDialog(data: DialogData) {
    const idVerso = this.versoToInt(data.verso);
    const nuovaPrenotazione: NuovaPrenotazione = {
      cfChild: data.alunno.codiceFiscale,
      idFermata: data.fermataId,
      verso: idVerso
    };

    console.log('POSTTT:' + nuovaPrenotazione.cfChild + ' ' + nuovaPrenotazione.idFermata + ' ' + nuovaPrenotazione.verso);

    return this.httpClient
      .post(this.baseURL + 'reservations/' + data.linea + '/' + this.datePipe
        .transform(data.data, 'yyyy-MM-dd'), nuovaPrenotazione);
  }

  /**
   * Permette ad un genitore di eliminare una prenotazione riguardante il figlio
   */
  deletePrenotazioneGenitore(codiceFiscale: string, idLinea: string, data: Date, verso: string) {
    return this.httpClient
      .delete(this.baseURL + 'reservations/' + codiceFiscale + '/' + idLinea + '/'
        + this.datePipe.transform(data, 'yyyy-MM-dd') + '/' + this.versoToBool(verso));
  }
}
