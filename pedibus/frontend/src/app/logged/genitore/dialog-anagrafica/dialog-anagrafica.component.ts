import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {ApiService} from '../../api.service';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {ChildrenDTO} from '../dtos';
import {Fermata, StopsByLine} from '../../line-details';
import {forkJoin, Observable} from 'rxjs';
import {finalize, first} from 'rxjs/operators';
import {Point} from 'geojson';
import * as moment from 'moment';


export interface AnagraficaDialogData {
  child: ChildrenDTO;
  linee: string[];
  defaultAndata: Observable<Fermata>;
  defaultRitorno: Observable<Fermata>;
  orarioAndata: string;
  orarioRitorno: string;
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
  stopsLocationA: Point[] = [];
  stopsDescriptionA: string[] = [];
  stopsLocationR: Point[];
  stopsDescriptionR: string[];


  anagraficaForm = new FormGroup({
    andataSelect: new FormControl('', Validators.required),
    ritornoSelect: new FormControl('', Validators.required),
    andataFermataSelect: new FormControl({value: '', disabled: true}, Validators.required),
    ritornoFermataSelect: new FormControl({value: '', disabled: true}, Validators.required),
  });

  loading = 0;
  selectedLineaAndata: any;
  selectedLineaRitorno: any;

  constructor(
    public dialogRef: MatDialogRef<DialogAnagraficaComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AnagraficaDialogData,
    private apiService: ApiService,
    private fb: FormBuilder
  ) {

  }

  ngOnInit() {
    const lines$ = [];
    this.data.linee.forEach((value, index) => {
      lines$.push(this.apiService.getStopsByLine(value).pipe(first()));
    });

    /**
     * Ottiene i dati riguardanti tutte le linee, dopodichè ottiene fermate di default che usa per riempire il form
     */
    forkJoin(lines$).pipe(finalize(() => {
      this.data.defaultAndata.subscribe((res) => {
        this.changeFermateAndata(res.idLinea);
        this.selectAndata(res.id);
        this.anagraficaForm.patchValue({
          andataSelect: res.idLinea,
          andataFermataSelect: res.id
        });
      });
      this.data.defaultRitorno.subscribe((res) => {
        this.changeFermateRitorno(res.idLinea);
        this.selectRitorno(res.id);
        this.anagraficaForm.patchValue({
          ritornoSelect: res.idLinea,
          ritornoFermataSelect: res.id
        });
      });
    })).subscribe((result: StopsByLine[]) => {
      result.forEach((value, index) => {
        this.lineData.set(value.id, value);
      });

    });
  }

  /**
   * Cambio delle fermate dell'andata
   * @param emit valore dell'evento
   */
  changeFermateAndata(emit: any) {
    const sameLine = this.selectedLineaAndata === emit;
    this.selectedLineaAndata = emit;
    this.anagraficaForm.patchValue({
      andataFermataSelect: undefined
    });
    this.selectedAndata = undefined;
    if (!sameLine) {
      this.andata = this.lineData.get(this.selectedLineaAndata).andata;
      this.stopsLocationA = [];
      this.stopsDescriptionA = [];
      for (const s of this.andata) {
        this.stopsLocationA.push(s.location);
        this.stopsDescriptionA.push(s.nome);
      }
    }

  }

  /**
   * Cambio delle fermate del ritorno
   * @param emit valore dell'evento
   */
  changeFermateRitorno(emit: any) {
    const sameLine = this.selectedLineaRitorno === emit;
    this.selectedLineaRitorno = emit;
    this.anagraficaForm.patchValue({
      ritornoFermataSelect: undefined
    });
    this.selectedRitorno = undefined;
    if (!sameLine) {
      this.ritorno = this.lineData.get(this.selectedLineaRitorno).ritorno;
      this.stopsLocationR = [];
      this.stopsDescriptionR = [];
      for (const s of this.ritorno) {
        this.stopsLocationR.push(s.location);
        this.stopsDescriptionR.push(s.nome);
      }
    }
  }

  /**
   * Selezione andata
   * @param emit valore dell'evento
   */
  selectAndata(emit: any) {
    const fermata = emit;
    this.selectedAndata = this.andata.find((el) => el.id === fermata);
  }

  /**
   * Selezione ritorno
   * @param emit valore dell'evento
   */
  selectRitorno(emit: any) {
    const fermata = emit;
    this.selectedRitorno = this.ritorno.find((el) => el.id === fermata);
  }

  /**
   * Selezione andata index
   * @param emit evento
   */
  selectAndataIndex(emit: any) {
    this.selectedAndata = this.andata[emit];
    this.anagraficaForm.patchValue({
      andataFermataSelect: this.selectedAndata.id
    });
  }

  /**
   * Selezione ritorno index
   * @param emit evento
   */
  selectRitornoIndex(emit: any) {
    this.selectedRitorno = this.ritorno[emit];
    this.anagraficaForm.patchValue({
      ritornoFermataSelect: this.selectedRitorno.id
    });
  }

  /**
   * Chiudi finestra di dialogo
   */
  cancel() {
    this.dialogRef.close();
  }

  /**
   * Sottomissione del form
   */
  submit() {
    this.data.child.idFermataAndata = this.selectedAndata.id;
    this.data.child.idFermataRitorno = this.selectedRitorno.id;
    this.data.orarioAndata = this.selectedAndata.orario;
    this.data.orarioRitorno = this.selectedRitorno.orario;
    this.dialogRef.close({data: this.data});
  }

  getDateMod() {
    return moment().add(1, 'days');
  }
}
