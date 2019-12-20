import { Injectable } from '@angular/core';
import { ChildrenDTO } from './dtos';
import { ApiService } from '../api.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GenitoreService {
  
  constructor(private apiService: ApiService) { }

  getChildren(): Observable<ChildrenDTO[]> {
    return this.apiService.getChildren();
  }

  getLinee(): Observable<string[]> {
    throw this.apiService.getLinee();
  }

}
