import {Injectable} from '@angular/core';
import {ApiService} from '../../api.service';
import {Observable} from 'rxjs';
import {Fermata, NuovaPrenotazione} from '../../line-details';
import {MatDialog} from '@angular/material';
import {DialogAnagraficaComponent} from '../dialog-anagrafica/dialog-anagrafica.component';
import {ChildrenDTO, ReservationDTO} from '../dtos';
import {RxStompService} from '@stomp/ng2-stompjs';
import {DialogPrenotazioneComponent} from '../dialog-prenotazione/dialog-prenotazione.component';
import {DatePipe} from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class BambinoService {

  constructor(
    private apiService: ApiService, public dialog: MatDialog, private rxStompService: RxStompService, private datePipe: DatePipe) {

  }


  /**
   * Ottieni dati sulla fermata dall'ID
   * @param idFermata id della fermata
   */
  getFermata(idFermata: number): Observable<Fermata> {
    return this.apiService.getFermata(idFermata);
  }

  /**
   * Aggiorna le fermate di default per il tuo bambino
   * @param data bambino contenente i dati
   * @param date data da cui partire
   */
  updateFermate(data: ChildrenDTO, date: Date): Observable<any> {
    return this.apiService.updateFermate(data, date);
  }

  /**
   * Ottieni stato del bambino
   * @param bambino struttura bambino
   * @param data data
   */
  getStatus(bambino: ChildrenDTO, data: Date): Observable<ReservationDTO[]> {
    return this.apiService.getStatus(bambino, data);
  }

  /**
   * Apri finestra di dialogo per la modifica delle fermate di default del bambino
   * @param bambino struttura bambino
   * @param linee vettore delle linee
   * @param defaultAndata fermata di andata di default
   * @param defaultRitorno fermata di ritorno di default
   */
  openDialog(bambino: ChildrenDTO, linee: string[], defaultAndata: Observable<Fermata>, defaultRitorno: Observable<Fermata>): Observable<any> {
    const dialogRef = this.dialog.open(DialogAnagraficaComponent, {
      hasBackdrop: true,
      data: {child: bambino, linee, defaultAndata, defaultRitorno}
    });
    return dialogRef.afterClosed();
  }

  /**
   * Websocket per aggiornamenti del proprio figlio, dato il suo codice fiscale
   * @param cf codice fiscale
   */
  watchChild(cf: string) {
    return this.rxStompService.watch('/user/child/' + cf);
  }

  /**
   * Websocket per il aggiornamenti sulla modifica della prenotazione del proprio figlio
   * @param cf codice fiscale
   * @param data data
   */
  watchChildReservation(cf: string, data: Date) {
    return this.rxStompService.watch('/user/child/res/' + cf + '/' + this.datePipe.transform(data, 'yyyy-MM-dd'));
  }

  /**
   * Apri finestra di dialogo per inserire la prenotazione
   * @param bambino struttura bambino
   * @param andata true se andata, false altrimenti
   * @param data data
   * @param linee vettore delle linee
   * @param aggiunta true se aggiunta, false se modifica
   * @param reservation nel caso in cui il precedente booleano fosse true, mando la reservation
   */
  openDialogReservation(bambino: ChildrenDTO, andata: boolean, data: Date, linee: string[], aggiunta: boolean, reservation: ReservationDTO) {
    const dialogRef = this.dialog.open(DialogPrenotazioneComponent, {
      hasBackdrop: true,
      data: {bambino, andata, data, linee, aggiunta, reservation}
    });
    return dialogRef.afterClosed();
  }

  /**
   * Crea la reservation
   * @param linea linea
   * @param data data
   * @param childId codice fiscale del bambino
   * @param stopId id della fermata
   * @param verso verso
   */
  createReservation(linea: string, data: Date, childId: string, stopId: number, verso: boolean) {
    const prenotazione: NuovaPrenotazione = {
      cfChild: childId,
      idFermata: stopId,
      verso: verso ? 1 : 0
    };
    return this.apiService.postPrenotazione(linea, data, prenotazione);
  }

  /**
   * Aggiornamento prenotazione
   * @param linea linea
   * @param data data
   * @param childId codice fiscale bambino
   * @param stopId id della fermata
   * @param verso verso
   * @param id id della prenotazione
   */
  updateReservation(linea: string, data: Date, childId: string, stopId: number, verso: boolean, id: string) {
    const prenotazione: NuovaPrenotazione = {
      cfChild: childId,
      idFermata: stopId,
      verso: verso ? 1 : 0
    };
    return this.apiService.updatePrenotazione(linea, data, prenotazione, id);
  }

  /**
   * Cancellazione della prenotazione
   * @param linea linea
   * @param data data
   * @param id id della prenotazione
   */
  deleteReservation(linea: string, data: Date, id: string) {
    return this.apiService.deletePrenotazione(linea, data, id);
  }

}
