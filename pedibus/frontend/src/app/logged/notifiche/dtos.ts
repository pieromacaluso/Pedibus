export interface Notifica {
    idNotifica: string; 
    type: string;
    dispID: string;
    usernameDestinatario: string;
    msg: string;
    isTouched: boolean;
    isAck: boolean;
}