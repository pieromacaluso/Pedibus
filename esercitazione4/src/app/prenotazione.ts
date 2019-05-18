export interface Prenotazione {
  alunniPerFermataAndata: {
      fermata: {
        id: number;
        nome: string;
        orario: string;
      },
      alunni: string[];
    } [];
  alunniPerFermataRitorno: {
      fermata: {
        id: number;
        nome: string;
        orario: string;
      },
      alunni: string[];
    }[];
}
