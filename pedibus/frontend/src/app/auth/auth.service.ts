import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {SignInModel, SignUpModel} from './models';
import {shareReplay, tap} from 'rxjs/operators';
import * as moment from 'moment';
import * as jwt_decode from 'jwt-decode';
import {myRxStompConfig} from '../configuration/my-rx-stomp.config';
import {InjectableRxStompConfig, RxStompService} from '@stomp/ng2-stompjs';
import {BehaviorSubject, Observable} from 'rxjs';
import {DeprecatedCommand} from '@angular/cli/commands/deprecated-impl';

@Injectable({
  providedIn: 'root'
})
/**
 * Authentication Service:
 * Servizio per la gestione dell'autenticazione degli utenti in Angular
 */
export class AuthService {

  baseURL = environment.baseURL;
  private sessionSource = new BehaviorSubject<string>('');
  newSession = this.sessionSource.asObservable();

  /**
   * Costruttore Authentication Service
   * Controlla se presente il token e in caso positivo inizializza web socket e inserisce nella subject il token presente. In caso
   * contrario non fa nulla.
   *
   * @param httpClient HttpClient
   * @param rxStompService RxStomp Service
   */
  constructor(private httpClient: HttpClient, private rxStompService: RxStompService) {
    if (localStorage.getItem('id_token')) {
      if (!this.rxStompService.connected()) {
        const stompConfig: InjectableRxStompConfig = Object.assign({}, myRxStompConfig, {
          connectHeaders: {
            Authentication: localStorage.getItem('id_token')
          },
          beforeConnect: () => {
          }
        });
        this.rxStompService.configure(stompConfig);
      }
      this.sessionSource.next(localStorage.getItem('id_token'));
    }
  }

  /**
   * Rinnovo delle credenziali usato dalla pagine di recover
   *
   * @param token token di recupero password
   * @param model modello da usare per la richiesta
   */
  postNewPassword(token: string, model) {
    return this.httpClient.post(this.baseURL + 'recover/' + token, model);
  }

  /**
   * Rinnovo credenziali per un nuovo utente
   *
   * @param token token nuovo utente
   * @param model modello da usare per la richiesta
   */
  newUserPasswordChange(token: any, model: { password: string; passMatch: string }) {
    return this.httpClient.post(this.baseURL + 'new-user/' + token, model);
  }

  /**
   * Richiesta per conferma token attivazione account
   * @deprecated non esiste più il flusso di conferma
   * @param token token conferma
   */
  getConfirm(token: string) {
    return this.httpClient.get(this.baseURL + 'confirm/' + token);
  }

  /**
   * Post richiesta di recupero password
   * @param email email per cui richiedere il recupero password
   */
  postRecover(email: string) {
    return this.httpClient.post(this.baseURL + 'recover', email);
  }

  /**
   * Metodo per eseguire la registrazione all'account
   * @deprecated non esiste più il flusso di registrazione
   * @param model modello da usare per la richiesta `SignUpModel`
   */
  signUp(model: SignUpModel) {
    return this.httpClient.post(this.baseURL + 'register', model);
  }

  /**
   * @deprecated
   * Funzione utilizzata nel form di registrazione per verificare in tempo reale la presenza di un account con la mail specificata
   *
   * @param email email da usare per l'iscrizione
   */
  checkDuplicate(email: string) {
    return this.httpClient.get<boolean>(this.baseURL + 'register/checkMail/' + email);
  }

  /**
   * Login al sistema
   *
   * @param model modello richiesta `SignInModel`
   */
  signIn(model: SignInModel) {
    return this.httpClient.post(this.baseURL + 'login', model).pipe(tap(res => this.setSession(res)), shareReplay());
  }

  /**
   * Setup della sessione corrente, configurazione WebSocket e JWT nello storage
   *
   * @param authResult risultati di autenticazione che contengono JWT token
   */
  private setSession(authResult) {
    const expiresAt = moment((jwt_decode(authResult.token).exp) * 1000);
    localStorage.setItem('id_token', authResult.token);
    localStorage.setItem('roles', JSON.stringify(jwt_decode(authResult.token).roles));
    localStorage.setItem('expires_at', JSON.stringify(expiresAt.valueOf()));
    const stompConfig: InjectableRxStompConfig = Object.assign({}, myRxStompConfig, {
      connectHeaders: {
        Authentication: localStorage.getItem('id_token')
      },
      beforeConnect: () => {
      }
    });
    this.rxStompService.configure(stompConfig);
    this.sessionSource.next(localStorage.getItem('id_token'));
    if (!this.rxStompService.connected()) {
      this.rxStompService.activate();
    }
  }

  /**
   * Funzione di logout. Vengono eliminati i dati sul JWT e viene disattivato il servizio di websocket
   */
  logout() {
    localStorage.removeItem('id_token');
    localStorage.removeItem('roles');
    localStorage.removeItem('expires_at');
    this.rxStompService.deactivate();
    const stompConfig: InjectableRxStompConfig = Object.assign({}, myRxStompConfig, {
      connectHeaders: {
        Authentication: ''
      },
      beforeConnect: () => {
      }
    });
    this.rxStompService.configure(stompConfig);
    this.sessionSource.next('');
  }

  /**
   * Controlla se l'utente attualmente loggato è un ADMIN DI LINEA
   */
  isAdmin() {
    if (this.isLoggedIn()) {
      const roles = JSON.parse(localStorage.getItem('roles'));
      return roles.find(role => role === 'ROLE_ADMIN');
    }
    return false;
  }

  /**
   * Controlla se l'utente attualmente loggato è una GUIDA
   */
  isGuide() {
    if (this.isLoggedIn()) {
      const roles = JSON.parse(localStorage.getItem('roles'));
      return roles.find(role => role === 'ROLE_GUIDE');
    }
    return false;
  }

  /**
   * Controlla se l'utente attualmente loggato è un utente semplice GENITORE
   */
  isUser() {
    if (this.isLoggedIn()) {
      const roles = this.getRoles();
      return roles.find(role => role === 'ROLE_USER');
    }
    return false;
  }

  /**
   * Ottieni i ruoli dell'utente loggato dal JWT nello storage
   */
  getRoles(): string[] {
    const roles = jwt_decode(localStorage.getItem('id_token')).roles;
    return roles as string[];
  }

  /**
   * Ottieni lo username dell'utente loggato dal JWT nello storage
   */
  getUsername() {
    return jwt_decode(localStorage.getItem('id_token')).sub;
  }

  /**
   * Controlla se l'utente attualmente loggato è un utente
   */
  isSysAdmin() {
    if (this.isLoggedIn()) {
      const roles = this.getRoles();
      return roles.find(role => role === 'ROLE_SYSTEM-ADMIN');
    }
    return false;
  }

  /**
   * Controllo se l'utente è loggato o meno
   */
  public isLoggedIn() {
    if (this.getExpiration() == null) {
      return false;
    } else {
      return moment().isBefore(this.getExpiration());
    }
  }

  /**
   * Ritorno data di scadenza del JWT token
   */
  getExpiration() {
    const expiration = localStorage.getItem('expires_at');
    if (expiration != null) {
      const expiresAt = JSON.parse(expiration);
      return moment(expiresAt);
    } else {
      return null;
    }
  }

  /**
   * Ottieni la stringa che riguarda la home per un determinato utente
   */
  getHome(): string {
    if (this.isUser()) {
      return 'genitore';
    }
    if (this.isGuide()) {
      return 'presenze';
    }
    if (this.isAdmin()) {
      return 'presenze';
    }
    if (this.isSysAdmin()) {
      return 'anagrafica';
    }
    return 'genitore';
  }
}
