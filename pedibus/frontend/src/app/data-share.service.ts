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

  updateNotifiche(notifiche: Notifica[]) {
    this.comuArray = notifiche;
    this.comunicazioniSource.next(this.comuArray);
  }

  updateTotal(totalNumber: number) {
    this.totalNotifications = totalNumber;
    this.comunicazioniNumberSource.next(this.totalNotifications);
  }

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
