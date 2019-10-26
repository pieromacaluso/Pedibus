import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import { Notifica } from './logged/notifiche/dtos';

@Injectable({
  providedIn: 'root'
})
export class DataShareService {

  private comunicazioniSource = new BehaviorSubject<Notifica[]>([]);
  comunicazioni = this.comunicazioniSource.asObservable();

  constructor() { }

  updateNotifiche(notifiche: Notifica[]) {
    this.comunicazioniSource.next(notifiche);
  }
}
