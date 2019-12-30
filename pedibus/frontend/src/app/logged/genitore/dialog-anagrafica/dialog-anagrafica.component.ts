import {Component, OnInit, Inject} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import {ApiService} from '../../api.service';
import {FormBuilder, Validators} from '@angular/forms';
import {ChildrenDTO} from '../dtos';
import {Fermata, StopsByLine} from '../../line-details';
import {BambinoService} from '../scheda-bambino/bambino.service';
import {Observable} from 'rxjs';
import {finalize, tap} from 'rxjs/operators';

export interface AnagraficaDialogData {
    child: ChildrenDTO;
    linee: string[];
    defaultAndata: Observable<Fermata>;
    defaultRitorno: Observable<Fermata>;
}

@Component({
    selector: 'app-dialog-anagrafica',
    templateUrl: './dialog-anagrafica.component.html',
    styleUrls: ['./dialog-anagrafica.component.scss']
})
export class DialogAnagraficaComponent implements OnInit {

    andata: Fermata[] = [];
    lineData: Map<string, StopsByLine> = new Map<string, StopsByLine>();
    ritorno: Fermata[] = [];
    selectedAndata: Fermata;
    selectedRitorno: Fermata;

    anagraficaForm = this.fb.group({
        andataSelect: ['', [Validators.required]],
        ritornoSelect: ['', [Validators.required]],
        andataFermataSelect: ['', [Validators.required]],
        ritornoFermataSelect: ['', [Validators.required]],
    });
    private loading = 0;

    constructor(
        public dialogRef: MatDialogRef<DialogAnagraficaComponent>,
        @Inject(MAT_DIALOG_DATA) public data: AnagraficaDialogData,
        private apiService: ApiService,
        private fb: FormBuilder
    ) {

    }

    ngOnInit() {
        this.data.linee.forEach((value, index) => {
            this.loading++;
            this.apiService.getStopsByLine(value).subscribe((data) => {
                this.lineData.set(value, data);
                this.loading--;
            }, (error) => console.log(error));
        });
        this.data.defaultAndata.subscribe((res) => {
            this.anagraficaForm.patchValue({
                andataSelect: res.idLinea,
                andataFermataSelect: res.id
            });
            this.changeFermateAndata(res.idLinea);
            this.selectAndata(res.id);

        });
        this.data.defaultRitorno.subscribe((res) => {
            this.anagraficaForm.patchValue({
                ritornoSelect: res.idLinea,
                ritornoFermataSelect: res.id
            });
            this.changeFermateRitorno(res.idLinea);
            this.selectRitorno(res.id);
        });
    }

    changeFermateAndata(emit: any) {
        const linea = emit;
        this.andata = this.lineData.get(linea).andata;
    }

    changeFermateRitorno(emit: any) {
        const linea = emit;
        this.ritorno = this.lineData.get(linea).ritorno;

    }

    selectAndata(emit: any) {
        const fermata = emit;
        this.selectedAndata = this.andata.find((el) => el.id === fermata);
    }

    selectRitorno(emit: any) {
        const fermata = emit;
        this.selectedRitorno = this.ritorno.find((el) => el.id === fermata);
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
