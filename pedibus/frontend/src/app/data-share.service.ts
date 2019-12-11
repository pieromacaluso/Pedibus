import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {Notifica} from './logged/notifiche/dtos';

@Injectable({
  providedIn: 'root'
})
export class DataShareService {

  private comunicazioniSource = new BehaviorSubject<Notifica[]>([]);
  comunicazioni = this.comunicazioniSource.asObservable();
  comuArray: Notifica[] = [];

  constructor() {
  }

  updateNotifiche(notifiche: Notifica[]) {
    if (Array.isArray(notifiche)) {
      this.comuArray = notifiche.concat(this.comuArray);
    } else {
      this.comuArray.unshift(notifiche);
    }
    console.log('comu Array:', this.comuArray);
    this.comunicazioniSource.next(this.comuArray);
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
