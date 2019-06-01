import {Injectable} from '@angular/core';
import {environment} from '../environments/environment';
import {RegisterModel} from './register/reg-interfaces';
import {HttpClient} from '@angular/common/http';

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
}
