import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {SignInModel, SignUpModel} from './models';
import {shareReplay, tap} from 'rxjs/operators';
import * as moment from 'moment';
import * as jwt_decode from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  baseURL = environment.baseURL;

  constructor(private httpClient: HttpClient) {
  }

  signUp(model: SignUpModel) {
    return this.httpClient.post(this.baseURL + 'register', model);
  }

  signIn(model: SignInModel) {
    // We are calling shareReplay to prevent the receiver of this Observable from
    // accidentally triggering multiple POST requests due to multiple subscriptions.
    return this.httpClient.post(this.baseURL + 'login', model).pipe(tap(res => this.setSession(res)), shareReplay());
  }

  private setSession(authResult) {
    console.log('exp: ' + jwt_decode(authResult.token).exp);
    const expiresAt = moment((jwt_decode(authResult.token).exp) * 1000);
    console.log('expires at: ' + expiresAt);

    localStorage.setItem('id_token', authResult.token);
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
    return moment(expiresAt);
  }

}
