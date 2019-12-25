export interface ChildrenDTO {
    codiceFiscale: string; //Se si cambia questo campo bisogna cambiare "$.alunniPerFermataAndata[0].alunni[0].codiceFiscale" in test2
    name: string;
    surname: string;
    idFermataAndata: number;   //in fase di registrazione ad ogni bambino bisogna indicare la sua fermata di default dalla quale partire/arrivare
    idFermataRitorno: number;
    //private ObjectId idParent;
}

export interface FermataDTO {
    id: number;
    nome: number;
    orario: string;
}

export interface ReservationDTO {
    cfChild: string;
    data: Date;
    idLinea: string;
    idFermata: number;
    verso: boolean;
    presoInCarico: boolean;
    arrivatoScuola: boolean;
}