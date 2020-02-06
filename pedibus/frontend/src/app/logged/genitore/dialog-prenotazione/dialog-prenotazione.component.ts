import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {ApiService} from '../../api.service';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {ChildrenDTO, ReservationDTO} from '../dtos';
import {Observable} from 'rxjs';
import {Fermata, PrenotazioneRequest, StopsByLine} from '../../line-details';
import {Point} from 'geojson';


export interface PrenotazioneDialogData {
  bambino: ChildrenDTO;
  andata: boolean;
  data: Date;
  linee: string[];
  aggiunta: boolean;
  reservation: ReservationDTO;
}


@Component({
  selector: 'app-dialog-prenotazione',
  templateUrl: './dialog-prenotazione.component.html',
  styleUrls: ['./dialog-prenotazione.component.scss']
})
export class DialogPrenotazioneComponent implements OnInit {

  prenotazioneForm = new FormGroup({
    lineSelect: new FormControl('', Validators.required),
    stopSelect: new FormControl('', Validators.required)
  });
  lineData: Map<string, StopsByLine> = new Map<string, StopsByLine>();
  loading = 0;
  private stops: Fermata[];
  private selectedStop: Fermata;
  private selectedLine: string;
  private stopsLocation: Point[] = [];
  private stopsDescription: string[] = [];

  constructor(
    public dialogRef: MatDialogRef<DialogPrenotazioneComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PrenotazioneDialogData,
    private apiService: ApiService,
  ) {

  }

  ngOnInit() {
    this.data.linee.forEach((value, index) => {
      this.loading++;
      this.apiService.getStopsByLine(value).subscribe((data) => {
        this.lineData.set(value, data);
        if (this.data.reservation && this.data.reservation.idLinea === value) {
          this.changeFermate(this.data.reservation.idLinea);
          this.selectStop(this.data.reservation.idFermata);
          this.prenotazioneForm.patchValue({
            lineSelect: this.data.reservation.idLinea,
            stopSelect: this.data.reservation.idFermata
          });
        }
        this.loading--;
      }, (error) => {
        // TODO: ERROR?
      });
    });
  }

  submit() {
    this.dialogRef.close({
      line: this.selectedLine,
      data: this.data.data,
      childId: this.data.bambino.codiceFiscale,
      stopId: this.selectedStop.id,
      verso: this.data.andata
    });
  }


  changeFermate(value: any) {
    this.selectedStop = undefined;
    this.prenotazioneForm.patchValue({
      stopSelect: undefined
    });
    const sameLine = this.selectedLine === value;
    this.selectedLine = value;
    if (!sameLine) {
      this.stops = this.data.andata ? this.lineData.get(value).andata : this.lineData.get(value).ritorno;
      this.stopsLocation = [];
      this.stopsDescription = [];
      for (const s of this.stops) {
        this.stopsLocation.push(s.location);
        this.stopsDescription.push(s.nome);
      }
    }
  }

  selectStopIndex(value: any) {
    this.selectedStop = this.stops[value];
    this.prenotazioneForm.patchValue({
      stopSelect: this.selectedStop.id
    });
  }

  selectStop(value: any) {
    this.selectedStop = this.stops.find((stop) => stop.id === value);
  }

  cancel() {
    this.dialogRef.close();
  }

  isOnTime(orario: string) {
    const date = new Date();
    const reqData = this.data.data;
    if (reqData.getFullYear() === date.getFullYear() && reqData.getMonth() === date.getMonth() && reqData.getDate() === date.getDate()) {
      const ora = +orario.split(':')[0];
      const min = +orario.split(':')[1];
      return date.getHours() < ora || (date.getHours() === ora && date.getMinutes() < min);
    }
    return true;

  }
}
