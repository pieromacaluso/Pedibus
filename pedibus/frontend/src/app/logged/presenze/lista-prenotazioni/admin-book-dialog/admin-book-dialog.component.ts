import {Component, Inject} from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {AlunniPerFermata, AlunnoNotReserved} from '../../../line-details';
import {SignInModel} from '../../../../registration/models';
import {FormControl, Validators} from '@angular/forms';


export interface DialogData {
  alunno: AlunnoNotReserved;
  data: Date;
  verso: string;
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
    @Inject(MAT_DIALOG_DATA) public data: DialogData) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }


  submit(event) {
    if (event.isTrusted) {
      if (this.formControl.valid) {
        console.log(this.data.fermataId);
        this.dialogRef.close(`${this.data.fermataId}`);      }

    }
  }

}
