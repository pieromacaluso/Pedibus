import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  links = ['register', 'login', 'presenze'];
  activeLink = this.links[0];

  constructor() { }

  ngOnInit() {
  }


}
