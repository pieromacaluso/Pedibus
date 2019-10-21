// export interface LineReservation {
//   alunniPerFermataAndata: AlunniPerFermata[];
//   alunniPerFermataRitorno: AlunniPerFermata[];
//   arrivoScuola: string;
//   partenzaScuola: string;
//   childrenNotReserved: AlunnoNotReserved[];
//   canModify: boolean;
// }

export interface LineReservationVerso {
  alunniPerFermata: AlunniPerFermata[];
  orarioScuola: string;
  childrenNotReserved: AlunnoNotReserved[];
  canModify: boolean;
}

export interface AlunnoNotReserved {
  codiceFiscale: string;
  name: string;
  surname: string;
  idFermataDefault: number;
  idParent: string;
  update: boolean;
}

export interface AlunniPerFermata {
  fermata: Fermata;
  alunni: Alunno[];
}

export interface Alunno {
  codiceFiscale: string;
  name: string;
  surname: string;
  presoInCarico: boolean;
  arrivatoScuola: boolean;
  update: boolean;
}

export interface Fermata {
  id: number;
  nome: string;
  orario: string;
}

export interface PrenotazioneRequest {
  linea: string;
  verso: string;
  data: Date;
}

export interface NuovaPrenotazione {
  cfChild: string;
  idFermata: number;
  verso: number;
}

export interface StopsByLine {
  id: string;
  nome: string;
  adminMast: string;
  adminList: string[];
  andata: Fermata[];
  ritorno: Fermata[];

}
