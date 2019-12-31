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

  constructor(private apiService: ApiService, public dialog: MatDialog, private rxStompService: RxStompService, private datePipe: DatePipe) {

  }

  getFermata(idFermata: number): Observable<Fermata> {
    return this.apiService.getFermata(idFermata);
  }

  updateFermate(data: ChildrenDTO, date: Date): Observable<any> {
    return this.apiService.updateFermate(data, date);
  }

  getStatus(bambino: ChildrenDTO, data: Date): Observable<ReservationDTO[]> {
    return this.apiService.getStatus(bambino, data);
  }

  openDialog(bambino: ChildrenDTO, linee: string[], defaultAndata: Observable<Fermata>, defaultRitorno: Observable<Fermata>): Observable<any> {
    const dialogRef = this.dialog.open(DialogAnagraficaComponent, {
      hasBackdrop: true,
      data: {child: bambino, linee, defaultAndata, defaultRitorno}
    });
    return dialogRef.afterClosed();
  }

  watchChild(cf: string) {
    return this.rxStompService.watch('/user/child/' + cf);
  }

  watchChildReservation(cf: string, data: Date) {
    return this.rxStompService.watch('/user/child/res/' + cf + '/' + this.datePipe.transform(data, 'yyyy-MM-dd'));
  }

  openDialogReservation(bambino: ChildrenDTO, andata: boolean, data: Date, linee: string[], aggiunta: boolean, reservation: ReservationDTO) {
    const dialogRef = this.dialog.open(DialogPrenotazioneComponent, {
      hasBackdrop: true,
      data: {bambino, andata, data, linee, aggiunta, reservation}
    });
    return dialogRef.afterClosed();
  }

  createReservation(linea: string, data: Date, childId: string, stopId: number, verso: boolean) {
    const prenotazione: NuovaPrenotazione = {
      cfChild: childId,
      idFermata: stopId,
      verso: verso ? 1 : 0
    };
    return this.apiService.postPrenotazione(linea, data, prenotazione);
  }

  updateReservation(linea: string, data: Date, childId: string, stopId: number, verso: boolean, id: string) {
    const prenotazione: NuovaPrenotazione = {
      cfChild: childId,
      idFermata: stopId,
      verso: verso ? 1 : 0
    };
    return this.apiService.updatePrenotazione(linea, data, prenotazione, id);
  }

  deleteReservation(linea: string, data: Date, id: string) {
    return this.apiService.deletePrenotazione(linea, data, id);
  }

}
