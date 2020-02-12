import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';


/**
 * Pagina non trovata
 */
@Component({
  selector: 'app-page-not-found',
  templateUrl: './page-not-found.component.html',
  styleUrls: ['./page-not-found.component.scss']
})
export class PageNotFoundComponent implements OnInit {

  error404logo: any = '../../assets/svg/error404.svg';

  constructor(private router: Router) {
  }

  ngOnInit() {
  }

  home() {
    this.router.navigate(['sign-in']);
  }

}
