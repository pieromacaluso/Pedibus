import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {UserDTO} from './dtos';
import {ApiService} from '../api.service';

@Injectable({
  providedIn: 'root'
})
export class AnagraficaService {

  constructor(private apiService: ApiService) { }

  getUsers(pageNumber: number, keyword: string): Observable<any> {
    return this.apiService.getUsers(pageNumber, keyword);
  }
}
