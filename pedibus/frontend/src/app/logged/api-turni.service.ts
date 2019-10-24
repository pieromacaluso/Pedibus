import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {DatePipe} from '@angular/common';
import {DispAllResource} from './api-disp.service';

@Injectable({
  providedIn: 'root'
})
export class ApiTurniService {

  baseURL = environment.baseURL;

  constructor(private httpClient: HttpClient, private datePipe: DatePipe) {
  }

  static versoToInt(verso: string) {
    return verso === 'Andata' ? 1 : 0;
  }

  getTurno(idLinea: string, verso: string, data: Date) {
    // @PostMapping("/disp/{idLinea}/{verso}/{data}")
    const idVerso = ApiTurniService.versoToInt(verso);
    return this.httpClient
      .get<TurnoDispResource>(this.baseURL + 'turno/disp/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'));
  }

  setStateTurno(idLinea: string, verso: string, data: Date, b: boolean) {
    const idVerso = ApiTurniService.versoToInt(verso);
    return this.httpClient
      .put(this.baseURL + 'turno/state/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'), b ? 1 : 0);
  }
}

export interface TurnoResource {
  isOpen: boolean;
  isExpired: boolean;
  opening: boolean;
  closing: boolean;
}

export interface TurnoDispResource {
  turno: TurnoResource;
  listDisp: MapDisp;
}

export interface MapDisp {
  [index: string]: DispAllResource[];

}

