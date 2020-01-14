import {Component, Input, OnInit} from '@angular/core';
import {AnagraficaService} from '../anagrafica.service';
import {UserDTO} from '../dtos';
import {PageEvent} from '@angular/material';

@Component({
  selector: 'app-tab',
  templateUrl: './tab.component.html',
  styleUrls: ['./tab.component.css']
})
export class TabComponent implements OnInit {

  @Input() page: any;
  elements: UserDTO[];
  len: number;
  pageEvent: PageEvent;

  constructor(private anagraficaService: AnagraficaService) {
  }

  ngOnInit() {
    console.log(this.page);
    this.elements = this.page.content;
    this.len = this.page.totalElements;
  }

  cambiaPagina($event: PageEvent) {
    this.anagraficaService.getUsers($event.pageIndex, '').subscribe((res) => {
      this.elements = res.content;
    }, (error) => {
      // TODO: Errore cambio pagina
    });
  }

  searchKeyword(keyword: string) {
    this.anagraficaService.getUsers(0, keyword).subscribe((res) => {
      this.elements = res.content;
      this.len = res.totalElements;

    }, (error) => {
      // TODO: Errore cambio pagina
    });
  }
}
