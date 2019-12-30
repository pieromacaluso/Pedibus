import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {ApiService} from '../../api.service';
import {FormBuilder, Validators} from '@angular/forms';
import {ChildrenDTO, ReservationDTO} from '../dtos';
import {Observable} from 'rxjs';
import {Fermata, PrenotazioneRequest, StopsByLine} from '../../line-details';


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

  prenotazioneForm = this.fb.group({
    lineSelect: ['', [Validators.required]],
    stopSelect: ['', [Validators.required]]
  });
  lineData: Map<string, StopsByLine> = new Map<string, StopsByLine>();
  loading = 0;
  private stops: Fermata[];
  private selectedStop: Fermata;
  private selectedLine: string;

  constructor(
    public dialogRef: MatDialogRef<DialogPrenotazioneComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PrenotazioneDialogData,
    private apiService: ApiService,
    private fb: FormBuilder
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
      }, (error) => console.log(error));
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
    this.selectedLine = value;
    this.stops = this.data.andata ? this.lineData.get(value).andata : this.lineData.get(value).ritorno;
  }

  selectStop(value: any) {
    this.selectedStop = this.stops.find((stop) => stop.id === value);
  }

  cancel() {
    this.dialogRef.close();
  }
}
