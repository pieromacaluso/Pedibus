import {Component} from '@angular/core';
import {AuthService} from './registration/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  userLogged: boolean;
  loggedLinks = ['presenze'];
  notLoggedLinks = ['sign-in', 'sign-up'];
  activeLoggedLink;
  activeNotLoggedLink;

  constructor(private auth: AuthService, private router: Router) {
    this.userLogged = this.auth.isLoggedIn();
    this.activeLoggedLink = this.loggedLinks[0];
    this.activeNotLoggedLink = this.notLoggedLinks[0];
  }

  isLoggedIn() {
    return this.auth.isLoggedIn();
  }

  logOut() {
    this.userLogged = false;
    this.auth.logout();
    this.router.navigate(['sign-in']);
  }

}
