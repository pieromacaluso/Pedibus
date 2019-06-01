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
              name: 'Stefania',
              surname: 'Pellegrin',
              presenza: false
            },
            {
              name: 'Simone',
              surname: 'Benedetti',
              presenza: false
            },
            {
              name: 'Matteo',
              surname: 'Fabbri',
              presenza: false
            },
            {
              name: 'Michela',
              surname: 'Rossetti',
              presenza: false
            },
            {
              name: 'Valerio',
              surname: 'Damico',
              presenza: false
            },
            {
              name: 'Alberto',
              surname: 'Morelli',
              presenza: false
            },
            {
              name: 'Anna',
              surname: 'Farina',
              presenza: false
            },
            {
              name: 'Salvatore',
              surname: 'Barone',
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
                name: 'Luca',
                surname: 'Silvestri',
                presenza: false
              },
              {
                name: 'Matteo',
                surname: 'Colombo',
                presenza: false
              },
              {
                name: 'Alessandra',
                surname: 'Morelli',
                presenza: false
              },
              {
                name: 'Valeria',
                surname: 'Marini',
                presenza: false
              },
              {
                name: 'Luca',
                surname: 'Bianchi',
                presenza: false
              },
              {
                name: 'Filippo',
                surname: 'Testa',
                presenza: false
              },
              {
                name: 'Edoardo',
                surname: 'Gallo',
                presenza: false
              },
              {
                name: 'Valentina',
                surname: 'Moretti',
                presenza: false
              },
              {
                name: 'Nicole',
                surname: 'Valentini',
                presenza: false
              },
              {
                name: 'Gianluca',
                surname: 'Piras',
                presenza: false
              },
              {
                name: 'Sara',
                surname: 'Bellini',
                presenza: false
              },
              {
                name: 'Federica',
                surname: 'Martino',
                presenza: false
              },
              {
                name: 'Stefano',
                surname: 'Grasso',
                presenza: false
              },
              {
                name: 'Giovanni',
                surname: 'Mancini',
                presenza: false
              },
              {
                name: 'Stefano',
                surname: 'Costa',
                presenza: false
              },
              {
                name: 'Sara',
                surname: 'Marini',
                presenza: false
              },
              {
                name: 'Valentina',
                surname: 'Rinaldi',
                presenza: false
              },
              {
                name: 'Alessandra',
                surname: 'Pagano',
                presenza: false
              },
              {
                name: 'Claudia',
                surname: 'Parisi',
                presenza: false
              },
              {
                name: 'Giulia',
                surname: 'Conte',
                presenza: false
              },
              {
                name: 'Claudio',
                surname: 'Marino',
                presenza: false
              },
              {
                name: 'Cristina',
                surname: 'Grassi',
                presenza: false
              },
              {
                name: 'Lisa',
                surname: 'Monti',
                presenza: false
              },
              {
                name: 'Jessica',
                surname: 'Vitali',
                presenza: false
              },
              {
                name: 'Serena',
                surname: 'Ruggiero',
                presenza: false
              },
              {
                name: 'Nicola',
                surname: 'Fabbri',
                presenza: false
              },
              {
                name: 'Angela',
                surname: 'Bianco',
                presenza: false
              },
              {
                name: 'Nicolò',
                surname: 'Valentini',
                presenza: false
              },
              {
                name: 'Gabriele',
                surname: 'Marchetti',
                presenza: false
              },
              {
                name: 'Valentina',
                surname: 'Orlando',
                presenza: false
              },
              {
                name: 'Anna',
                surname: 'Carbone',
                presenza: false
              },
              {
                name: 'Alessandro',
                surname: 'Carbone',
                presenza: false
              },
              {
                name: 'Dario',
                surname: 'Negri',
                presenza: false
              },
              {
                name: 'Michela',
                surname: 'Amato',
                presenza: false
              },
              {
                name: 'Stefania',
                surname: 'Pellegrini',
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
                name: 'Salvatore',
                surname: 'Barone',
                presenza: false
              },
              {
                name: 'Luca',
                surname: 'Silvestri',
                presenza: false
              },
              {
                name: 'Matteo',
                surname: 'Colombo',
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
            alunni: []
          }]
      ,
      alunniPerFermataRitorno:
        [{
          fermata: {
            id: 5,
            nome: 'Scuola',
            orario: '13:10'
          },
          alunni: []
        },
          {
            fermata: {
              id: 6,
              nome: 'Monte Grappa',
              orario: '13:30'
            },
            alunni: [
              {
                name: 'Salvatore',
                surname: 'Barone',
                presenza: false
              },
              {
                name: 'Luca',
                surname: 'Silvestri',
                presenza: false
              },
              {
                name: 'Matteo',
                surname: 'Colombo',
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
                name: 'Luca',
                surname: 'Silvestri',
                presenza: false
              },
              {
                name: 'Matteo',
                surname: 'Colombo',
                presenza: false
              },
              {
                name: 'Alessandra',
                surname: 'Morelli',
                presenza: false
              },
              {
                name: 'Valeria',
                surname: 'Marini',
                presenza: false
              },
              {
                name: 'Luca',
                surname: 'Bianchi',
                presenza: false
              },
              {
                name: 'Filippo',
                surname: 'Testa',
                presenza: false
              },
              {
                name: 'Edoardo',
                surname: 'Gallo',
                presenza: false
              },
              {
                name: 'Valentina',
                surname: 'Moretti',
                presenza: false
              },
              {
                name: 'Nicole',
                surname: 'Valentini',
                presenza: false
              },
              {
                name: 'Gianluca',
                surname: 'Piras',
                presenza: false
              },
              {
                name: 'Sara',
                surname: 'Bellini',
                presenza: false
              },
              {
                name: 'Federica',
                surname: 'Martino',
                presenza: false
              },
              {
                name: 'Stefano',
                surname: 'Grasso',
                presenza: false
              },
              {
                name: 'Giovanni',
                surname: 'Mancini',
                presenza: false
              },
              {
                name: 'Stefano',
                surname: 'Costa',
                presenza: false
              },
              {
                name: 'Sara',
                surname: 'Marini',
                presenza: false
              },
              {
                name: 'Valentina',
                surname: 'Rinaldi',
                presenza: false
              },
              {
                name: 'Alessandra',
                surname: 'Pagano',
                presenza: false
              },
              {
                name: 'Claudia',
                surname: 'Parisi',
                presenza: false
              },
              {
                name: 'Giulia',
                surname: 'Conte',
                presenza: false
              },
              {
                name: 'Claudio',
                surname: 'Marino',
                presenza: false
              },
              {
                name: 'Cristina',
                surname: 'Grassi',
                presenza: false
              },
              {
                name: 'Lisa',
                surname: 'Monti',
                presenza: false
              },
              {
                name: 'Jessica',
                surname: 'Vitali',
                presenza: false
              },
              {
                name: 'Serena',
                surname: 'Ruggiero',
                presenza: false
              },
              {
                name: 'Nicola',
                surname: 'Fabbri',
                presenza: false
              },
              {
                name: 'Angela',
                surname: 'Bianco',
                presenza: false
              },
              {
                name: 'Nicolò',
                surname: 'Valentini',
                presenza: false
              },
              {
                name: 'Gabriele',
                surname: 'Marchetti',
                presenza: false
              },
              {
                name: 'Valentina',
                surname: 'Orlando',
                presenza: false
              },
              {
                name: 'Anna',
                surname: 'Carbone',
                presenza: false
              },
              {
                name: 'Alessandro',
                surname: 'Carbone',
                presenza: false
              },
              {
                name: 'Dario',
                surname: 'Negri',
                presenza: false
              },
              {
                name: 'Michela',
                surname: 'Amato',
                presenza: false
              },
              {
                name: 'Stefania',
                surname: 'Pellegrini',
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
                name: 'Stefania',
                surname: 'Pellegrin',
                presenza: false
              },
              {
                name: 'Simone',
                surname: 'Benedetti',
                presenza: false
              },
              {
                name: 'Matteo',
                surname: 'Fabbri',
                presenza: false
              },
              {
                name: 'Michela',
                surname: 'Rossetti',
                presenza: false
              },
              {
                name: 'Valerio',
                surname: 'Damico',
                presenza: false
              },
              {
                name: 'Alberto',
                surname: 'Morelli',
                presenza: false
              },
              {
                name: 'Anna',
                surname: 'Farina',
                presenza: false
              },
              {
                name: 'Salvatore',
                surname: 'Barone',
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
                name: 'Cristina',
                surname: 'Grassi',
                presenza: false
              },
              {
                name: 'Lisa',
                surname: 'Monti',
                presenza: false
              },
              {
                name: 'Jessica',
                surname: 'Vitali',
                presenza: false
              },
              {
                name: 'Serena',
                surname: 'Ruggiero',
                presenza: false
              },
              {
                name: 'Nicola',
                surname: 'Fabbri',
                presenza: false
              },
              {
                name: 'Angela',
                surname: 'Bianco',
                presenza: false
              },
              {
                name: 'Nicolò',
                surname: 'Valentini',
                presenza: false
              },
              {
                name: 'Gabriele',
                surname: 'Marchetti',
                presenza: false
              },
              {
                name: 'Valentina',
                surname: 'Orlando',
                presenza: false
              },
              {
                name: 'Anna',
                surname: 'Carbone',
                presenza: false
              },
              {
                name: 'Alessandro',
                surname: 'Carbone',
                presenza: false
              },
              {
                name: 'Dario',
                surname: 'Negri',
                presenza: false
              },
              {
                name: 'Michela',
                surname: 'Amato',
                presenza: false
              },
              {
                name: 'Stefania',
                surname: 'Pellegrini',
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
                name: 'Luca',
                surname: 'Silvestri',
                presenza: false
              },
              {
                name: 'Matteo',
                surname: 'Colombo',
                presenza: false
              },
              {
                name: 'Alessandra',
                surname: 'Morelli',
                presenza: false
              },
              {
                name: 'Valeria',
                surname: 'Marini',
                presenza: false
              },
              {
                name: 'Luca',
                surname: 'Bianchi',
                presenza: false
              },
              {
                name: 'Filippo',
                surname: 'Testa',
                presenza: false
              },
              {
                name: 'Edoardo',
                surname: 'Gallo',
                presenza: false
              },
              {
                name: 'Valentina',
                surname: 'Moretti',
                presenza: false
              },
              {
                name: 'Nicole',
                surname: 'Valentini',
                presenza: false
              },
              {
                name: 'Gianluca',
                surname: 'Piras',
                presenza: false
              },
              {
                name: 'Sara',
                surname: 'Bellini',
                presenza: false
              },
              {
                name: 'Federica',
                surname: 'Martino',
                presenza: false
              },
              {
                name: 'Stefano',
                surname: 'Grasso',
                presenza: false
              },
              {
                name: 'Giovanni',
                surname: 'Mancini',
                presenza: false
              },
              {
                name: 'Stefano',
                surname: 'Costa',
                presenza: false
              },
              {
                name: 'Sara',
                surname: 'Marini',
                presenza: false
              },
              {
                name: 'Valentina',
                surname: 'Rinaldi',
                presenza: false
              },
              {
                name: 'Alessandra',
                surname: 'Pagano',
                presenza: false
              },
              {
                name: 'Claudia',
                surname: 'Parisi',
                presenza: false
              },
              {
                name: 'Giulia',
                surname: 'Conte',
                presenza: false
              },
              {
                name: 'Claudio',
                surname: 'Marino',
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
            alunni: []
          }]
      ,
      alunniPerFermataRitorno:
        [{
          fermata: {
            id: 5,
            nome: 'Scuola',
            orario: '13:10'
          },
          alunni: []
        },
          {
            fermata: {
              id: 6,
              nome: 'Monte Grappa',
              orario: '13:30'
            },
            alunni: [
              {
                name: 'Luca',
                surname: 'Silvestri',
                presenza: false
              },
              {
                name: 'Matteo',
                surname: 'Colombo',
                presenza: false
              },
              {
                name: 'Alessandra',
                surname: 'Morelli',
                presenza: false
              },
              {
                name: 'Valeria',
                surname: 'Marini',
                presenza: false
              },
              {
                name: 'Luca',
                surname: 'Bianchi',
                presenza: false
              },
              {
                name: 'Filippo',
                surname: 'Testa',
                presenza: false
              },
              {
                name: 'Edoardo',
                surname: 'Gallo',
                presenza: false
              },
              {
                name: 'Valentina',
                surname: 'Moretti',
                presenza: false
              },
              {
                name: 'Nicole',
                surname: 'Valentini',
                presenza: false
              },
              {
                name: 'Gianluca',
                surname: 'Piras',
                presenza: false
              },
              {
                name: 'Sara',
                surname: 'Bellini',
                presenza: false
              },
              {
                name: 'Federica',
                surname: 'Martino',
                presenza: false
              },
              {
                name: 'Stefano',
                surname: 'Grasso',
                presenza: false
              },
              {
                name: 'Giovanni',
                surname: 'Mancini',
                presenza: false
              },
              {
                name: 'Stefano',
                surname: 'Costa',
                presenza: false
              },
              {
                name: 'Sara',
                surname: 'Marini',
                presenza: false
              },
              {
                name: 'Valentina',
                surname: 'Rinaldi',
                presenza: false
              },
              {
                name: 'Alessandra',
                surname: 'Pagano',
                presenza: false
              },
              {
                name: 'Claudia',
                surname: 'Parisi',
                presenza: false
              },
              {
                name: 'Giulia',
                surname: 'Conte',
                presenza: false
              },
              {
                name: 'Claudio',
                surname: 'Marino',
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
                name: 'Cristina',
                surname: 'Grassi',
                presenza: false
              },
              {
                name: 'Lisa',
                surname: 'Monti',
                presenza: false
              },
              {
                name: 'Jessica',
                surname: 'Vitali',
                presenza: false
              },
              {
                name: 'Serena',
                surname: 'Ruggiero',
                presenza: false
              },
              {
                name: 'Nicola',
                surname: 'Fabbri',
                presenza: false
              },
              {
                name: 'Angela',
                surname: 'Bianco',
                presenza: false
              },
              {
                name: 'Nicolò',
                surname: 'Valentini',
                presenza: false
              },
              {
                name: 'Gabriele',
                surname: 'Marchetti',
                presenza: false
              },
              {
                name: 'Valentina',
                surname: 'Orlando',
                presenza: false
              },
              {
                name: 'Anna',
                surname: 'Carbone',
                presenza: false
              },
              {
                name: 'Alessandro',
                surname: 'Carbone',
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
                name: 'Dario',
                surname: 'Negri',
                presenza: false
              },
              {
                name: 'Michela',
                surname: 'Amato',
                presenza: false
              },
              {
                name: 'Stefania',
                surname: 'Pellegrini',
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
              nome: 'Duca Degli Abruzzi',
              orario: '07:20'
            },
            alunni: [
              {
                name: 'Jessica',
                surname: 'Giuliani',
                presenza: false
              },
              {
                name: 'Jessica',
                surname: 'Sala',
                presenza: false
              },
              {
                name: 'Tommaso',
                surname: 'Parisi',
                presenza: false
              },
              {
                name: 'Lisa',
                surname: 'Rossetti',
                presenza: false
              },
              {
                name: 'Vincenzo',
                surname: 'Piras',
                presenza: false
              },
              {
                name: 'Sara',
                surname: 'Bellini',
                presenza: false
              },
              {
                name: 'Serena',
                surname: 'Longo',
                presenza: false
              },
            ]
          },
            {
              fermata: {
                id: 2,
                nome: 'Stati Uniti',
                orario: '07:30'
              },
              alunni: [
                {
                  name: 'Lucia De',
                  surname: 'Luca',
                  presenza: false
                },
                {
                  name: 'Riccardo',
                  surname: 'Lombardi',
                  presenza: false
                },
                {
                  name: 'Ilaria',
                  surname: 'Bernardi',
                  presenza: false
                },
                {
                  name: 'Mirko',
                  surname: 'Costa',
                  presenza: false
                },
                {
                  name: 'Michele',
                  surname: 'Esposito',
                  presenza: false
                }
              ]
            },
            {
              fermata: {
                id: 3,
                nome: 'Vinzaglio',
                orario: '07:40'
              },
              alunni: [
                {
                  name: 'Nicole',
                  surname: 'Riva',
                  presenza: false
                },
                {
                  name: 'Greta',
                  surname: 'Caruso',
                  presenza: false
                },
                {
                  name: 'Erica',
                  surname: 'Giuliani',
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
              alunni: []
            }]
        ,
        alunniPerFermataRitorno:
          [{
            fermata: {
              id: 5,
              nome: 'Scuola',
              orario: '13:10'
            },
            alunni: []
          },
            {
              fermata: {
                id: 6,
                nome: 'Vinzaglio',
                orario: '13:30'
              },
              alunni: [
                {
                  name: 'Nicole',
                  surname: 'Riva',
                  presenza: false
                },
                {
                  name: 'Greta',
                  surname: 'Caruso',
                  presenza: false
                },
                {
                  name: 'Erica',
                  surname: 'Giuliani',
                  presenza: false
                }
              ]
            },
            {
              fermata: {
                id: 7,
                nome: 'Stati Uniti',
                orario: '13:45'
              },
              alunni: [
                {
                  name: 'Lucia De',
                  surname: 'Luca',
                  presenza: false
                },
                {
                  name: 'Riccardo',
                  surname: 'Lombardi',
                  presenza: false
                },
                {
                  name: 'Ilaria',
                  surname: 'Bernardi',
                  presenza: false
                },
                {
                  name: 'Mirko',
                  surname: 'Costa',
                  presenza: false
                },
                {
                  name: 'Michele',
                  surname: 'Esposito',
                  presenza: false
                },
              ]
            },
            {
              fermata: {
                id: 8,
                nome: 'Duca Degli Abruzzi',
                orario: '14:00'
              },
              alunni: [
                {
                  name: 'Jessica',
                  surname: 'Giuliani',
                  presenza: false
                },
                {
                  name: 'Jessica',
                  surname: 'Sala',
                  presenza: false
                },
                {
                  name: 'Tommaso',
                  surname: 'Parisi',
                  presenza: false
                },
                {
                  name: 'Lisa',
                  surname: 'Rossetti',
                  presenza: false
                },
                {
                  name: 'Vincenzo',
                  surname: 'Piras',
                  presenza: false
                },
                {
                  name: 'Sara',
                  surname: 'Bellini',
                  presenza: false
                },
                {
                  name: 'Serena',
                  surname: 'Longo',
                  presenza: false
                },
              ]
            }]
      },
      {
        date: new Date(Date.now() + 24 * 60 * 60 * 1000),
        alunniPerFermataAndata:
          [{
            fermata: {
              id: 1,
              nome: 'Duca Degli Abruzzi',
              orario: '07:20'
            },
            alunni: [

              {
                name: 'Tommaso',
                surname: 'Parisi',
                presenza: false
              },
              {
                name: 'Lisa',
                surname: 'Rossetti',
                presenza: false
              },
              {
                name: 'Vincenzo',
                surname: 'Piras',
                presenza: false
              },
              {
                name: 'Sara',
                surname: 'Bellini',
                presenza: false
              },
              {
                name: 'Serena',
                surname: 'Longo',
                presenza: false
              },
            ]
          },
            {
              fermata: {
                id: 2,
                nome: 'Stati Uniti',
                orario: '07:30'
              },
              alunni: [
                {
                  name: 'Lucia De',
                  surname: 'Luca',
                  presenza: false
                },
                {
                  name: 'Riccardo',
                  surname: 'Lombardi',
                  presenza: false
                },
                {
                  name: 'Jessica',
                  surname: 'Giuliani',
                  presenza: false
                },
                {
                  name: 'Jessica',
                  surname: 'Sala',
                  presenza: false
                },
                {
                  name: 'Ilaria',
                  surname: 'Bernardi',
                  presenza: false
                },
                {
                  name: 'Mirko',
                  surname: 'Costa',
                  presenza: false
                },
                {
                  name: 'Michele',
                  surname: 'Esposito',
                  presenza: false
                }
              ]
            },
            {
              fermata: {
                id: 3,
                nome: 'Vinzaglio',
                orario: '07:40'
              },
              alunni: [
                {
                  name: 'Nicole',
                  surname: 'Riva',
                  presenza: false
                },
                {
                  name: 'Greta',
                  surname: 'Caruso',
                  presenza: false
                },
                {
                  name: 'Erica',
                  surname: 'Giuliani',
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
              alunni: []
            }]
        ,
        alunniPerFermataRitorno:
          [{
            fermata: {
              id: 5,
              nome: 'Scuola',
              orario: '13:10'
            },
            alunni: []
          },
            {
              fermata: {
                id: 6,
                nome: 'Vinzaglio',
                orario: '13:30'
              },
              alunni: [
                {
                  name: 'Nicole',
                  surname: 'Riva',
                  presenza: false
                },
                {
                  name: 'Greta',
                  surname: 'Caruso',
                  presenza: false
                },
                {
                  name: 'Erica',
                  surname: 'Giuliani',
                  presenza: false
                }
              ]
            },
            {
              fermata: {
                id: 7,
                nome: 'Stati Uniti',
                orario: '13:45'
              },
              alunni: [
                {
                  name: 'Lucia De',
                  surname: 'Luca',
                  presenza: false
                },
                {
                  name: 'Riccardo',
                  surname: 'Lombardi',
                  presenza: false
                },
                {
                  name: 'Ilaria',
                  surname: 'Bernardi',
                  presenza: false
                },
                {
                  name: 'Mirko',
                  surname: 'Costa',
                  presenza: false
                },
                {
                  name: 'Michele',
                  surname: 'Esposito',
                  presenza: false
                },
                {
                  name: 'Vincenzo',
                  surname: 'Piras',
                  presenza: false
                },
                {
                  name: 'Sara',
                  surname: 'Bellini',
                  presenza: false
                },
                {
                  name: 'Serena',
                  surname: 'Longo',
                  presenza: false
                },
              ]
            },
            {
              fermata: {
                id: 8,
                nome: 'Duca Degli Abruzzi',
                orario: '14:00'
              },
              alunni: [
                {
                  name: 'Jessica',
                  surname: 'Giuliani',
                  presenza: false
                },
                {
                  name: 'Jessica',
                  surname: 'Sala',
                  presenza: false
                },
                {
                  name: 'Tommaso',
                  surname: 'Parisi',
                  presenza: false
                },
                {
                  name: 'Lisa',
                  surname: 'Rossetti',
                  presenza: false
                },

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

  static getLinee() {
    return LINEE;
  }

  getPrenotazioneByLineaAndDateAndVerso(selectedLinea: number, date: Date, selectedVerso: string) {
    const temp1 = RESERVATIONS.find(l => l.line_id === selectedLinea);
    if (temp1 !== undefined) {
      const temp2 = temp1.dates.find(d => d.date.getDate() === date.getDate()
        && d.date.getMonth() === date.getMonth() && d.date.getFullYear() === date.getFullYear());
      if (temp2 !== undefined) {
        return selectedVerso === 'Andata' ? temp2.alunniPerFermataAndata : temp2.alunniPerFermataRitorno;
      }
    }
  }
}
