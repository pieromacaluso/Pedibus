import {Component, OnInit} from '@angular/core';
import {AuthService} from '../registration/auth.service';
import {Router} from '@angular/router';
import {HeaderService, MenuItem} from './header.service';
import {Observable} from 'rxjs';
import {Notifica} from '../logged/notifiche/dtos';
import {DataShareService} from '../data-share.service';

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
  unReadNotification$: Observable<Notifica[]>;

  constructor(private auth: AuthService, private router: Router, private header: HeaderService,
              private dataShareService: DataShareService) {
    this.userLogged = this.auth.isLoggedIn();
    this.headerMenu$ = this.header.menuAnnounced$;
    this.unReadNotification$ = this.dataShareService.comunicazioni;
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
