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


  getLineInfo(idFermata: number): Observable<StopsByLine> {
    return this.apiService.getFermata(idFermata).pipe(
      flatMap((res: Fermata) => {
        return this.apiService.getStopsByLine(res.idLinea);
      })
    );
  }

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

  addChild(child: ChildrenDTO) {
    return this.apiService.addChild(child);
  }

  updateChild(codiceFiscale: string, child: ChildrenDTO) {
    return this.apiService.updateChild(codiceFiscale, child);
  }

  getRoles(): Observable<string[]> {
    return this.apiService.getRoles();
  }

  childDetails(value: string): Observable<ChildrenDTO> {
    return this.apiService.getChild(value);
  }

  updateUser(oldEmail: string, user: UserDTO) {
    return this.apiService.updateUser(oldEmail, user);
  }

  getListChildKey(value: string): Observable<ChildrenDTO[]> {
    return this.apiService.getFirst5Children(value);
  }

  subscribeKeyword() {
    return this.keywordChild.pipe(debounceTime(1000));
  }

  emitKeyword(value: string) {
    this.keywordChildSource.next(value);
  }

  createUser(user: UserDTO) {
    return this.apiService.createUser(user);

  }
}
