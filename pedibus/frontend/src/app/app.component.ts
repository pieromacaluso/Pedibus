import {Component, OnInit} from '@angular/core';
import {AuthService} from './auth/auth.service';
import {Router, RouterOutlet} from '@angular/router';
import {environment} from '../environments/environment';
import {HeaderService} from './utilities/header/header.service';
import {fadeAnimation} from './routes/route-animations';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  animations: [ // <-- add your animations here
    fadeAnimation
    // slider,
    // transformer,
    // stepper
  ]
})
export class AppComponent implements OnInit {

  opened: boolean;
  logo: any = '../../assets/svg/logo.svg';
  userLogged: boolean;

  constructor(private auth: AuthService, private router: Router, private header: HeaderService) {
    this.userLogged = this.auth.isLoggedIn();
    console.log('Main URL: ' + window.location.origin);
  }

  ngOnInit(): void {
    const timer = JSON.parse(localStorage.getItem('expires_at'));
    if (timer && (Date.now() > timer)) {
      this.auth.logout();
      this.router.navigate(['/sign-in']);
    }
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
      return !environment.production ? 'User logged: '
        + this.auth.isLoggedIn() + ', expiration: ' + this.auth.getExpiration().toLocaleString() : '';
    }
  }

  changeOfRoutes() {
    this.header.update();
  }

  prepareRoute(outlet: RouterOutlet) {
    return outlet && outlet.activatedRouteData;
  }

}
