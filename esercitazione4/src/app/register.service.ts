import {Injectable} from '@angular/core';
import {environment} from '../environments/environment';
import {LoginModel, RegisterModel} from './register/reg-interfaces';
import {HttpClient} from '@angular/common/http';
import * as moment from 'moment';
import {shareReplay, tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RegisterService {

  baseURL = environment.baseURL;

  constructor(private httpClient: HttpClient) {
  }

  register(model: RegisterModel) {
    return this.httpClient.post(this.baseURL + 'register', model);
  }

  login(model: LoginModel) {
    // We are calling shareReplay to prevent the receiver of this Observable from
    // accidentally triggering multiple POST requests due to multiple subscriptions.
    return this.httpClient.post(this.baseURL + 'login', model).pipe(tap(res => this.setSession(res)), shareReplay());
  }

  private setSession(authResult) {
    const expiresAt = moment().add(authResult.expiresIn, 'second');

    localStorage.setItem('id_token', authResult.idToken);
    localStorage.setItem('expires_at', JSON.stringify(expiresAt.valueOf()));
  }

  logout() {
    localStorage.removeItem('id_token');
    localStorage.removeItem('expires_at');
  }

  public isLoggedIn() {
    return moment().isBefore(this.getExpiration());
  }

  getExpiration() {
    const expiration = localStorage.getItem('expires_at');
    const expiresAt = JSON.parse(expiration);
    return moment(expiresAt + 3600000);
  }

}
