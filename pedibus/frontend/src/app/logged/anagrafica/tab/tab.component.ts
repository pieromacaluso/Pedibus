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
        // Gestito da Interceptor
      }
    );
    this.updatesSub = this.anagraficaService.watchUpdates().subscribe((res) => {
      this.updateEmit();
    });
  }

  /**
   * Cambio della pagina
   * @param $event evento
   */
  cambiaPagina($event: PageEvent) {
    switch (this.type) {
      case ElementType.User:
        this.keywordSub.unsubscribe();
        this.keywordSub = this.anagraficaService.getKeywordSub(this.type).subscribe((res) => {
            this.searchKeyword(res, $event.pageIndex);
          }, error => {
            // Gestito da Interceptor
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

  /**
   * Aggiornamento emit
   */
  updateEmit() {
    this.anagraficaService.updateEmit();
  }

  /**
   * Emit una nuova keyword
   * @param keyword keyword
   * @param paginator paginatore
   */
  emitKeyword(keyword: string, paginator: MatPaginator) {
    this.anagraficaService.emitKeyword(keyword, this.type);
    paginator.firstPage();
  }

  /**
   * Cerca parola chiave
   * @param keyword parola chiave
   * @param pageIndex pagina da ricercare
   */
  searchKeyword(keyword: string, pageIndex: number) {
    switch (this.type) {
      case ElementType.User:
        this.anagraficaService.getUsers(pageIndex, keyword).subscribe((res) => {
          this.elements = res.content;
          this.len = res.totalElements;
        }, (error) => {
          // Gestito da Interceptor
        });
        break;
      case ElementType.Child:
        this.anagraficaService.getChildren(pageIndex, keyword).subscribe((res) => {
          this.elements = res.content;
          this.len = res.totalElements;
        }, (error) => {
          // Gestito da Interceptor
        });
        break;
    }
  }

  /**
   * E' un userDTO
   */
  isUserDTO() {
    return this.type === ElementType.User;
  }

  /**
   * E' un ChildDTO?
   */
  isChildDTO() {
    return this.type === ElementType.Child;
  }

  /**
   * Resetta valori
   * @param search
   */
  resetValue(search: HTMLInputElement) {
    search.value = '';
    switch (this.type) {
      case ElementType.User:
        this.anagraficaService.emitKeyword('', this.type);
        this.anagraficaService.getUsers(0, '').subscribe((res) => {
          this.elements = res.content;
          this.pageIndex = 0;
        }, (error) => {
          // Gestito da Interceptor
        });
        break;
      case ElementType.Child:
        this.anagraficaService.emitKeyword('', this.type);
        this.anagraficaService.getChildren(0, '').subscribe((res) => {
          this.elements = res.content;
          this.pageIndex = 0;
        }, (error) => {
          // Gestito da Interceptor
        });
        break;

    }
  }

  ngOnDestroy() {
    this.keywordSub.unsubscribe();
    this.updatesSub.unsubscribe();
  }
}
