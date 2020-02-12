import {Injectable} from '@angular/core';
import {BehaviorSubject, forkJoin, Observable} from 'rxjs';
import {Fermata, StopsByLine} from '../line-details';
import {debounceTime, flatMap, map} from 'rxjs/operators';
import {ApiService} from '../api.service';
import {ChildrenDTO} from '../genitore/dtos';
import {UserDTO} from './dtos';

export interface ForkJoinRes {
  lines: Map<string, StopsByLine>;
  andata: StopsByLine;
  ritorno: StopsByLine;
}

@Injectable({
  providedIn: 'root'
})
export class AnagraficaDialogService {

  private keywordChildSource = new BehaviorSubject<string>('');
  keywordChild = this.keywordChildSource.asObservable();


  constructor(private apiService: ApiService) {
  }


  /**
   * Ottieni informazioni sulla linea a partire dall'id di un fermata in essa contenuta
   * @param idFermata id della fermata
   */
  getLineInfo(idFermata: number): Observable<StopsByLine> {
    return this.apiService.getFermata(idFermata).pipe(
      flatMap((res: Fermata) => {
        return this.apiService.getStopsByLine(res.idLinea);
      })
    );
  }

  /**
   * Ottieni le informazioni di tutte le linee andando a trasformarle in una Mappa
   */
  getAllLinesInfo(): Observable<Map<string, StopsByLine>> {
    return this.apiService.getLinee().pipe(
      flatMap((res: string[]) => {
        const subArray = [];
        res.forEach(((value, index) => {
          subArray.push(this.apiService.getStopsByLine(value));
        }));
        return forkJoin(subArray);
      }),
      map((obj: StopsByLine[]) => {
        const mapLine = new Map<string, StopsByLine>();
        obj.forEach((value => {
          mapLine.set(value.id, value);
        }));
        return mapLine;
      }));
  }

  /**
   * Aggiungi un bambino
   * @param child struttura bambino
   */
  addChild(child: ChildrenDTO) {
    return this.apiService.addChild(child);
  }

  /**
   * Aggiorna un bambino
   * @param codiceFiscale codice fiscale del bambino da aggiornare
   * @param child struttura bambino
   */
  updateChild(codiceFiscale: string, child: ChildrenDTO) {
    return this.apiService.updateChild(codiceFiscale, child);
  }

  /**
   * Ottieni i ruoli
   */
  getRoles(): Observable<string[]> {
    return this.apiService.getRoles();
  }

  /**
   * Ottieni i dettagli del bambino a partire dal suo codice fiscale
   * @param value codice fiscale
   */
  childDetails(value: string): Observable<ChildrenDTO> {
    return this.apiService.getChild(value);
  }

  /**
   * Aggiorna l'utente
   * @param oldEmail vecchia email
   * @param user struttura utente
   */
  updateUser(oldEmail: string, user: UserDTO) {
    return this.apiService.updateUser(oldEmail, user);
  }

  /**
   * Ottieni una lista di 5 bambini a partire da una ricerca con keywords
   * @param value keywords
   */
  getListChildKey(value: string): Observable<ChildrenDTO[]> {
    return this.apiService.getFirst5Children(value);
  }

  /**
   * Iscrizione a Keyword Child
   */
  subscribeKeyword() {
    return this.keywordChild.pipe(debounceTime(1000));
  }

  /**
   * Emetti Keyword
   * @param value keyword
   */
  emitKeyword(value: string) {
    this.keywordChildSource.next(value);
  }

  /**
   * Crea un utente
   * @param user struttura utente
   */
  createUser(user: UserDTO) {
    return this.apiService.createUser(user);

  }
}
