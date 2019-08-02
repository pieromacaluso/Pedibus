import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DataShareService {

  private comunicazioniSource = new BehaviorSubject<string[]>([""]);
  comunicazioni = this.comunicazioniSource.asObservable();

  constructor() { }

  updateNotifiche(notifiche: string[]) {
    this.comunicazioniSource.next(notifiche);
  }
}
