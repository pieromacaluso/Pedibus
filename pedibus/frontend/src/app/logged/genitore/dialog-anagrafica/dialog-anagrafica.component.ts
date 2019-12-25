import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { ApiService } from '../../api.service';
import { FormBuilder, Validators } from '@angular/forms';
import { ChildrenDTO } from '../dtos';
import { Fermata } from '../../line-details';
import { BambinoService } from '../scheda-bambino/bambino.service';

export interface AnagraficaDialogData {
  child: ChildrenDTO;
  linee: string[];
}

@Component({
  selector: 'app-dialog-anagrafica',
  templateUrl: './dialog-anagrafica.component.html',
  styleUrls: ['./dialog-anagrafica.component.css']
})
export class DialogAnagraficaComponent implements OnInit {

  andata: Fermata[] = [];
  ritorno: Fermata[] = [];
  selectedAndata: Fermata = { id: 0, nome: '', orario: '' };
  selectedRitorno: Fermata = { id: 0, nome: '', orario: '' };

  anagraficaForm = this.fb.group({
    anadataSelect: ['', [Validators.required]],
    ritornoSelect: ['', [Validators.required]],
    andataFermataSelect: ['', [Validators.required]],
    ritornoFermataSelect: ['', [Validators.required]],
  });

  constructor(
    public dialogRef: MatDialogRef<DialogAnagraficaComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AnagraficaDialogData,
    private apiService: ApiService,
    private fb: FormBuilder
  ) {

  }

  ngOnInit() {
  }

  changeFermateAndata(emit: any) {
    const linea = emit.value;
    this.apiService.getStopsByLine(linea).subscribe((data) => { this.andata = data.andata }, (error) => console.log(error));
  }

  changeFermateRitorno(emit: any) {
    const linea = emit.value;
    this.apiService.getStopsByLine(linea).subscribe((data) => { this.ritorno = data.ritorno }, (error) => console.log(error));
  }

  selectAndata(emit: any) {
    const fermata = emit.value;
    this.selectedAndata = fermata;
  }
  
  selectRitorno(emit: any) {
    const fermata = emit.value;
    this.selectedRitorno = fermata;
  }

  cancel() {
    this.dialogRef.close();
  }

  submit() {
    this.data.child.idFermataAndata = this.selectedAndata.id;
    this.data.child.idFermataRitorno = this.selectedRitorno.id;
    this.dialogRef.close({data: this.data});
  }

}
