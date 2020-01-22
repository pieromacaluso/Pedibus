import {Component, OnInit} from '@angular/core';
import {AuthService} from '../registration/auth.service';
import {Router} from '@angular/router';
import {HeaderService, MenuItem} from './header.service';
import {Observable} from 'rxjs';
import {DataShareService} from '../data-share.service';
import {NotificheService} from '../logged/notifiche/notifiche.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  opened: boolean;
  logo: any = '../../assets/svg/logo.svg';
  userLogged: boolean;
  activeLoggedLink: any;
  headerMenu$: Observable<MenuItem[]>;
  unReadNotification$: Observable<number>;

  constructor(private auth: AuthService, private router: Router, private header: HeaderService,
              private dataShareService: DataShareService, private notificheService: NotificheService) {
    this.userLogged = this.auth.isLoggedIn();
    this.headerMenu$ = this.header.menuAnnounced$;
    this.unReadNotification$ = this.dataShareService.comunicazioniTotal;
  }

  isLoggedIn() {
    return this.auth.isLoggedIn();
  }

  logOut() {
    this.userLogged = false;
    this.auth.logout();
    this.header.update();
    this.router.navigate(['sign-in']);
  }

  getAuthData() {
    if (this.auth.getExpiration() == null) {
      return 'User logged: ' + this.auth.isLoggedIn() + ', expiration: not defined';
    } else {

      return 'User logged: ' + this.auth.isLoggedIn() + ', expiration: ' + this.auth.getExpiration().toLocaleString();
    }
  }

  ngOnInit() {
  }

}
