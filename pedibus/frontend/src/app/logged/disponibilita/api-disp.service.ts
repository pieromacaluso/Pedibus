import {Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {DatePipe} from '@angular/common';
import {TurnoResource} from '../turni/api-turni.service';

@Injectable({
  providedIn: 'root'
})
export class ApiDispService {

  baseURL = environment.baseURL;

  constructor(private httpClient: HttpClient, private datePipe: DatePipe) {
  }

  /**
   * From Verso to Int
   * @param verso verso come stringa
   */
  static versoToInt(verso: string) {
    return verso === 'Andata' ? 1 : 0;
  }

  /**
   * Post Disponibilità
   * @param idLinea id Linea
   * @param idFermata id Fermata
   * @param verso verso
   * @param data data
   */
  postDisp(idLinea: string, idFermata: string, verso: string, data: Date) {
    // @PostMapping("/disp/{idLinea}/{verso}/{data}")
    const idVerso = ApiDispService.versoToInt(verso);
    return this.httpClient
      .post<DispAllResource>(this.baseURL + 'disp/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'), idFermata);
  }

  /**
   * Cancellazione Disponibilità
   * @param idLinea id Linea
   * @param idFermata id Fermata
   * @param verso verso
   * @param data data
   */
  delDisp(idLinea: string, idFermata: string, verso: string, data: Date) {
    // @PostMapping("/disp/{idLinea}/{verso}/{data}")
    const idVerso = ApiDispService.versoToInt(verso);
    return this.httpClient
      .delete(this.baseURL + 'disp/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'));
  }

  /**
   * Get della disponibilità
   * @param verso verso
   * @param data data
   */
  getDisp(verso: string, data: Date) {
    // @GetMapping("/disp/{verso}/{data}")
    const idVerso = ApiDispService.versoToInt(verso);
    return this.httpClient
      .get<DispTurnoResource>(this.baseURL + 'disp/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'));
  }

  /**
   * Ack della disponibilità
   * @param idLinea id linea
   * @param verso verso
   * @param data data
   */
  ackDisp(idLinea: string, verso: string, data: Date) {
    const idVerso = ApiDispService.versoToInt(verso);
    return this.httpClient
      .post(this.baseURL + 'turno/disp/ack/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'), ' ');
  }
}

export interface DispAllResource {
  id: string;
  guideUsername: string;
  idLinea: string;
  orario: string;
  nomeLinea: string;
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
