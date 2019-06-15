import {Component} from '@angular/core';
import {AuthService} from './registration/auth.service';
import {Router} from '@angular/router';
import {environment} from '../environments/environment';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  opened: boolean;
  logo: any = '../../assets/svg/logo.svg';
  userLogged: boolean;
  loggedLinksIcon = ['../../assets/svg/logo.svg'];
  loggedLinks = ['presenze'];
  notLoggedLinks = ['sign-in', 'sign-up'];
  activeLoggedLink;
  activeNotLoggedLink;

  constructor(private auth: AuthService, private router: Router) {
    this.userLogged = this.auth.isLoggedIn();
    this.activeLoggedLink = this.loggedLinks[0];
    this.activeNotLoggedLink = this.notLoggedLinks[0];
    console.log('Main URL: ' + window.location.origin);
  }

  isLoggedIn() {
    return this.auth.isLoggedIn();
  }

  logOut() {
    this.userLogged = false;
    this.auth.logout();
    this.router.navigate(['sign-in']);
  }

  getAuthData() {
    if (this.auth.getExpiration() == null) {
      return !environment.production ? 'User logged: ' + this.auth.isLoggedIn() + ', expiration: not defined' : '';
    } else {
      return !environment.production ? 'User logged: ' + this.auth.isLoggedIn() + ', expiration: ' + this.auth.getExpiration().toLocaleString() : '';
    }
  }
}
