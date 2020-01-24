import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class MongoService {

  constructor(private httpClient: HttpClient) {

  }

  getLinee() {
    console.log('invio richiesta getLinee');
    return this.httpClient.get('http://localhost:8080/lines');
  }

  getPrenotazioneByLineaAndDateAndVerso(selectedLinea: string, date: Date) {
    console.log('invio richiesta getPrenotazioneByLineaAndDateAndVerso ' + date.toLocaleDateString());
    return  this.httpClient.get('http://localhost:8080/reservations/' + selectedLinea + '/2019-06-09');
  }
}
