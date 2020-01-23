import { Injectable } from '@angular/core';
import {MatDialog} from '@angular/material';
import {ChildrenDTO} from '../logged/genitore/dtos';
import {Observable} from 'rxjs';
import {Fermata} from '../logged/line-details';
import {DialogAnagraficaComponent} from '../logged/genitore/dialog-anagrafica/dialog-anagrafica.component';
import {MapDialogComponent} from './map-dialog/map-dialog.component';

@Injectable({
  providedIn: 'root'
})
export class MapService {

  constructor(public dialog: MatDialog) { }

  openMapDialog(idFermata: number): Observable<any> {
    const dialogRef = this.dialog.open(MapDialogComponent, {
      hasBackdrop: true,
      data: {idFermata}
    });
    return dialogRef.afterClosed();
  }
}
