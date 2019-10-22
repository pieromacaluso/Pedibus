import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {DatePipe} from '@angular/common';
import {Alunno, PrenotazioneRequest} from './line-details';

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
      .post(this.baseURL + '/disp/' + idLinea + '/' + idVerso + '/' + this.datePipe
        .transform(data, 'yyyy-MM-dd'), idFermata);
  }
}
