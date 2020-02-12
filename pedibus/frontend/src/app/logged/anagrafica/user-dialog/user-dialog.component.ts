import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {ChildrenDTO} from '../../genitore/dtos';
import {UserDTO} from '../dtos';
import {MAT_DIALOG_DATA, MatCheckboxChange, MatDialogRef} from '@angular/material';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AnagraficaDialogService} from '../anagrafica-dialog.service';
import {Observable} from 'rxjs';
import {StopsByLine} from '../../line-details';
import {animate, style, transition, trigger} from '@angular/animations';
import {flatMap} from 'rxjs/operators';

export interface DialogData {
  user: UserDTO;
}

@Component({
  selector: 'app-user-dialog',
  templateUrl: './user-dialog.component.html',
  styleUrls: ['./user-dialog.component.css']
})
export class UserDialogComponent implements OnInit, OnDestroy {
  profileForm = new FormGroup({
    name: new FormControl('', Validators.required),
    surname: new FormControl('', Validators.required),
    userId: new FormControl('', [Validators.required, Validators.email]),
    user: new FormControl(),
    guide: new FormControl(),
    lineAdmin: new FormControl(),
  });
  oldEmail: string;
  childObs: Map<string, Observable<ChildrenDTO>>;
  lines: Observable<Map<string, StopsByLine>>;
  childSearchObs: Observable<ChildrenDTO[]>;
  userDTOres: UserDTO;
  addingUser = false;

  constructor(
    private anagraficaDialogService: AnagraficaDialogService,
    public dialogRef: MatDialogRef<UserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
    this.userDTOres = {
      childIdList: [],
      lineaIdList: [],
      name: '',
      roleIdList: [],
      surname: '',
      userId: '',
    };
    this.childObs = new Map<string, Observable<ChildrenDTO>>();
    this.lines = this.anagraficaDialogService.getAllLinesInfo();
    this.childSearchObs = this.anagraficaDialogService.subscribeKeyword().pipe(
      flatMap((res) => {
        return this.anagraficaDialogService.getListChildKey(res);
      }));
    if (this.data) {
      this.userDTOres = this.data.user;
      this.userDTOres.childIdList.forEach((value => {
        this.childObs.set(value, this.anagraficaDialogService.childDetails(value));
      }));
      this.oldEmail = this.userDTOres.userId;
      this.profileForm.patchValue({
        name: this.userDTOres.name,
        surname: this.userDTOres.surname,
        userId: this.userDTOres.userId,
        user: this.userDTOres.roleIdList.includes('ROLE_USER'),
        guide: this.userDTOres.roleIdList.includes('ROLE_GUIDE'),
        lineAdmin: this.userDTOres.roleIdList.includes('ROLE_ADMIN'),
      });
    }
  }

  /**
   * Aggiorna Utente
   */
  updateUserDialog() {
    if (!this.addingUser) {
      this.addingUser = true;
      const user: UserDTO = this.userDTOres;
      user.name = this.profileForm.get('name').value;
      user.surname = this.profileForm.get('surname').value;
      user.userId = this.profileForm.get('userId').value;
      user.childIdList = this.userDTOres.childIdList;
      user.lineaIdList = this.userDTOres.lineaIdList;
      user.roleIdList = [];
      if (this.profileForm.get('user').value) {
        user.roleIdList.push('ROLE_USER');
      }
      if (this.profileForm.get('guide').value) {
        user.roleIdList.push('ROLE_GUIDE');
      }
      if (this.profileForm.get('lineAdmin').value) {
        user.roleIdList.push('ROLE_ADMIN');
      }

      if (this.data) {
        this.anagraficaDialogService.updateUser(this.oldEmail, user).subscribe((u) => {
          this.addingUser = false;
          this.dialogRef.close();
        }, error => {
          // Gestito da Interceptor
          this.addingUser = false;
        });
      } else {
        this.anagraficaDialogService.createUser(user).subscribe((u) => {
          this.addingUser = false;
          this.dialogRef.close();
        }, error => {
          this.addingUser = false;
          // Gestito da Interceptor
        });
      }
    }
  }

  /**
   * Cambia la lista delle linee
   * @param $event evento di modifica
   * @param id id della linea aggiunta o tolta
   */
  changeLineList($event: MatCheckboxChange, id: string) {
    if ($event.checked) {
      this.userDTOres.lineaIdList.push(id);
    } else {
      const index = this.userDTOres.lineaIdList.indexOf(id, 0);
      if (index > -1) {
        this.userDTOres.lineaIdList.splice(index, 1);
      }
    }
  }

  /**
   * Ottieni array da mappa
   * @param linee mappa linee
   */
  getArray(linee: Map<string, StopsByLine>) {
    return Array.from(linee.values());
  }

  /**
   * Emetti i nuovi dati ricaricando la pagina
   * @param value valore
   */
  newData(value: string) {
    this.anagraficaDialogService.emitKeyword(value);
  }

  ngOnDestroy(): void {
  }

  /**
   * Controlla se già selezionato
   * @param codiceFiscale codice fiscale
   */
  alreadySelected(codiceFiscale: string) {
    return this.userDTOres.childIdList.includes(codiceFiscale);
  }

  /**
   * Aggiungi bambino
   * @param codiceFiscale codice fiscale
   */
  addChild(codiceFiscale: string) {
    this.userDTOres.childIdList.push(codiceFiscale);
    this.childObs.set(codiceFiscale, this.anagraficaDialogService.childDetails(codiceFiscale));
  }

  /**
   * Rimuovi bambino
   * @param codiceFiscale codice fiscale
   */
  removeChild(codiceFiscale: string) {
    const index = this.userDTOres.childIdList.indexOf(codiceFiscale, 0);
    if (index > -1) {
      this.userDTOres.childIdList.splice(index, 1);
    }
    this.childObs.delete(codiceFiscale);
  }

  /**
   * Ottieni array dei bambini
   */
  getArrayChild() {
    return Array.from(this.childObs.values());
  }

  /**
   * Cambia linea amministratore
   * @param $event evento checkbox
   */
  lineAdminChange($event: MatCheckboxChange) {
    if (!$event.checked) {
      this.userDTOres.lineaIdList = [];
    }
  }

  /**
   * E' master della linea?
   * @param line struttura linea
   */
  isMaster(line: StopsByLine) {
    return line.master === this.userDTOres.userId;
  }

  /**
   * E' amministratore master di una linea?
   * @param linee mappa delle linee
   */
  isMasterAdmin(linee: Map<string, StopsByLine>) {
    for (const lineI of Array.from(linee.values())) {
      if (this.isMaster(lineI)) {
        return true;
      }
    }
    return false;
  }
}
