import {Injectable} from '@angular/core';
import {BehaviorSubject, Subject} from 'rxjs';
import {AuthService} from '../../auth/auth.service';
import {NotificheService} from '../../logged/notifiche/notifiche.service';

@Injectable({
  providedIn: 'root'
})
export class HeaderService {

  // Observable string sources
  private menuSource = new BehaviorSubject<MenuItem[]>([]);

  // Observable string streams
  menuAnnounced$ = this.menuSource.asObservable();
  private anagrafica = {icon: 'person_add', link: 'anagrafica', title: 'Anagrafica'};
  private genitori = {icon: 'supervisor_account', link: 'genitore', title: 'Genitore'};
  private presenze = {icon: 'people', link: 'presenze', title: 'Presenze'};
  private schedule = {icon: 'schedule', link: 'disponibilita', title: 'Disponibilità'};
  private notification = {icon: 'notification_important', link: 'notifiche', title: 'Comunicazioni'};
  private turni = {icon: 'verified_user', link: 'turni', title: 'Turni'};
  private signIn = {icon: 'vpn_key', link: 'sign-in', title: 'Sign In'};
  private signUp = {icon: 'person_add', link: 'sign-up', title: 'Sign Up'};
  private lineAdmin = {icon: 'directions_bus', link: 'line-admin', title: 'Permessi'};


  constructor(private auth: AuthService) {
  }

  /**
   * Aggiorna le schermate in base al ruolo
   */
  update() {
    const menu: MenuItem[] = [];
    if (this.auth.isLoggedIn()) {
      if (this.auth.getRoles().includes('ROLE_SYSTEM-ADMIN')) {
        menu.push(this.anagrafica, this.presenze, this.turni, this.lineAdmin);
      } else {
        if (this.auth.getRoles().includes('ROLE_GUIDE') || this.auth.getRoles().includes('ROLE_ADMIN')) {
          menu.push(this.presenze);
        }
        if (this.auth.getRoles().includes('ROLE_ADMIN')) {
          menu.push(this.turni, this.lineAdmin);
        }
        if (this.auth.getRoles().includes('ROLE_GUIDE')) {
          menu.push(this.schedule);
        }
        if (this.auth.getRoles().includes('ROLE_USER')) {
          menu.push(this.genitori);
        }
      }
      menu.push(this.notification);
    } else {
      menu.push(this.signIn);
    }
    this.menuSource.next(menu);
  }
}

export interface MenuItem {
  icon: string;
  link: string;
  title: string;
}
