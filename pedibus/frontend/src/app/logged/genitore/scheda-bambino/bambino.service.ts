import { Injectable } from '@angular/core';
import { ApiService } from '../../api.service';
import { FermataDTO } from '../dtos';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BambinoService {

  constructor(private apiService: ApiService) { 

  }

  getFertmata(idFermata: number): Observable<FermataDTO> {
    return this.apiService.getFermata(idFermata);
  }
}
