import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {SignInModel, SignUpModel} from './models';
import {shareReplay, tap} from 'rxjs/operators';
import * as moment from 'moment';
import * as jwt_decode from 'jwt-decode';
import {myRxStompConfig} from '../my-rx-stomp.config';
import {InjectableRxStompConfig, RxStompService} from '@stomp/ng2-stompjs';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  baseURL = environment.baseURL;
  private sessionSource = new BehaviorSubject<string>(null);
  newSession = this.sessionSource.asObservable();

  constructor(private httpClient: HttpClient, private rxStompService: RxStompService) {

    if (localStorage.getItem('id_token')) {
      if (!this.rxStompService.connected()) {
        const stompConfig: InjectableRxStompConfig = Object.assign({}, myRxStompConfig, {
          connectHeaders: {
            Authentication: localStorage.getItem('id_token')
          },
          beforeConnect: () => {
            console.log('%c called before connect', 'color: blue');
          }
        });
        this.rxStompService.configure(stompConfig);
      }
      console.log('SETSETTION', 'color:red');
      this.sessionSource.next(localStorage.getItem('id_token'));
    }
  }

  /* METODO CHE RINNOVA LE CREDENZIALI */
  postNewPassword(token: string, model) {
    return this.httpClient.post(this.baseURL + 'recover/' + token, model);
  }

  newUserPasswordChange(token: any, model: { password: string; passMatch: string; oldPassword: string }) {
    return this.httpClient.post(this.baseURL + 'new-user/' + token, model);

  }

  getConfirm(token: string) {
    return this.httpClient.get(this.baseURL + 'confirm/' + token);
  }

  postRecover(email: string) {
    return this.httpClient.post(this.baseURL + 'recover', email);
  }



  signUp(model: SignUpModel) {
    return this.httpClient.post(this.baseURL + 'register', model);
  }

  checkDuplicate(email: string) {
    return this.httpClient.get<boolean>(this.baseURL + 'register/checkMail/' + email);
  }

  signIn(model: SignInModel) {
    // We are calling shareReplay to prevent the receiver of this Observable from
    // accidentally triggering multiple POST requests due to multiple subscriptions.
    return this.httpClient.post(this.baseURL + 'login', model).pipe(tap(res => this.setSession(res)), shareReplay());
  }

  private setSession(authResult) {
    console.log('SETSETTION', 'color:red');
    console.log(JSON.stringify(jwt_decode(authResult.token)));
    const expiresAt = moment((jwt_decode(authResult.token).exp) * 1000);
    console.log('expires at: ' + expiresAt);
    console.log(JSON.stringify(jwt_decode(authResult.token)));
    localStorage.setItem('id_token', authResult.token);
    localStorage.setItem('roles', JSON.stringify(jwt_decode(authResult.token).roles));
    localStorage.setItem('expires_at', JSON.stringify(expiresAt.valueOf()));
    const stompConfig: InjectableRxStompConfig = Object.assign({}, myRxStompConfig, {
      connectHeaders: {
        Authentication: localStorage.getItem('id_token')
      },
      beforeConnect: () => {
        console.log('%c called before connect', 'color: blue');
      }
    });
    this.rxStompService.configure(stompConfig);
    this.sessionSource.next(localStorage.getItem('id_token'));
    if (!this.rxStompService.connected()) {
      this.rxStompService.activate();
    }
  }

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
        console.log('%c called before connect', 'color: blue');
      }
    });
    this.rxStompService.configure(stompConfig);
    this.sessionSource.next(null);
  }

  isAdmin() {
    if (this.isLoggedIn()) {
      const roles = JSON.parse(localStorage.getItem('roles'));
      return  roles.find(role => role === 'ROLE_ADMIN');
    }
    return false;
  }
  isGuide() {
    if (this.isLoggedIn()) {
      const roles = JSON.parse(localStorage.getItem('roles'));
      return roles.find(role => role === 'ROLE_GUIDE');
    }
    return false;
  }

  getUsername() {
    return jwt_decode(localStorage.getItem('id_token')).sub;
  }

  isUser() {
    if (this.isLoggedIn()) {
      const roles = this.getRoles();
      return roles.find(role => role === 'ROLE_USER');
    }
    return false;
  }

  getRoles(): string[] {
    const roles = jwt_decode(localStorage.getItem('id_token')).roles;
    return roles as string[];
  }

  public isLoggedIn() {
    if (this.getExpiration() == null) {
      return false;
    } else {
      return moment().isBefore(this.getExpiration());
    }
  }

  getExpiration() {
    const expiration = localStorage.getItem('expires_at');
    if (expiration != null) {
      const expiresAt = JSON.parse(expiration);
      return moment(expiresAt);
    } else {
      return null;
    }

  }

  getHome(): string {
    // TODO: rivedere home per ogni ruolo
    if (this.isUser()) {
      return 'genitore';
    }
    if (this.isGuide()) {
      return 'presenze';
    }
    if (this.isAdmin()) {
      return 'anagrafica';
    }
    if (this.isSysAdmin()) {
      return 'anagrafica';
    }
  }


  isSysAdmin() {
    if (this.isLoggedIn()) {
      const roles = this.getRoles();
      return roles.find(role => role === 'ROLE_SYSTEM-ADMIN');
    }
    return false;
  }
}
