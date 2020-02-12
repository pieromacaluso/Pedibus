import {Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {DatePipe} from '@angular/common';
import {DispAllResource} from '../disponibilita/api-disp.service';
import {first} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ApiTurniService {

  baseURL = environment.baseURL;

  constructor(private httpClient: HttpClient, private datePipe: DatePipe) {
  }

  /**
   * Da verso a int
   * @param verso verso
   */
  static versoToInt(verso: string) {
    return verso === 'Andata' ? 1 : 0;
  }

  /**
   * Ottieni il turno partendo dai dati
   * @param idLinea id della linea
   * @param verso verso
   * @param data data
   */
  getTurno(idLinea: string, verso: string, data: Date) {
    // @GetMapping("/turno/disp/{idLinea}/{verso}/{data}")
    const idVerso = ApiTurniService.versoToInt(verso);
    return this.httpClient
      .get<TurnoDispResource>(this.baseURL + 'turno/disp/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'));
  }

  /**
   * Ottieni lo stato del turno
   * @param idLinea id Linea
   * @param verso verso
   * @param data data
   */
  getTurnoState(idLinea: string, verso: string, data: Date) {
    // @GetMapping("/turno/state/{idLinea}/{verso}/{data}")
    const idVerso = ApiTurniService.versoToInt(verso);
    return this.httpClient
      .get<TurnoResource>(this.baseURL + 'turno/state/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'));
  }

  /**
   * Set lo stato del turno
   * @param idLinea id linea
   * @param verso verso
   * @param data data
   * @param b true se aperto, false altrimenti
   */
  setStateTurno(idLinea: string, verso: string, data: Date, b: boolean) {
    const idVerso = ApiTurniService.versoToInt(verso);
    return this.httpClient
      .put(this.baseURL + 'turno/state/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'), b ? 1 : 0);
  }

  /**
   * Conferma disponibilità
   * @param idLinea id della linea
   * @param verso verso
   * @param data data
   * @param disp struttura disponibilità
   */
  confirmDisp(idLinea: string, verso: string, data: Date, disp: DispAllResource) {
    const idVerso = ApiTurniService.versoToInt(verso);
    return this.httpClient
      .post(this.baseURL + 'turno/disp/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'), disp);
  }

  /**
   * Aggiorna disponibilità
   * @param id id della disponibilità
   * @param disp struttura disponibilità
   */
  updateDisp(id: string, disp: DispAllResource) {
    return this.httpClient
      .put(this.baseURL + 'admin/disp/' + id.toString(), disp);
  }

  /**
   * Cancella disponibilità
   * @param id id disponibilità da cancellare
   */
  deleteDisp(id: string) {
    return this.httpClient
      .delete(this.baseURL + 'admin/disp/' + id.toString()).pipe(first());
  }
}

export interface TurnoResource {
  idLinea: string;
  data: Date;
  verso: boolean;
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

