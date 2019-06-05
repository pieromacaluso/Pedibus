import {Component, OnInit} from '@angular/core';
import {RegisterService} from '../register.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  links;
  activeLink;
  userLogged: boolean;

  constructor(private registerService: RegisterService, private router: Router) {
    this.userLogged = this.registerService.isLoggedIn();
    this.links = this.userLogged ? ['presenze'] : ['register', 'login'];
    this.activeLink = this.links[0];
  }

  ngOnInit() {
  }


  logOut() {
    this.userLogged = false;
    this.links = ['register', 'login'];
    this.registerService.logout();
    this.router.navigate(['/login']);
  }
}
