import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {DatePipe} from '@angular/common';
import {Alunno, PrenotazioneRequest} from './line-details';
import {TurnoResource} from './api-turni.service';

@Injectable({
  providedIn: 'root'
})
export class ApiDispService {

  baseURL = environment.baseURL;

  constructor(private httpClient: HttpClient, private datePipe: DatePipe) {
  }

  static versoToInt(verso: string) {
    return verso === 'Andata' ? 1 : 0;
  }

  postDisp(idLinea: string, idFermata: string, verso: string, data: Date) {
    // @PostMapping("/disp/{idLinea}/{verso}/{data}")
    const idVerso = ApiDispService.versoToInt(verso);
    return this.httpClient
      .post<DispAllResource>(this.baseURL + 'disp/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'), idFermata);
  }

  delDisp(idLinea: string, idFermata: string, verso: string, data: Date) {
    // @PostMapping("/disp/{idLinea}/{verso}/{data}")
    const idVerso = ApiDispService.versoToInt(verso);
    return this.httpClient
      .delete(this.baseURL + 'disp/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'));
  }

  getDisp(idLinea: string, verso: string, data: Date) {
    // @GetMapping("/disp/{idLinea}/{verso}/{data}")
    const idVerso = ApiDispService.versoToInt(verso);
    return this.httpClient
      .get<DispTurnoResource>(this.baseURL + 'disp/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'));
  }

  ackDisp(idLinea: string, verso: string, data: Date) {
    const idVerso = ApiDispService.versoToInt(verso);
    return this.httpClient
      .post(this.baseURL + 'turno/disp/ack/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'), ' ');
  }
}

export interface DispAllResource {
  guideUsername: string;
  idFermata: number;
  nomeFermata: string;
  isConfirmed: boolean;
  isAck: boolean;
  add: boolean;
  delete: boolean;
  ack: boolean;
}

export interface DispTurnoResource {
  disp: DispAllResource;
  turno: TurnoResource;
}
