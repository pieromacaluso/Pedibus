import {Component, Inject, OnInit} from '@angular/core';
import {AlunniPerFermata, AlunnoNotReserved, Fermata} from '../../../line-details';
import {DispAllResource} from '../../../disponibilita/api-disp.service';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ApiService} from '../../../api.service';
import {first} from 'rxjs/operators';
import {ApiTurniService, TurnoDispResource, TurnoResource} from '../../api-turni.service';


export interface DialogData {
  disps: DispAllResource;
  turno: TurnoResource;
  linea: Fermata[];
}

@Component({
  selector: 'app-update-disp-dialog',
  templateUrl: './update-disp-dialog.component.html',
  styleUrls: ['./update-disp-dialog.component.css']
})
export class UpdateDispDialogComponent {

  dispForm = this.fb.group({
    fermataSelect: ['', [Validators.required]]
  });

  constructor(
    public dialogRef: MatDialogRef<UpdateDispDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private apiService: ApiService,
    private apiTurniService: ApiTurniService,
    private fb: FormBuilder) {
    console.log(data);
  }

  onNoClick(): void {
    console.log('noclick');
    this.dialogRef.close();
  }


  submit() {
    if (this.dispForm.controls.fermataSelect.valid) {
      this.data.disps.idFermata = this.dispForm.controls.fermataSelect.value;
      this.updateDisp(this.data.disps);
    }
  }

  updateDisp(data: DispAllResource) {
    this.apiTurniService.updateDisp(data.id, data).pipe(first()).subscribe(response => {
      this.dialogRef.close();
    }, (error) => {
      // TODO: errore
    });
  }

}
