import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {
  Alunno,
  AlunnoNotReserved,
  LineReservationVerso,
  NuovaPrenotazione,
  PrenotazioneRequest,
  StopsByLine,
  Fermata
} from './line-details';
import {DatePipe} from '@angular/common';
import {DialogData} from './presenze/lista-prenotazioni/admin-book-dialog/admin-book-dialog.component';
import {Observable} from 'rxjs';
import {Notifica} from './notifiche/dtos';
import {ChildrenDTO, ReservationDTO} from './genitore/dtos';
import {UserDTO} from './anagrafica/dtos';
import {debounceTime, distinctUntilChanged, first, flatMap, map, share, take} from 'rxjs/operators';
import {Line} from 'tslint/lib/verify/lines';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  baseURL = environment.baseURL;

  constructor(private httpClient: HttpClient, private datePipe: DatePipe) {
  }

  getNotificheNonLette(username: string, pageNumber: number): Observable<any> {
    const params = {
      page: JSON.stringify(pageNumber),
      size: JSON.stringify(10),
    };
    return this.httpClient.get<any>(this.baseURL + 'notifiche/all/' + username, {params});
  }

  deleteNotifica(idNotifica: string): Observable<any[]> {
    return this.httpClient.delete<any[]>(this.baseURL + 'notifiche/' + idNotifica);
  }

  getChildren(): Observable<ChildrenDTO[]> {
    return this.httpClient.get<ChildrenDTO[]>(this.baseURL + 'children');
  }

  getAllChildren(pageNumber: number, keyword: string): Observable<any> {
    const params = {
      page: JSON.stringify(pageNumber),
      size: JSON.stringify(10),
      keyword
    };
    return this.httpClient.get<any>(this.baseURL + 'sysadmin/children', {params}).pipe(first());
  }

  getFirst5Children(keyword: string): Observable<ChildrenDTO[]> {
    const params = {
      page: JSON.stringify(0),
      size: JSON.stringify(5),
      keyword
    };
    return this.httpClient.get<any>(this.baseURL + 'sysadmin/children', {params}).pipe(
      debounceTime(1000),
      map((res) => {
        return res.content;
      })
    );
  }

  getLinee(): Observable<string[]> {
    return this.httpClient.get<string[]>(this.baseURL + 'lines');
  }

  getLineeFiltered(): Observable<string[]> {
    return this.httpClient.get<string[]>(this.baseURL + 'lines/filtered');
  }

  getStopsByLine(idLinea: string): Observable<StopsByLine> {
    return this.httpClient.get<StopsByLine>(this.baseURL + 'lines/' + idLinea);
  }

  /**
   * Aggiorna le fermate di default di un bambino
   * @param bambino oggetto corrispondente a ChildDefaultStopResource su backend
   * @param date data da cui aggiornare le prenotazione
   */
  updateFermate(bambino: ChildrenDTO, date: Date): Observable<any> {
    const body = {
      idFermataAndata: bambino.idFermataAndata,
      idFermataRitorno: bambino.idFermataRitorno,
      data: this.datePipe.transform(date, 'yyyy-MM-dd')
    };
    return this.httpClient.put<any>(this.baseURL + 'children/stops/' + bambino.codiceFiscale, body);
  }

  getStatus(bambino: ChildrenDTO, data: Date): Observable<ReservationDTO[]> {
    return this.httpClient.get<ReservationDTO[]>(this.baseURL + 'children/stops/' + bambino.codiceFiscale + '/' + this.datePipe.transform(data, 'yyyy-MM-dd'));
  }

  getFermata(idFermata: number): Observable<Fermata> {
    return this.httpClient.get<Fermata>(this.baseURL + 'lines/stops/' + idFermata);
  }

  getLineeNomi(): Observable<string[]> {
    return this.httpClient.get<string[]>(this.baseURL + 'lines/name');
  }

  getPrenotazioneByLineaAndDateAndVerso(p: PrenotazioneRequest) {
    const idVerso = this.versoToInt(p.verso);
    return this.httpClient.get<LineReservationVerso>(this.baseURL + 'reservations/verso/' + p.linea + '/' +
      this.datePipe.transform(p.data, 'yyyy-MM-dd') + '/' + idVerso);
  }

  /**
   * Segnala la presoInCarico di un alunno
   */
  postPresenza(alunno: Alunno, presenza: PrenotazioneRequest, choice: boolean) {
    const idVerso = this.versoToInt(presenza.verso);
    const choiceNum = choice ? 1 : 0;
    return this.httpClient
      .post(this.baseURL + 'reservations/handled/' + presenza.linea + '/' + idVerso + '/' + this.datePipe
        .transform(presenza.data, 'yyyy-MM-dd') + '/' + choiceNum, alunno.codiceFiscale);
  }

  /**
   * Segnala la assente di un alunno
   */
  postAssenza(alunno: Alunno, presenza: PrenotazioneRequest, choice: boolean) {
    const idVerso = this.versoToInt(presenza.verso);
    const choiceNum = choice ? 1 : 0;
    return this.httpClient
      .post(this.baseURL + 'reservations/assente/' + presenza.linea + '/' + idVerso + '/' + this.datePipe
        .transform(presenza.data, 'yyyy-MM-dd') + '/' + choiceNum, alunno.codiceFiscale);
  }

  /**
   * Segnala la presoInCarico di un alunno
   */
  postArrivato(alunno: Alunno, presenza: PrenotazioneRequest, choice: boolean) {
    const idVerso = this.versoToInt(presenza.verso);
    const choiceNum = choice ? 1 : 0;
    return this.httpClient
      .post(this.baseURL + 'reservations/arrived/' + presenza.linea + '/' + idVerso + '/' + this.datePipe
        .transform(presenza.data, 'yyyy-MM-dd') + '/' + choiceNum, alunno.codiceFiscale);
  }

  /**
   * Segnala la presoInCarico di un alunno
   */
  postRestore(alunno: Alunno, presenza: PrenotazioneRequest) {
    const idVerso = this.versoToInt(presenza.verso);
    return this.httpClient
      .post(this.baseURL + 'reservations/restore/' + presenza.linea + '/' + idVerso + '/' + this.datePipe
        .transform(presenza.data, 'yyyy-MM-dd'), alunno.codiceFiscale);
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
    return this.postPrenotazione(data.linea, data.data, nuovaPrenotazione);
  }

  postPrenotazione(linea: string, data: Date, prenotazione: NuovaPrenotazione) {
    return this.httpClient
      .post(this.baseURL + 'reservations/' + linea + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'), prenotazione);
  }

  updatePrenotazione(linea: string, data: Date, prenotazione: NuovaPrenotazione, id: string) {
    return this.httpClient
      .put(this.baseURL + 'reservations/' + linea + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd') + '/' + id, prenotazione);
  }

  deletePrenotazione(linea: string, data: Date, id: string) {
    return this.httpClient
      .delete(this.baseURL + 'reservations/' + linea + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd') + '/' + id);
  }

  /**
   * Permette ad un genitore di eliminare una prenotazione riguardante il figlio
   */
  deletePrenotazioneGenitore(codiceFiscale: string, idLinea: string, data: Date, verso: string) {
    return this.httpClient
      .delete(this.baseURL + 'reservations/' + codiceFiscale + '/' + idLinea + '/'
        + this.datePipe.transform(data, 'yyyy-MM-dd') + '/' + this.versoToBool(verso));
  }

  getUsers(pageNumber: number, keyword: string): Observable<any> {
    const params = {
      page: JSON.stringify(pageNumber),
      size: JSON.stringify(10),
      keyword
    };
    return this.httpClient.get<any>(this.baseURL + '/sysadmin/users', {params}).pipe(first());
  }

  addChild(child: ChildrenDTO) {
    return this.httpClient
      .post(this.baseURL + 'sysadmin/children/', child).pipe(first());
  }

  updateChild(codiceFiscale: string, child: ChildrenDTO) {
    return this.httpClient.put<ChildrenDTO>(this.baseURL + 'sysadmin/children/' + codiceFiscale, child).pipe(first());
  }

  deleteChild(codiceFiscale: string) {
    return this.httpClient.delete<void>(this.baseURL + 'sysadmin/children/' + codiceFiscale).pipe(first());
  }

  deleteUser(userId: string) {
    return this.httpClient.delete<void>(this.baseURL + 'sysadmin/users/' + userId).pipe(first());
  }

  getRoles(): Observable<string[]> {
    return this.httpClient.get<string[]>(this.baseURL + '/sysadmin/roles').pipe(first());

  }

  getChild(child: any): Observable<ChildrenDTO> {
    return this.httpClient.get<ChildrenDTO>(this.baseURL + 'sysadmin/children/' + child);
  }

  updateUser(oldEmail: string, user: UserDTO) {
    return this.httpClient.put<UserDTO>(this.baseURL + 'sysadmin/users/' + oldEmail, user).pipe(first());
  }

  createUser(user: UserDTO) {
    return this.httpClient
      .post(this.baseURL + 'sysadmin/users', user).pipe(first());
  }

  getLineAdmin(): Observable<Map<string, StopsByLine>> {
    return this.httpClient.get<Map<string, StopsByLine>>(this.baseURL + 'admin/lines').pipe(first(),
      map(
        (res) => {
          const lineMap = new Map<string, StopsByLine>();
          res.forEach((value: StopsByLine) => {
            lineMap.set(value.id, value);
          });
          return lineMap;
        }
      ));
  }

  getGuides() {
    return this.httpClient.get<UserDTO[]>(this.baseURL + '/admin/users').pipe(first());
  }

  downloadJson(prenotazione: PrenotazioneRequest) {
    return this.httpClient.get<any>(this.baseURL + '/reservations/dump/' + prenotazione.linea + '/'
      + this.datePipe.transform(prenotazione.data, 'yyyy-MM-dd') + '/' + this.versoToBool(prenotazione.verso));
  }
}
