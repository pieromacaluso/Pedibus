import {Injectable} from '@angular/core';
import {ChildrenDTO} from './dtos';
import {ApiService} from '../api.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GenitoreService {

  constructor(private apiService: ApiService) {
  }

  /**
   * Ottieni i tuoi bambini
   */
  getChildren(): Observable<ChildrenDTO[]> {
    return this.apiService.getChildren();
  }

  /**
   * Ottieni le linee
   */
  getLinee(): Observable<string[]> {
    return this.apiService.getLinee();
  }

}
