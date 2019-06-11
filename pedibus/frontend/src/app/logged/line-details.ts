export interface LineReservation {
  alunniPerFermataAndata: AlunniPerFermata[];
  alunniPerFermataRitorno: AlunniPerFermata[];
}

export interface AlunnoNotReserved {
  codiceFiscale: string;
  name: string;
  surname: string;
  idFermataDefault: number;
  idParent: string;
}

export interface NotReservation {
  childrenNotReserved: AlunnoNotReserved[];
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
