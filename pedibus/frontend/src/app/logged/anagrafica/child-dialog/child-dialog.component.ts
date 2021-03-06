import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {ChildrenDTO} from '../../genitore/dtos';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {Fermata, StopsByLine} from '../../line-details';
import {forkJoin, Observable} from 'rxjs';
import {AnagraficaDialogService, ForkJoinRes} from '../anagrafica-dialog.service';

export interface DialogData {
  child: ChildrenDTO;
}

@Component({
  selector: 'app-child-dialog',
  templateUrl: './child-dialog.component.html',
  styleUrls: ['./child-dialog.component.css']
})
export class ChildDialogComponent implements OnInit {
  profileForm = new FormGroup({
    name: new FormControl('', Validators.required),
    surname: new FormControl('', Validators.required),
    codiceFiscale: new FormControl('',
      [Validators.required, Validators.pattern(/^[a-zA-Z]{6}[0-9]{2}[a-zA-Z][0-9]{2}[a-zA-Z][0-9]{3}[a-zA-Z]$/i)]),
    idLineaAndata: new FormControl('', Validators.required),
    idFermataAndata: new FormControl('', Validators.required),
    idLineaRitorno: new FormControl('', Validators.required),
    idFermataRitorno: new FormControl('', Validators.required)
  });
  observableLines: Observable<ForkJoinRes>;
  lineeObs: Observable<Map<string, StopsByLine>>;
  andataSelected: Fermata[];
  ritornoSelected: Fermata[];
  arrayLine: StopsByLine[];
  mapLine: Map<string, StopsByLine>;
  private cfOld: string;
  private addingChild = false;

  constructor(
    private anagraficaDialogService: AnagraficaDialogService,
    public dialogRef: MatDialogRef<ChildDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit(): void {
    if (this.data) {
      this.cfOld = this.data.child.codiceFiscale;
      this.profileForm.patchValue({
        name: this.data.child.name,
        surname: this.data.child.surname,
        codiceFiscale: this.data.child.codiceFiscale,
        idFermataAndata: this.data.child.idFermataAndata,
        idFermataRitorno: this.data.child.idFermataRitorno
      });

      /**
       * Ottieni informazioni sulle linee e sulle fermate di default
       */
      this.observableLines = forkJoin({
        lines: this.anagraficaDialogService.getAllLinesInfo(),
        andata: this.anagraficaDialogService.getLineInfo(this.data.child.idFermataAndata),
        ritorno: this.anagraficaDialogService.getLineInfo(this.data.child.idFermataRitorno)
      });
      this.observableLines.subscribe((res) => {
        this.mapLine = res.lines;
        this.arrayLine = Array.from(this.mapLine.values());
        this.profileForm.patchValue({
          idLineaAndata: res.andata.id,
          idLineaRitorno: res.ritorno.id,
          idFermataAndata: this.data.child.idFermataAndata,
          idFermataRitorno: this.data.child.idFermataRitorno
        });
      }, (error => {
        // Gestito da Interceptor
      }));
    } else {
      this.lineeObs = this.anagraficaDialogService.getAllLinesInfo();
    }
  }

  /**
   * Ottieni andata
   * @param details mappa linee
   * @param value valore
   */
  getAndata(details: Map<string, StopsByLine>, value: any) {
    if (value) {
      return Array.from(details.get(value).andata);
    } else {
      return Array.from([]);
    }
  }

  /**
   * Ottieni ritorno
   * @param details mappa linee
   * @param value valore
   */
  getRitorno(details: Map<string, StopsByLine>, value: any) {
    if (value) {
      return Array.from(details.get(value).ritorno);
    } else {
      return Array.from([]);
    }
  }

  /**
   * Ottieni array delle linee da mappa
   * @param details mappa delle linee
   */
  getLines(details: Map<string, StopsByLine>) {
    return Array.from(details.values());
  }

  /**
   * Cambia andata
   * @param details mappa delle linee
   * @param value valore
   */
  changeAndata(details: any, value: any) {
    this.profileForm.patchValue({
      idFermataAndata: undefined
    });
  }

  /**
   * Cambia andata
   * @param details mappa delle linee
   * @param value valore
   */
  changeRitorno(details: any, value: any) {
    this.profileForm.patchValue({
      idFermataRitorno: undefined
    });
  }

  /**
   * Aggiungi Bambino
   */
  addChild() {
    if (!this.addingChild) {
      const child: ChildrenDTO = this.profileForm.value;
      this.addingChild = true;
      this.anagraficaDialogService.addChild(child).subscribe((ch) => {
        this.addingChild = false;
        this.dialogRef.close();
      }, error => {
        this.addingChild = false;
      });
    }
  }

  /**
   * Aggiorna Bambino
   */
  updateChild() {
    if (!this.addingChild) {
      const child: ChildrenDTO = this.profileForm.value;
      this.addingChild = true;
      this.anagraficaDialogService.updateChild(this.cfOld, child).subscribe((ch) => {
        this.addingChild = false;
        this.dialogRef.close();
      }, error => {
        // Gestito da Interceptor
        this.addingChild = false;
      });
    }
  }
}
