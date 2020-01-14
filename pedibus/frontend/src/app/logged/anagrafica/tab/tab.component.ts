import {Component, Input, OnInit} from '@angular/core';
import {AnagraficaService, ElementType, RoleType} from '../anagrafica.service';
import {UserDTO} from '../dtos';
import {PageEvent} from '@angular/material';
import {ChildrenDTO} from '../../genitore/dtos';

@Component({
  selector: 'app-tab',
  templateUrl: './tab.component.html',
  styleUrls: ['./tab.component.scss']
})
export class TabComponent implements OnInit {

  @Input() page: any;

  @Input() type: ElementType;
  elements: any[];
  len: number;

  constructor(private anagraficaService: AnagraficaService) {
  }

  ngOnInit() {

    console.log(this.page);
    this.elements = this.page.content;
    this.len = this.page.totalElements;
  }

  cambiaPagina($event: PageEvent) {
    switch (this.type) {
      case ElementType.User:
        this.anagraficaService.getUsers($event.pageIndex, '').subscribe((res) => {
          this.elements = res.content;
        }, (error) => {
          // TODO: Errore cambio pagina
        });
        break;
      case ElementType.Child:
        this.anagraficaService.getChildren($event.pageIndex, '').subscribe((res) => {
          this.elements = res.content;
        }, (error) => {
          // TODO: Errore cambio pagina
        });
        break;

    }

  }

  searchKeyword(keyword: string) {
    switch (this.type) {
      case ElementType.User:
        this.anagraficaService.getUsers(0, keyword).subscribe((res) => {
          this.elements = res.content;
          this.len = res.totalElements;

        }, (error) => {
          // TODO: Errore cambio pagina
        });
        break;
      case ElementType.Child:
        this.anagraficaService.getChildren(0, keyword).subscribe((res) => {
          this.elements = res.content;
          this.len = res.totalElements;

        }, (error) => {
          // TODO: Errore cambio pagina
        });
        break;
    }
  }

  isUserDTO() {
    return this.type === ElementType.User;
  }

  isChildDTO() {
    return this.type === ElementType.Child;
  }

  resetValue(search: HTMLInputElement) {
    search.value = '';
    switch (this.type) {
      case ElementType.User:
        this.anagraficaService.getUsers(0, '').subscribe((res) => {
          this.elements = res.content;
        }, (error) => {
          // TODO: Errore cambio pagina
        });
        break;
      case ElementType.Child:
        this.anagraficaService.getChildren(0, '').subscribe((res) => {
          this.elements = res.content;
        }, (error) => {
          // TODO: Errore cambio pagina
        });
        break;

    }
  }
}
