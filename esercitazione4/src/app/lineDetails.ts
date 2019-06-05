export interface LineReservation {
  alunniPerFermataAndata: AlunniPerFermata[];
  alunniPerFermataRitorno: AlunniPerFermata[];
}

export interface AlunniPerFermata {
  fermata: Fermata;
  alunni: Alunno[];
}

export interface Alunno {
  codiceFiscale: string;
  name: string;
  surname: string;
  presenza: boolean;
}

export interface Fermata {
  id: number;
  nome: string;
  orario: string;
}
