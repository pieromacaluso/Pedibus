import {Component, OnInit} from '@angular/core';
import {AnagraficaService} from './anagrafica.service';
import {Observable} from 'rxjs';
import {UserDTO} from './dtos';

@Component({
  selector: 'app-anagrafica',
  templateUrl: './anagrafica.component.html',
  styleUrls: ['./anagrafica.component.css']
})
export class AnagraficaComponent implements OnInit {

  page: Observable<any>;
  pageNumber: number;

  constructor(private anagraficaService: AnagraficaService) {
    this.pageNumber = 0;
    this.page = anagraficaService.getUsers(this.pageNumber, '');
  }

  ngOnInit() {
  }

}
