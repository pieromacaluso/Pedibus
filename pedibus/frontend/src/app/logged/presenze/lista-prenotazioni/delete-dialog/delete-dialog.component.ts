import {Component, Inject} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {AlunniPerFermata, Alunno, AlunnoNotReserved, LineReservationVerso} from '../../../line-details';
import {FormBuilder, Validators} from '@angular/forms';

import {ApiService} from '../../../api.service';


export interface DialogData {
  alunno: Alunno;
  data: Date;
  verso: string;
  linea: string;
  res: AlunniPerFermata[];
  fermataId: number;
  resource: LineReservationVerso;
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
      this.data.fermataId = this.prenotazioneForm.controls.fermataSelect.value;
      this.data.alunno.update = true;
      this.deletePrenotazione(this.data);
  }

  deletePrenotazione(data: DialogData) {
    console.log('delete prenotazione function called');
    this.apiService.deletePrenotazioneGenitore(data.alunno.codiceFiscale, data.linea, data.data, data.verso)
      .subscribe((res) => {
        console.log('result:', res);
        data.alunno.update = false;
        this.dialogRef.close();
        const indexFermata = this.data.resource.alunniPerFermata.findIndex((afe) => {
          console.log('afe-id:', afe.fermata.nome, 'data-id:', this.data.linea);
          return afe.fermata.id === this.data.fermataId;
        });
        const indexAlunno = this.data.resource.alunniPerFermata[indexFermata]
          .alunni.findIndex((a) => a.codiceFiscale === data.alunno.codiceFiscale);
        this.data.resource.alunniPerFermata[indexFermata].alunni.splice(indexAlunno, 1);
        this.data.resource.childrenNotReserved.push({
          codiceFiscale: data.alunno.codiceFiscale,
          name: data.alunno.name,
          surname: data.alunno.surname,
          idFermataDefault: 0,
          idParent: '',
          update: false
        });
      }, (err) => console.log('err:', err));
  }

}
