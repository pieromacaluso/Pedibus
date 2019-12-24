import { Injectable } from '@angular/core';
import { ApiService } from '../../api.service';
import { FermataDTO } from '../dtos';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BambinoService {

  defaultAndata: Observable<FermataDTO>;
  defaultRitorno: Observable<FermataDTO>;

  constructor(private apiService: ApiService) {

  }

  getFertmata(idFermata: number, andata: boolean) {
    if (andata) {
      this.defaultAndata = this.apiService.getFermata(idFermata);
    }
    else {
      this.defaultRitorno = this.apiService.getFermata(idFermata);
    }
  }
}
