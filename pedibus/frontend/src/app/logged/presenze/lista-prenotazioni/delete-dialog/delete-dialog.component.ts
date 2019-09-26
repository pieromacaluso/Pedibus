import {Component, Inject} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {AlunniPerFermata, Alunno, AlunnoNotReserved} from '../../../line-details';
import {FormBuilder, Validators} from '@angular/forms';

import {ApiService} from '../../../api.service';


export interface DialogData {
  alunno: Alunno;
  data: Date;
  verso: string;
  linea: string;
  res: AlunniPerFermata[];
  fermataId: number;
}

@Component({
  selector: 'app-delete-dialog',
  templateUrl: './delete-dialog.component.html',
  styleUrls: ['./delete-dialog.component.css']
})
export class DeleteDialogComponent {

  prenotazioneForm = this.fb.group({
    fermataSelect: ['', [Validators.required]]
  });

  constructor(
    public dialogRef: MatDialogRef<DeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private apiService: ApiService,
    private fb: FormBuilder) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }


  submit() {
    console.log('submit function called');
    if (this.prenotazioneForm.controls.fermataSelect.valid) {
      this.data.fermataId = this.prenotazioneForm.controls.fermataSelect.value;
      this.data.alunno.update = true;
      this.deletePrenotazione(this.data);
    }
  }

  deletePrenotazione(data: DialogData) {
    console.log('delete prenotazione function called');
  }

}
