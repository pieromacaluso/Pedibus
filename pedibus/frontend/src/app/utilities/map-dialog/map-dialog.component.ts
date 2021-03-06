import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {ApiService} from '../../logged/api.service';
import {Fermata} from '../../logged/line-details';
import {Observable} from 'rxjs';


export interface MapDialogData {
  idFermata: number;
}

/**
 * Dialog che mostra la mappa di una determinata fermata
 */
@Component({
  selector: 'app-map-dialog',
  templateUrl: './map-dialog.component.html',
  styleUrls: ['./map-dialog.component.scss']
})
export class MapDialogComponent implements OnInit {
  fermata: Observable<Fermata>;

  constructor(public dialogRef: MatDialogRef<MapDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: MapDialogData,
              private apiService: ApiService) {
  }

  ngOnInit() {
    this.fermata = this.apiService.getFermata(this.data.idFermata);
  }

}
