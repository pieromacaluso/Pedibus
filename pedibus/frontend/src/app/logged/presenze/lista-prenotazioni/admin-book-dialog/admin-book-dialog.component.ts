import {Component, Inject} from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {AlunniPerFermata, Alunno, AlunnoNotReserved, NuovaPrenotazione} from '../../../line-details';
import {SignInModel} from '../../../../registration/models';
import {FormControl, Validators} from '@angular/forms';
import {ApiService} from '../../../api.service';
import {first} from 'rxjs/operators';


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

  formControl = new FormControl('', [Validators.required]);

  constructor(
    public dialogRef: MatDialogRef<AdminBookDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private apiService: ApiService) {
  }

  onNoClick(): void {
    console.log('noclick');
    this.dialogRef.close();
  }


  submit() {
    console.log('submit function called');
    if (this.formControl.valid) {
      this.data.fermataId = this.formControl.value;
      this.data.alunno.update = true;
      this.nuovaPrenotazione(this.data);
    }
  }

  nuovaPrenotazione(data: DialogData) {
    console.log('nuovaPrenotazione function called');
    this.apiService.postPrenotazioneDialog(data).subscribe((rese) => {
      console.log('subscribe in nuovaPrenotazione emmited something');
      data.alunno.update = false;
      this.dialogRef.close();
    }, (error) => console.error(error));
  }
}