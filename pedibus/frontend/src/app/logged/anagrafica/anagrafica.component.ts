import {Component, OnInit} from '@angular/core';
import {AnagraficaService, ElementType} from './anagrafica.service';
import {Observable} from 'rxjs';
import {UserDTO} from './dtos';



@Component({
  selector: 'app-anagrafica',
  templateUrl: './anagrafica.component.html',
  styleUrls: ['./anagrafica.component.css']
})
export class AnagraficaComponent implements OnInit {

  pageUser: Observable<any>;
  pageNumber: number;
  pageChildren: Observable<any>;

  constructor(private anagraficaService: AnagraficaService) {
    this.pageNumber = 0;
    this.pageUser = anagraficaService.getUsers(this.pageNumber, '');
    this.pageChildren = anagraficaService.getChildren(this.pageNumber, '');
  }

  ngOnInit() {
  }

}
