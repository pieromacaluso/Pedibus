import {Injectable} from '@angular/core';
import {Linea, LineDetails} from './lineDetails';

const LINEE: Linea[] = [
  {
    id: 1,
    name: 'Linea 1'
  },
  {
    id: 2,
    name: 'Linea 2'
  }
];

const RESERVATIONS: LineDetails[] = [{
  line_id: 1,
  dates: [
    {
      date: new Date(),
      alunniPerFermataAndata:
        [{
          fermata: {
            id: 1,
            nome: 'Bernini',
            orario: '07:20'
          },
          alunni: [
            {
              name: 'Giovanni',
              presenza: false
            }
          ]
        },
          {
            fermata: {
              id: 2,
              nome: 'Medici',
              orario: '07:30'
            },
            alunni: []
          },
          {
            fermata: {
              id: 3,
              nome: 'Monte Grappa',
              orario: '07:40'
            },
            alunni: [
              {
                name: 'Beatrice',
                presenza: false
              }
            ]
          },
          {
            fermata: {
              id: 4,
              nome: 'Scuola',
              orario: '08:00'
            },
            alunni: [
              {
                name: 'Simone',
                presenza: false
              }
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
            {
              name: 'Marco',
              presenza: false
            },
            {
              name: 'Angelo',
              presenza: false
            },
            {
              name: 'Piero',
              presenza: false
            }
          ]
        },
          {
            fermata: {
              id: 6,
              nome: 'Monte Grappa',
              orario: '13:30'
            },
            alunni: [
              {
                name: 'Carmelo',
                presenza: false
              },
              {
                name: 'Luca',
                presenza: false
              }
            ]
          },
          {
            fermata: {
              id: 7,
              nome: 'Racconigi',
              orario: '13:45'
            },
            alunni: [
              {
                name: 'Laura',
                presenza: false
              }
            ]
          },
          {
            fermata: {
              id: 8,
              nome: 'Bernini',
              orario: '14:00'
            },
            alunni: [
              {
                name: 'Vercinge',
                presenza: false
              },
              {
                name: 'Martina',
                presenza: false
              }
            ]
          }]
    },
    {
      date: new Date(Date.now() + 24 * 60 * 60 * 1000),
      alunniPerFermataAndata:
        [{
          fermata: {
            id: 1,
            nome: 'Bernini',
            orario: '07:20'
          },
          alunni: []
        },
          {
            fermata: {
              id: 2,
              nome: 'Medici',
              orario: '07:30'
            },
            alunni: [
              {
                name: 'Martina',
                presenza: false
              }
            ]
          },
          {
            fermata: {
              id: 3,
              nome: 'Monte Grappa',
              orario: '07:40'
            },
            alunni: [
              {
                name: 'Giorgia',
                presenza: false
              }
            ]
          },
          {
            fermata: {
              id: 4,
              nome: 'Scuola',
              orario: '08:00'
            },
            alunni: [
              {
                name: 'Simone',
                presenza: false
              }
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
            {
              name: 'Marco',
              presenza: false
            },
            {
              name: 'Angelo',
              presenza: false
            },
            {
              name: 'Peppino',
              presenza: false
            }
          ]
        },
          {
            fermata: {
              id: 6,
              nome: 'Monte Grappa',
              orario: '13:30'
            },
            alunni: [
              {
                name: 'Carmelo',
                presenza: false
              },
              {
                name: 'Luca',
                presenza: false
              }
            ]
          },
          {
            fermata: {
              id: 7,
              nome: 'Racconigi',
              orario: '13:45'
            },
            alunni: [
              {
                name: 'Laura',
                presenza: false
              }
            ]
          },
          {
            fermata: {
              id: 8,
              nome: 'Bernini',
              orario: '14:00'
            },
            alunni: [
              {
                name: 'Vercinge',
                presenza: false
              },
              {
                name: 'Martina',
                presenza: false
              }
            ]
          }]
    }
  ]
},
  {
    line_id: 2,
    dates: [
      {
        date: new Date(),
        alunniPerFermataAndata:
          [{
            fermata: {
              id: 1,
              nome: 'Bernini',
              orario: '07:20'
            },
            alunni: [
              {
                name: 'Beppe',
                presenza: false
              }
            ]
          },
            {
              fermata: {
                id: 2,
                nome: 'Medici',
                orario: '07:30'
              },
              alunni: []
            },
            {
              fermata: {
                id: 3,
                nome: 'Monte Grappa',
                orario: '07:40'
              },
              alunni: [
                {
                  name: 'Beatrice',
                  presenza: false
                }
              ]
            },
            {
              fermata: {
                id: 4,
                nome: 'Scuola',
                orario: '08:00'
              },
              alunni: [
                {
                  name: 'Simone',
                  presenza: false
                }
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
              {
                name: 'Marco',
                presenza: false
              },
              {
                name: 'Angelo',
                presenza: false
              },
              {
                name: 'Ludovico',
                presenza: false
              }
            ]
          },
            {
              fermata: {
                id: 6,
                nome: 'Monte Grappa',
                orario: '13:30'
              },
              alunni: [
                {
                  name: 'Carmelo',
                  presenza: false
                },
                {
                  name: 'Luca',
                  presenza: false
                }
              ]
            },
            {
              fermata: {
                id: 7,
                nome: 'Racconigi',
                orario: '13:45'
              },
              alunni: [
                {
                  name: 'Laura',
                  presenza: false
                }
              ]
            },
            {
              fermata: {
                id: 8,
                nome: 'Bernini',
                orario: '14:00'
              },
              alunni: [
                {
                  name: 'Vercinge',
                  presenza: false
                },
                {
                  name: 'Martina',
                  presenza: false
                }
              ]
            }]
      },
      {
        date: new Date(Date.now() + 24 * 60 * 60 * 1000),
        alunniPerFermataAndata:
          [{
            fermata: {
              id: 1,
              nome: 'Bernini',
              orario: '07:20'
            },
            alunni: [
              {
                name: 'Gianvito',
                presenza: false
              }
            ]
          },
            {
              fermata: {
                id: 2,
                nome: 'Medici',
                orario: '07:30'
              },
              alunni: [
                {
                  name: 'Martina',
                  presenza: false
                }
              ]
            },
            {
              fermata: {
                id: 3,
                nome: 'Monte Grappa',
                orario: '07:40'
              },
              alunni: [
                {
                  name: 'Giorgia',
                  presenza: false
                }
              ]
            },
            {
              fermata: {
                id: 4,
                nome: 'Scuola',
                orario: '08:00'
              },
              alunni: [
                {
                  name: 'Simone',
                  presenza: false
                }
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
              {
                name: 'Romario',
                presenza: false
              },
              {
                name: 'Marco',
                presenza: false
              },
              {
                name: 'Angelo',
                presenza: false
              },
              {
                name: 'Peppino',
                presenza: false
              }
            ]
          },
            {
              fermata: {
                id: 6,
                nome: 'Monte Grappa',
                orario: '13:30'
              },
              alunni: [
                {
                  name: 'Carmelo',
                  presenza: false
                },
                {
                  name: 'Luca',
                  presenza: false
                }
              ]
            },
            {
              fermata: {
                id: 7,
                nome: 'Racconigi',
                orario: '13:45'
              },
              alunni: [
                {
                  name: 'Laura',
                  presenza: false
                }
              ]
            },
            {
              fermata: {
                id: 8,
                nome: 'Bernini',
                orario: '14:00'
              },
              alunni: [
                {
                  name: 'Vercinge',
                  presenza: false
                },
                {
                  name: 'Martina',
                  presenza: false
                }
              ]
            }]
      }
    ]
  }
];


@Injectable({
  providedIn: 'root'
})
export class MongoService {

  constructor() {
  }

  getLinee() {
    return LINEE;
  }

  getPrenotazioneByLineaAndDateAndVerso(selectedLinea: number, date: Date, selectedVerso: string) {
    const temp = RESERVATIONS.find(l => l.line_id === selectedLinea)
      .dates.find(d => d.date.getDate() === date.getDate() && d.date.getFullYear() === date.getFullYear());
    return selectedVerso === 'Andata' ? temp.alunniPerFermataAndata : temp.alunniPerFermataRitorno;
  }
}
