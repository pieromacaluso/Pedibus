import {Component, Inject, OnInit} from '@angular/core';
import {MatBottomSheetRef} from '@angular/material';
import {AlunniPerFermata, Alunno, AlunnoNotReserved, LineReservationVerso, PrenotazioneRequest} from '../../../line-details';
import {ReservationDTO} from '../../../genitore/dtos';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ApiService} from '../../../api.service';

export interface PresenceDialogData {
  alunno: Alunno;
  prenotazione: PrenotazioneRequest;
  res: AlunniPerFermata[];
  fermataId: number;
  resource: LineReservationVerso;
}

@Component({
  selector: 'app-presence-dialog',
  templateUrl: './presence-dialog.component.html',
  styleUrls: ['./presence-dialog.component.scss']
})
export class PresenceDialogComponent implements OnInit {
  constructor(public dialogRef: MatDialogRef<PresenceDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: PresenceDialogData,
              private apiService: ApiService) {
  }

  onNoClick(): void {
    console.log('noclick');
    this.dialogRef.close();
  }

  ngOnInit(): void {
  }

  selectAction(id) {
    switch (id) {
      case 0:
        // Preso in Carico
        this.apiService.postPresenza(this.data.alunno, this.data.prenotazione, true).subscribe((rese) => {
          this.data.alunno.update = false;
        }, (error) => {
          console.error(error);
          this.data.alunno.update = false;
        });
        break;
      case 1:
        // Preso in Carico
        this.apiService.postAssenza(this.data.alunno, this.data.prenotazione, true).subscribe((rese) => {
          this.data.alunno.update = false;
        }, (error) => {
          console.error(error);
          this.data.alunno.update = false;
        });
        break;
      case 2:
        // Preso in Carico
        this.apiService.postArrivato(this.data.alunno, this.data.prenotazione, true).subscribe((rese) => {
          this.data.alunno.update = false;
        }, (error) => {
          console.error(error);
          this.data.alunno.update = false;
        });
        break;
      case 3:
        // Preso in Carico
        this.apiService.postRestore(this.data.alunno, this.data.prenotazione).subscribe((rese) => {
          this.data.alunno.update = false;
        }, (error) => {
          console.error(error);
          this.data.alunno.update = false;
        });
        break;

    }
    this.dialogRef.close();
  }
}
