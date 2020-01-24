import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {AnagraficaService, ElementType, RoleType} from '../anagrafica.service';
import {UserDTO} from '../dtos';
import {MatPaginator, PageEvent} from '@angular/material';
import {ChildrenDTO} from '../../genitore/dtos';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-tab',
  templateUrl: './tab.component.html',
  styleUrls: ['./tab.component.scss']
})
export class TabComponent implements OnInit, OnDestroy {

  @Input() page: any;

  @Input() type: ElementType;
  elements: any[];
  len: number;
  pageIndex: number;
  private updatesSub: Subscription;
  private keywordSub: Subscription;

  constructor(private anagraficaService: AnagraficaService) {
  }

  ngOnInit() {
    this.elements = this.page.content;
    this.len = this.page.totalElements;
    this.anagraficaService.resetKeyword();
    this.keywordSub = this.anagraficaService.getKeywordSub(this.type).subscribe((res) => {
        this.searchKeyword(res, 0);
      }, error => {
        // TODO: error management
      }
    );
    this.updatesSub = this.anagraficaService.watchUpdates().subscribe((res) => {
      this.updateEmit();
    });
  }

  cambiaPagina($event: PageEvent) {
    switch (this.type) {
      case ElementType.User:
        this.keywordSub.unsubscribe();
        this.keywordSub = this.anagraficaService.getKeywordSub(this.type).subscribe((res) => {
            this.searchKeyword(res, $event.pageIndex);
          }, error => {
            // TODO: error management
          }
        );
        break;
      case ElementType.Child:
        this.keywordSub.unsubscribe();
        this.keywordSub = this.anagraficaService.getKeywordSub(this.type).subscribe((res) => {
            this.searchKeyword(res, $event.pageIndex);
          }, error => {
            // TODO: error management
          }
        );
        break;

    }
  }

  updateEmit() {
    this.anagraficaService.updateEmit();
  }

  emitKeyword(keyword: string, paginator: MatPaginator) {
    this.anagraficaService.emitKeyword(keyword, this.type);
    paginator.firstPage();
  }

  searchKeyword(keyword: string, pageIndex: number) {
    switch (this.type) {
      case ElementType.User:
        this.anagraficaService.getUsers(pageIndex, keyword).subscribe((res) => {
          this.elements = res.content;
          this.len = res.totalElements;
        }, (error) => {
          // TODO: Errore cambio pagina
        });
        break;
      case ElementType.Child:
        this.anagraficaService.getChildren(pageIndex, keyword).subscribe((res) => {
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
          this.pageIndex = 0;
        }, (error) => {
          // TODO: Errore cambio pagina
        });
        break;
      case ElementType.Child:
        this.anagraficaService.getChildren(0, '').subscribe((res) => {
          this.elements = res.content;
          this.pageIndex = 0;
        }, (error) => {
          // TODO: Errore cambio pagina
        });
        break;

    }
  }

  ngOnDestroy() {
    this.keywordSub.unsubscribe();
    this.updatesSub.unsubscribe();
  }
}
