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
import {ConfirmationDialogComponent} from '../utilities/confimation-dialog/confirmation-dialog.component';
import {MatDialog} from '@angular/material';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  baseURL = environment.baseURL;

  constructor(private httpClient: HttpClient, private datePipe: DatePipe, public dialog: MatDialog) {
  }

  /**
   * Ottieni notifiche non lette
   * @param username username dell'utente
   * @param pageNumber numero di pagina (paginazione)
   */
  getNotificheNonLette(username: string, pageNumber: number): Observable<any> {
    const params = {
      page: JSON.stringify(pageNumber),
      size: JSON.stringify(10),
    };
    return this.httpClient.get<any>(this.baseURL + 'notifiche/all/' + username, {params});
  }

  /**
   * Cancellazione Notifica dato ID
   * @param idNotifica id della notifica
   */
  deleteNotifica(idNotifica: string): Observable<any[]> {
    return this.httpClient.delete<any[]>(this.baseURL + 'notifiche/' + idNotifica);
  }

  /**
   * Ottieni elenco bambini
   * @deprecated
   */
  getChildren(): Observable<ChildrenDTO[]> {
    return this.httpClient.get<ChildrenDTO[]>(this.baseURL + 'children');
  }

  /**
   * Ottieni bambini paginati
   * @param pageNumber numero di pagina
   * @param keyword keyword di ricerca
   */
  getAllChildren(pageNumber: number, keyword: string): Observable<any> {
    const params = {
      page: JSON.stringify(pageNumber),
      size: JSON.stringify(10),
      keyword
    };
    return this.httpClient.get<any>(this.baseURL + 'sysadmin/children', {params}).pipe(first());
  }

  /**
   * Ottieni 5 bambini secondo parametro di ricerca
   * @param keyword keyword di ricerca
   */
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

  /**
   * Ottieni tutti gli ID delle linee
   */
  getLinee(): Observable<string[]> {
    return this.httpClient.get<string[]>(this.baseURL + 'lines');
  }

  /**
   * Ottieni tutti gli ID delle linee di tua competenza
   */
  getLineeFiltered(): Observable<string[]> {
    return this.httpClient.get<string[]>(this.baseURL + 'lines/filtered');
  }

  /**
   * Ottieni informazioni dettagliate sulla linea
   * @param idLinea id linea
   */
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

  /**
   * Ottieni stato del bambino
   * @param bambino ChildrenDTO
   * @param data data
   */
  getStatus(bambino: ChildrenDTO, data: Date): Observable<ReservationDTO[]> {
    return this.httpClient.get<ReservationDTO[]>(this.baseURL + 'children/stops/' + bambino.codiceFiscale + '/'
      + this.datePipe.transform(data, 'yyyy-MM-dd'));
  }

  /**
   * Ottieni la fermata a partire dal suo ID
   * @param idFermata id fermata
   */
  getFermata(idFermata: number): Observable<Fermata> {
    return this.httpClient.get<Fermata>(this.baseURL + 'lines/stops/' + idFermata);
  }

  /**
   * Ottieni l'elenco dei nomi delle linee
   * @deprecated
   */
  getLineeNomi(): Observable<string[]> {
    return this.httpClient.get<string[]>(this.baseURL + 'lines/name');
  }

  /**
   * Ottieni prenotazione tramite Linea, data e verso
   * @param p Linea, Data e Verso
   */
  getPrenotazioneByLineaAndDateAndVerso(p: PrenotazioneRequest): Observable<LineReservationVerso> {
    const idVerso = this.versoToInt(p.verso);
    return this.httpClient.get<LineReservationVerso>(this.baseURL + 'reservations/verso/' + p.linea + '/' +
      this.datePipe.transform(p.data, 'yyyy-MM-dd') + '/' + idVerso);
  }

  /**
   * Segnala la presa in carico di un alunno
   * @param alunno Alunno
   * @param presenza prenotazione di cui segnalare la presa in carico
   * @param choice true se preso in carico, false altrimenti
   */
  postPresenza(alunno: Alunno, presenza: PrenotazioneRequest, choice: boolean) {
    const idVerso = this.versoToInt(presenza.verso);
    const choiceNum = choice ? 1 : 0;
    return this.httpClient
      .post(this.baseURL + 'reservations/handled/' + presenza.linea + '/' + idVerso + '/' + this.datePipe
        .transform(presenza.data, 'yyyy-MM-dd') + '/' + choiceNum, alunno.codiceFiscale);
  }

  /**
   * Segnala l'assenza di un alunno
   * @param alunno Alunno
   * @param presenza prenotazione di cui segnalare l'assenza
   * @param choice true se assente, false altrimenti
   */
  postAssenza(alunno: Alunno, presenza: PrenotazioneRequest, choice: boolean) {
    const idVerso = this.versoToInt(presenza.verso);
    const choiceNum = choice ? 1 : 0;
    return this.httpClient
      .post(this.baseURL + 'reservations/assente/' + presenza.linea + '/' + idVerso + '/' + this.datePipe
        .transform(presenza.data, 'yyyy-MM-dd') + '/' + choiceNum, alunno.codiceFiscale);
  }

  /**
   * Segnala l'arrivo a destinazione di un alunno
   * @param alunno Alunno
   * @param presenza prenotazione di cui segnalare l'arrivo a destinazione
   * @param choice true se arrivato, false altrimenti
   */
  postArrivato(alunno: Alunno, presenza: PrenotazioneRequest, choice: boolean) {
    const idVerso = this.versoToInt(presenza.verso);
    const choiceNum = choice ? 1 : 0;
    return this.httpClient
      .post(this.baseURL + 'reservations/arrived/' + presenza.linea + '/' + idVerso + '/' + this.datePipe
        .transform(presenza.data, 'yyyy-MM-dd') + '/' + choiceNum, alunno.codiceFiscale);
  }

  /**
   * Resetta la prenotazione allo stato iniziale
   * @param alunno Alunno
   * @param presenza prenotazione da resettare
   */
  postRestore(alunno: Alunno, presenza: PrenotazioneRequest) {
    const idVerso = this.versoToInt(presenza.verso);
    return this.httpClient
      .post(this.baseURL + 'reservations/restore/' + presenza.linea + '/' + idVerso + '/' + this.datePipe
        .transform(presenza.data, 'yyyy-MM-dd'), alunno.codiceFiscale);
  }

  /**
   * @deprecated vecchio flusso dell'applicazione
   * @param date data
   * @param verso verso
   */
  getNonPrenotati(date: Date, verso: string) {
    const idVerso = this.versoToInt(verso);
    if (verso && date) {
      return this.httpClient.get<AlunnoNotReserved[]>(
        this.baseURL + 'notreservations/' + this.datePipe.transform(date, 'yyyy-MM-dd') + '/' + idVerso);
    }
  }

  /**
   * Da verso a intero
   * @param verso verso
   */
  versoToInt(verso: string) {
    return verso === 'Andata' ? 1 : 0;
  }

  /**
   * Da verso a bool
   * @param verso verso
   */
  versoToBool(verso: string) {
    return verso === 'Andata' ? true : false;
  }

  /**
   * Da data a stringa
   * @param date data
   */
  dateToString(date: Date) {
    return this.datePipe.transform(date, 'yyyy-MM-dd');
  }

  /**
   * Posta prenotazione attraverso il dialog della guida
   * @param data dati del dialog
   */
  postPrenotazioneDialog(data: DialogData) {
    const idVerso = this.versoToInt(data.verso);
    const nuovaPrenotazione: NuovaPrenotazione = {
      cfChild: data.alunno.codiceFiscale,
      idFermata: data.fermataId,
      verso: idVerso
    };
    return this.postPrenotazione(data.linea, data.data, nuovaPrenotazione);
  }

  /**
   * Post di una nuova prenotazione
   * @param linea linea
   * @param data data
   * @param prenotazione dati della nuova prenotazione
   */
  postPrenotazione(linea: string, data: Date, prenotazione: NuovaPrenotazione) {
    return this.httpClient
      .post(this.baseURL + 'reservations/' + linea + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'), prenotazione);
  }

  /**
   * Aggiornamento di una prenotazione
   * @param linea linea
   * @param data data
   * @param prenotazione dati della nuova prenotazione
   * @param id id della fermata
   */
  updatePrenotazione(linea: string, data: Date, prenotazione: NuovaPrenotazione, id: string) {
    return this.httpClient
      .put(this.baseURL + 'reservations/' + linea + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd') + '/' + id, prenotazione);
  }

  /**
   * Cancellazione della prenotazione
   * @param linea linea
   * @param data data
   * @param id id della fermata
   */
  deletePrenotazione(linea: string, data: Date, id: string) {
    return this.httpClient
      .delete(this.baseURL + 'reservations/' + linea + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd') + '/' + id);
  }

  /**
   * Permette ad un genitore di eliminare una prenotazione riguardante il proprio figlio
   * @param codiceFiscale codice fiscale bambino
   * @param idLinea id linea
   * @param data data
   * @param verso verso
   */
  deletePrenotazioneGenitore(codiceFiscale: string, idLinea: string, data: Date, verso: string) {
    return this.httpClient
      .delete(this.baseURL + 'reservations/' + codiceFiscale + '/' + idLinea + '/'
        + this.datePipe.transform(data, 'yyyy-MM-dd') + '/' + this.versoToBool(verso));
  }

  /**
   * Ottieni tutti gli user paginati
   * @param pageNumber numero di pagina
   * @param keyword keyword di ricerca
   */
  getUsers(pageNumber: number, keyword: string): Observable<any> {
    const params = {
      page: JSON.stringify(pageNumber),
      size: JSON.stringify(10),
      keyword
    };
    return this.httpClient.get<any>(this.baseURL + '/sysadmin/users', {params}).pipe(first());
  }

  /**
   * Aggiungi Bambino da lato admin
   * @param child bambino
   */
  addChild(child: ChildrenDTO) {
    return this.httpClient
      .post(this.baseURL + 'sysadmin/children/', child).pipe(first());
  }

  /**
   * Aggiornamento bambino da lato admin
   * @param codiceFiscale codice fiscale
   * @param child oggetto ChildrenDTO
   */
  updateChild(codiceFiscale: string, child: ChildrenDTO) {
    return this.httpClient.put<ChildrenDTO>(this.baseURL + 'sysadmin/children/' + codiceFiscale, child).pipe(first());
  }

  /**
   * Delete bambino da lato admin
   * @param codiceFiscale codice fiscale
   */
  deleteChild(codiceFiscale: string) {
    return this.httpClient.delete<void>(this.baseURL + 'sysadmin/children/' + codiceFiscale).pipe(first());
  }

  /**
   * Delete user da lato admin
   * @param userId email
   */
  deleteUser(userId: string) {
    return this.httpClient.delete<void>(this.baseURL + 'sysadmin/users/' + userId).pipe(first());
  }

  /**
   * Ottieni tutti i ruoli da sysadmin
   */
  getRoles(): Observable<string[]> {
    return this.httpClient.get<string[]>(this.baseURL + '/sysadmin/roles').pipe(first());
  }

  /**
   * Ottieni Bambino lato admin
   * @param child codice fiscale del bambino
   */
  getChild(child: any): Observable<ChildrenDTO> {
    return this.httpClient.get<ChildrenDTO>(this.baseURL + 'sysadmin/children/' + child);
  }

  /**
   * Aggiornamento utente lato admin
   * @param oldEmail vecchia email
   * @param user utente aggiornato
   */
  updateUser(oldEmail: string, user: UserDTO) {
    return this.httpClient.put<UserDTO>(this.baseURL + 'sysadmin/users/' + oldEmail, user).pipe(first());
  }

  /**
   * Creazione Utente lato admin
   * @param user dati utente
   */
  createUser(user: UserDTO) {
    return this.httpClient
      .post(this.baseURL + 'sysadmin/users', user).pipe(first());
  }

  /**
   * Ottieni le linee di cui sei admin e trasformale in una mappa
   */
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

  /**
   * Ottieni le guide
   */
  getGuides() {
    return this.httpClient.get<UserDTO[]>(this.baseURL + '/admin/users').pipe(first());
  }

  /**
   * Scarica il file JSON del turno selezionato
   * @param prenotazione turno selezionato
   */
  downloadJson(prenotazione: PrenotazioneRequest) {
    return this.httpClient.get<any>(this.baseURL + 'admin/reservations/dump/' + prenotazione.linea + '/'
      + this.datePipe.transform(prenotazione.data, 'yyyy-MM-dd') + '/' + this.versoToBool(prenotazione.verso));
  }

  /**
   * Apri schermata di conferma
   * @param message messaggio da stampare nel dialog
   */
  openConfirmationDialog(message: string) {
    let dialogRef;
    dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {message}
    });

    return dialogRef.afterClosed();
  }

  /**
   * Cancellazione prenotazione da parte dell'admin di sistema
   * @param data data
   * @param verso verso
   * @param cfChild codice fiscale bambino
   */
  deletePrenotazioneAdmin(data: Date, verso: string, cfChild: string) {
    const idVerso = this.versoToInt(verso);
    return this.httpClient
      .delete(this.baseURL + 'sysadmin/reservations/' + this.datePipe
        .transform(data, 'yyyy-MM-dd') + '/' + idVerso + '/' + cfChild);
  }
}
