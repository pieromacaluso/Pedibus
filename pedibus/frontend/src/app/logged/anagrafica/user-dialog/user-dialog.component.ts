import {Component, Inject, OnInit} from '@angular/core';
import {ChildrenDTO} from '../../genitore/dtos';
import {UserDTO} from '../dtos';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AnagraficaDialogService} from '../anagrafica-dialog.service';
import {Observable} from 'rxjs';

export interface DialogData {
  user: UserDTO;
}

@Component({
  selector: 'app-user-dialog',
  templateUrl: './user-dialog.component.html',
  styleUrls: ['./user-dialog.component.css']
})
export class UserDialogComponent implements OnInit {
  profileForm = new FormGroup({
    name: new FormControl('', Validators.required),
    surname: new FormControl('', Validators.required),
    userId: new FormControl('', Validators.required),
    user: new FormControl('', Validators.required),
    guide: new FormControl('', Validators.required),
    lineAdmin: new FormControl('', Validators.required),
    sysAdmin: new FormControl('', Validators.required),
  });
  private oldEmail: string;
  private childObs: Observable<ChildrenDTO>[];

  constructor(
    private anagraficaDialogService: AnagraficaDialogService,
    public dialogRef: MatDialogRef<UserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
    this.childObs = [];
    this.data.user.childIdList.forEach((value => {
      this.childObs.push(this.anagraficaDialogService.childDetails(value));
    }));
    this.oldEmail = this.data.user.userId;
    this.profileForm.patchValue({
      name: this.data.user.name,
      surname: this.data.user.surname,
      userId: this.data.user.userId,
      user: this.data.user.roleIdList.includes('ROLE_USER'),
      guide: this.data.user.roleIdList.includes('ROLE_GUIDE'),
      lineAdmin: this.data.user.roleIdList.includes('ROLE_ADMIN'),
      sysAdmin: this.data.user.roleIdList.includes('ROLE_SYSTEM-ADMIN'),
    });
  }

  updateUserDialog() {
    const user: UserDTO = this.data.user;
    user.name = this.profileForm.get('name').value;
    user.surname = this.profileForm.get('surname').value;
    user.userId = this.profileForm.get('userId').value;
    user.childIdList = this.data.user.childIdList;
    user.lineaIdList = this.data.user.lineaIdList;
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
    this.anagraficaDialogService.updateUser(this.oldEmail, user).subscribe((u) => {
      // TODO: Gestisci successo
      this.dialogRef.close();
    }, error => {
      // TODO: Gestisci errore.
    });
  }
}
