import {Injectable} from '@angular/core';
import {forkJoin, Observable} from 'rxjs';
import {Fermata, StopsByLine} from '../line-details';
import {flatMap, map} from 'rxjs/operators';
import {ApiService} from '../api.service';
import {ChildrenDTO} from '../genitore/dtos';

export interface ForkJoinRes {
  lines: Map<string, StopsByLine>;
  andata: StopsByLine;
  ritorno: StopsByLine;
}

@Injectable({
  providedIn: 'root'
})
export class AnagraficaDialogService {

  constructor(private apiService: ApiService) {
  }


  getLineInfo(idFermata: number): Observable<StopsByLine> {
    return this.apiService.getFermata(idFermata).pipe(
      flatMap((res: Fermata) => {
        return this.apiService.getStopsByLine(res.idLinea);
      })
    );
  }

  getAllLinesInfo(): Observable<any> {
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
}
