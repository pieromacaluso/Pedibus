import {Injectable} from '@angular/core';
import {BehaviorSubject, forkJoin, Observable, Subscription} from 'rxjs';
import {UserDTO} from './dtos';
import {ApiService} from '../api.service';
import {Notifica} from '../notifiche/dtos';
import {debounceTime, first, flatMap, map, switchMap} from 'rxjs/operators';
import {MatDialog} from '@angular/material';
import {ChildDialogComponent} from './child-dialog/child-dialog.component';
import {ChildrenDTO} from '../genitore/dtos';
import {UserDialogComponent} from './user-dialog/user-dialog.component';
import {Fermata, StopsByLine} from '../line-details';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {RxStompService} from '@stomp/ng2-stompjs';
import {ConfirmationDialogComponent} from '../../utilities/confimation-dialog/confirmation-dialog.component';

export enum ElementType {
  User,
  Child
}

export enum RoleType {
  User = 'ROLE_USER',
  Guide = 'ROLE_GUIDE',
  Admin = 'ROLE_ADMIN',
  Sys = 'ROLE_SYSTEM-ADMIN'
}

@Injectable({
  providedIn: 'root'
})
export class AnagraficaService {

  private keywordUserSource = new BehaviorSubject<string>('');
  private keywordChildSource = new BehaviorSubject<string>('');
  keywordUser = this.keywordUserSource.asObservable();
  keywordChild = this.keywordChildSource.asObservable();

  baseURL = environment.baseURL;


  constructor(private apiService: ApiService, public dialog: MatDialog, private httpClient: HttpClient, private rxStompService: RxStompService) {
  }

  /**
   * Emetti una keyword
   * @param keyword keyword da emettere
   * @param e per utente o per bambino
   */
  emitKeyword(keyword: string, e: ElementType) {
    switch (e) {
      case ElementType.User:
        this.keywordUserSource.next(keyword);
        break;
      case ElementType.Child:
        this.keywordChildSource.next(keyword);
        break;
    }
  }

  /**
   * Aggiorna le emissioni
   */
  updateEmit() {
    this.keywordUserSource.next(this.keywordUserSource.value);
    this.keywordChildSource.next(this.keywordChildSource.value);
  }

  /**
   * Ottieni la sottoscrizione alle keyword partendo dal tipo
   * @param e tipo delle keyword
   */
  getKeywordSub(e: ElementType) {
    switch (e) {
      case ElementType.User:
        return this.keywordUser.pipe(debounceTime(500));
      case ElementType.Child:
        return this.keywordChild.pipe(debounceTime(500));
    }
  }

  /**
   * Ottieni gli utenti paginati e filtrati per keyword
   * @param pageNumber numero di pagina
   * @param keyword keyword
   */
  getUsers(pageNumber: number, keyword: string): Observable<any> {
    return this.apiService.getUsers(pageNumber, keyword);
  }

  /**
   * Ottieni i bambini paginati e filtrati per keyword
   * @param pageNumber numero di pagina
   * @param s keyword
   */
  getChildren(pageNumber: number, s: string) {
    return this.apiService.getAllChildren(pageNumber, s);
  }

  /**
   * Ottieni i dati di un bambino
   * @param child codice Fiscale
   */
  getChild(child: any) {
    return this.apiService.getChild(child);
  }

  /**
   * Cancella il bambino
   * @param codiceFiscale codice fiscale del bambino
   */
  deleteChild(codiceFiscale: string) {
    return this.apiService.deleteChild(codiceFiscale);
  }

  /**
   * Cancella utente
   * @param userId email dell'utente
   */
  deleteUser(userId: string) {
    return this.apiService.deleteUser(userId);
  }

  /**
   * Apri dialog Child
   * @param child struttura bambino
   */
  openDialogChild(child: ChildrenDTO): void {
    let dialogRef;
    if (child) {
      dialogRef = this.dialog.open(ChildDialogComponent, {
        data: {child}
      });
    } else {
      dialogRef = this.dialog.open(ChildDialogComponent, {});
    }

    dialogRef.afterClosed().subscribe(result => {
    });
  }

  /**
   * Apri dialog user
   * @param user struttura user
   */
  openDialogUser(user: UserDTO): void {
    let dialogRef;
    if (user) {
      dialogRef = this.dialog.open(UserDialogComponent, {
        data: {user}
      });
    } else {
      dialogRef = this.dialog.open(UserDialogComponent, {});
    }

    dialogRef.afterClosed().subscribe(result => {
    });
  }

  /**
   * Apri finestra di conferma indicando il messaggio
   * @param message messaggio
   */
  openConfirmationDialog(message: string) {
    let dialogRef;
    dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {message}
    });

    return dialogRef.afterClosed();
  }


  /**
   * Websocket per aggiornamenti anagrafica
   */
  watchUpdates() {
    return this.rxStompService.watch('/user/anagrafica');
  }

  /**
   * Resetta le keyword
   */
  resetKeyword() {
    this.keywordUserSource.next('');
    this.keywordChildSource.next('');
  }

  /**
   * Cambia amministratore
   * @param user struttura utente
   * @param idLinea id linea
   * @param addOrDel true se aggiungo, false se tolgo privilegio
   */
  changeAdmin(user: UserDTO, idLinea: string, addOrDel: boolean) {
    const bodyReq = {
      idLinea,
      addOrDel
    };
    return this.httpClient.put<void>(this.baseURL + '/admin/users/' + user.userId, bodyReq).pipe(first());

  }

}
