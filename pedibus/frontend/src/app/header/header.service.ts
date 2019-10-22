import {Injectable} from '@angular/core';
import {BehaviorSubject, Subject} from 'rxjs';
import {AuthService} from '../registration/auth.service';

@Injectable({
  providedIn: 'root'
})
export class HeaderService {

  // Observable string sources
  private menuSource = new BehaviorSubject<MenuItem[]>([]);

  // Observable string streams
  menuAnnounced$ = this.menuSource.asObservable();
  private presenze = {icon: 'people', link: 'presenze', title: 'Presenze'};
  private schedule = {icon: 'schedule', link: 'disponibilita', title: 'Disponibilità'};
  private notification = {icon: 'notification_important', link: 'comunicazioni', title: 'Comunicazioni'};
  private turni = {icon: 'verified_user', link: 'turni', title: 'Turni'};
  private signIn = {icon: 'vpn_key', link: 'sign-in', title: 'Sign In'};
  private signUp = {icon: 'person_add', link: 'sign-up', title: 'Sign Up'};


  constructor(private auth: AuthService) {
  }

  update() {
    const menu: MenuItem[] = [];
    if (this.auth.isLoggedIn()) {
      if (this.auth.getRoles().includes('ROLE_SYSTEM-ADMIN')) {
        menu.push(this.presenze, this.schedule, this.notification, this.turni);
      } else if (this.auth.getRoles().includes('ROLE_ADMIN')) {
        menu.push(this.presenze, this.schedule, this.notification);
      } else if (this.auth.getRoles().includes('ROLE_USER')) {
        menu.push(this.presenze);
      }
    } else {
      menu.push(this.signIn, this.signUp);
    }
    console.log(menu);

    this.menuSource.next(menu);

  }
}

export interface MenuItem {
  icon: string;
  link: string;
  title: string;
}
