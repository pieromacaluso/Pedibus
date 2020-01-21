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
    guide: new FormControl( ),
    lineAdmin: new FormControl(),
    sysAdmin: new FormControl(),
  });
  oldEmail: string;
  childObs: Map<string, Observable<ChildrenDTO>>;
  lines: Observable<Map<string, StopsByLine>>;
  childSearchObs: Observable<ChildrenDTO[]>;
  userDTOres: UserDTO;

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
        sysAdmin: this.userDTOres.roleIdList.includes('ROLE_SYSTEM-ADMIN'),
      });
    }
  }

  updateUserDialog() {
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
    if (this.profileForm.get('sysAdmin').value) {
      user.roleIdList.push('ROLE_SYSTEM-ADMIN');
    }
    if (this.data) {
      this.anagraficaDialogService.updateUser(this.oldEmail, user).subscribe((u) => {
        // TODO: Gestisci successo
        this.dialogRef.close();
      }, error => {
        // TODO: Gestisci errore.
      });
    } else {
      this.anagraficaDialogService.createUser(user).subscribe((u) => {
        // TODO: Gestisci successo
        this.dialogRef.close();
      }, error => {
        // TODO: Gestisci errore.
      });
    }
  }

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

  getArray(linee: Map<string, StopsByLine>) {
    return Array.from(linee.values());
  }

  newData(value: string) {
    this.anagraficaDialogService.emitKeyword(value);
  }

  ngOnDestroy(): void {
  }

  alreadySelected(codiceFiscale: string) {
    return this.userDTOres.childIdList.includes(codiceFiscale);
  }

  addChild(codiceFiscale: string) {
    this.userDTOres.childIdList.push(codiceFiscale);
    this.childObs.set(codiceFiscale, this.anagraficaDialogService.childDetails(codiceFiscale));
  }

  removeChild(codiceFiscale: string) {
    const index = this.userDTOres.childIdList.indexOf(codiceFiscale, 0);
    if (index > -1) {
      this.userDTOres.childIdList.splice(index, 1);
    }
    this.childObs.delete(codiceFiscale);
  }

  getArrayChild() {
    return Array.from(this.childObs.values());
  }

  lineAdminChange($event: MatCheckboxChange) {
    if (!$event.checked) {
      this.userDTOres.lineaIdList = [];
    }
  }
}
