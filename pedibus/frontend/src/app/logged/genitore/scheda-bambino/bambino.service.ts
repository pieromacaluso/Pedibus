import { Injectable } from '@angular/core';
import { ApiService } from '../../api.service';
import { Observable } from 'rxjs';
import { Fermata } from '../../line-details';
import { MatDialog } from '@angular/material';
import { DialogAnagraficaComponent } from '../dialog-anagrafica/dialog-anagrafica.component';
import { ChildrenDTO,ReservationDTO } from '../dtos';

@Injectable({
  providedIn: 'root'
})
export class BambinoService {

  constructor(private apiService: ApiService, public dialog: MatDialog) {

  }

  getFertmata(idFermata: number): Observable<Fermata> {
    return this.apiService.getFermata(idFermata);
  }

  updateFermate(data: ChildrenDTO): Observable<any> {
    return this.apiService.updateFermate(data);
  }

  getStatus(bambino: ChildrenDTO, data: Date, verso: string): Observable<ReservationDTO> {
    return this.apiService.getStatus(bambino, data, verso);
  }

  openDialog(bambino: ChildrenDTO, linee: string[]): Observable<any> {
    const dialogRef = this.dialog.open(DialogAnagraficaComponent, {
      hasBackdrop: true,
      data: {child: bambino, linee: linee}
    });
    return dialogRef.afterClosed();
  }
}
