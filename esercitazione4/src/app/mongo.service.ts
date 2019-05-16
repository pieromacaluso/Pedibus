import {Injectable} from '@angular/core';
import {Prenotazione} from './prenotazione';

const LINEE = [
  'linea1', 'linea2'
];

const RESERVATIONS: Prenotazione = {
  alunniPerFermataAndata: [
    {
      fermata: {
        id: 1,
        nome: 'Bernini',
        orario: '07:20'
      },
      alunni: [
        'Martina'
      ]
    }
  ],
  alunniPerFermataRitorno: [
    {
      fermata: {
        id: 5,
        nome: 'Scuola',
        orario: '13:10'
      },
      alunni: [
        'Marco',
        'Angelo',
        'Piero'
      ]
    }
  ]
};


@Injectable({
  providedIn: 'root'
})
export class MongoService {

  constructor() {
  }

  getLinee() {
    return LINEE;
  }

  getReservation() {
    return RESERVATIONS;
  }


}
