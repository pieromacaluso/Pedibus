import {Injectable} from '@angular/core';
import {ApiService} from '../../api.service';
import {Observable} from 'rxjs';
import {Fermata} from '../../line-details';
import {MatDialog} from '@angular/material';
import {DialogAnagraficaComponent} from '../dialog-anagrafica/dialog-anagrafica.component';
import {ChildrenDTO, ReservationDTO} from '../dtos';
import {RxStompService} from '@stomp/ng2-stompjs';

@Injectable({
  providedIn: 'root'
})
export class BambinoService {

  constructor(private apiService: ApiService, public dialog: MatDialog, private rxStompService: RxStompService) {

  }

  getFermata(idFermata: number): Observable<Fermata> {
    return this.apiService.getFermata(idFermata);
  }

  updateFermate(data: ChildrenDTO): Observable<any> {
    return this.apiService.updateFermate(data);
  }

  getStatus(bambino: ChildrenDTO, data: Date): Observable<ReservationDTO[]> {
    return this.apiService.getStatus(bambino, data);
  }

  openDialog(bambino: ChildrenDTO, linee: string[], defaultAndata: Observable<Fermata>, defaultRitorno: Observable<Fermata>): Observable<any> {
    const dialogRef = this.dialog.open(DialogAnagraficaComponent, {
      hasBackdrop: true,
      data: {child: bambino, linee, defaultAndata, defaultRitorno}
    });
    return dialogRef.afterClosed();
  }

  watchChild(cf: string) {
    return this.rxStompService.watch('/user/child/' + cf);
  }
}
