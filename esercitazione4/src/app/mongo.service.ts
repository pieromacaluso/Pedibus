import {Injectable} from '@angular/core';
import {Prenotazione} from './prenotazione';

const LINEE = [
  'linea1', 'linea2'
];

const RESERVATIONS: Prenotazione = {
  alunniPerFermataAndata:
    [{
      fermata: {
        id: 1,
        nome: 'Bernini',
        orario: '07:20'
      },
      alunni: [
        'Martina'
      ]
    },
      {
        fermata: {
          id: 2,
          nome: 'Medici',
          orario: '07:30'
        },
        alunni: [
          'Cristiano'
        ]
      },
      {
        fermata: {
          id: 3,
          nome: 'Monte Grappa',
          orario: '07:40'
        },
        alunni: [
          'Beatrice'
        ]
      },
      {
        fermata: {
          id: 4,
          nome: 'Scuola',
          orario: '08:00'
        },
        alunni: [
          'Simone'
        ]
      }]
  ,
  alunniPerFermataRitorno:
    [{
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
    },
      {
        fermata: {
          id: 6,
          nome: 'Monte Grappa',
          orario: '13:30'
        },
        alunni: [
          'Carmelo',
          'Luca'
        ]
      },
      {
        fermata: {
          id: 7,
          nome: 'Racconigi',
          orario: '13:45'
        },
        alunni: [
          'Laura'
        ]
      },
      {
        fermata: {
          id: 8,
          nome: 'Bernini',
          orario: '14:00'
        },
        alunni: [
          'Vercinge'
        ]
      }]
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
