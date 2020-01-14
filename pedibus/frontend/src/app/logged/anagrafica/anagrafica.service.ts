import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {UserDTO} from './dtos';
import {ApiService} from '../api.service';

export enum ElementType {
  User,
  Child
}

export enum RoleType {
  User = 'ROLE_USER',
  Guide = 'ROLE_GUIDE',
  Admin = 'ROLE_ADMIN',
  Sys = 'ROLE_SYS-ADMIN'
}

@Injectable({
  providedIn: 'root'
})
export class AnagraficaService {


  constructor(private apiService: ApiService) {
  }

  getUsers(pageNumber: number, keyword: string): Observable<any> {
    return this.apiService.getUsers(pageNumber, keyword);
  }

  getChildren(pageNumber: number, s: string) {
    return this.apiService.getAllChildren(pageNumber, s);
  }

  getChild(child: any) {
    return this.apiService.getChild(child);
  }
}
