import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {Notifica} from './logged/notifiche/dtos';
import {PageEvent} from '@angular/material';

@Injectable({
  providedIn: 'root'
})
export class DataShareService {

  private comunicazioniSource = new BehaviorSubject<Notifica[]>([]);
  comunicazioni = this.comunicazioniSource.asObservable();
  private comunicazioniNumberSource = new BehaviorSubject<number>(0);
  comunicazioniTotal = this.comunicazioniNumberSource.asObservable();
  comuArray: Notifica[] = [];
  totalNotifications: number;

  constructor() {
  }

  /**
   * Aggiorna le notifiche
   * @param notifiche notifiche da emettere
   */
  updateNotifiche(notifiche: Notifica[]) {
    this.comuArray = notifiche;
    this.comunicazioniSource.next(this.comuArray);
  }

  /**
   * Aggiorna il totale delle notifiche
   * @param totalNumber numero totale
   */
  updateTotal(totalNumber: number) {
    this.totalNotifications = totalNumber;
    this.comunicazioniNumberSource.next(this.totalNotifications);
  }

  /**
   * Rimuovi la notifica
   * @param idNotifica idNotifica da eliminare
   */
  removeNotifica(idNotifica: string) {
    const indexNotifica = this.comuArray.findIndex(n => n.idNotifica === idNotifica);
    if (indexNotifica !== -1) {
      this.comuArray.splice(indexNotifica, 1);
      this.comunicazioniSource.next(this.comuArray);
    }
  }

  reset() {
    this.comuArray = [];
  }
}
