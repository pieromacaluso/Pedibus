export interface LineDetails {
  line_id: number;
  dates: {
    date: Date;
    alunniPerFermataAndata: Prenotazioni [];
    alunniPerFermataRitorno: Prenotazioni [];
  } [];
}

export interface Linea {
  id: number;
  name: string;
}

export interface Prenotazioni {
  fermata: {
    id: number;
    nome: string;
    orario: string;
  };
  alunni: Alunno[];
}

export interface Alunno {
  name: string;
  presenza: boolean;
}
