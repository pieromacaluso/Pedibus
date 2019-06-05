import {Component} from '@angular/core';
import {AuthService} from './registration/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  userLogged: boolean;
  links;
  activeLink;

  constructor(private auth: AuthService) {
    this.links = ['sign-up', 'sign-in', 'presenze'];
    this.activeLink = this.links[0];
  }

  logOut() {
    this.auth.logout();
  }

}
