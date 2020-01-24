import {Component, Inject} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {AlunniPerFermata, Alunno, AlunnoNotReserved} from '../../../line-details';
import {FormBuilder, Validators} from '@angular/forms';

import {ApiService} from '../../../api.service';


export interface DialogData {
  alunno: AlunnoNotReserved;
  data: Date;
  verso: string;
  linea: string;
  res: AlunniPerFermata[];
  fermataId: number;
}

@Component({
  selector: 'app-admin-book-dialog',
  templateUrl: 'admin-book-dialog.component.html',
})
export class AdminBookDialogComponent {


  prenotazioneForm = this.fb.group({
    fermataSelect: ['', [Validators.required]]
  });

  constructor(
    public dialogRef: MatDialogRef<AdminBookDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private apiService: ApiService,
    private fb: FormBuilder) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }


  submit() {
    if (this.prenotazioneForm.controls.fermataSelect.valid) {
      this.data.fermataId = this.prenotazioneForm.controls.fermataSelect.value;
      this.data.alunno.update = true;
      this.nuovaPrenotazione(this.data);
    }
  }

  nuovaPrenotazione(data: DialogData) {
    this.apiService.postPrenotazioneDialog(data).subscribe((rese) => {
      data.alunno.update = false;
      this.dialogRef.close();
    }, (error) => {
      console.error(error);
      data.alunno.update = false;
    });
  }
}
