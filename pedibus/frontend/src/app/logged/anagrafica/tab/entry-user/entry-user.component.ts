import {Component, Input, OnInit} from '@angular/core';
import {UserDTO} from '../../dtos';
import {AnagraficaService, RoleType} from '../../anagrafica.service';
import {ChildrenDTO} from '../../../genitore/dtos';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-entry-user',
  templateUrl: './entry-user.component.html',
  styleUrls: ['./entry-user.component.scss']
})
export class EntryUserComponent implements OnInit {

  @Input() user: UserDTO;
  private childObs: Observable<ChildrenDTO>[];

  constructor(private anagraficaService: AnagraficaService) { }

  ngOnInit() {
    this.childObs = [];
    this.user.childIdList.forEach((value, index) => {
      this.childObs.push(this.childDetails(value));
    });
  }

  printRole(role: any) {
    switch (role) {
      case RoleType.Admin:
        return 'Amministratore';
      case RoleType.User:
        return 'Utente';
      case RoleType.Guide:
        return 'Guida';
      case RoleType.Sys:
        return 'Amministratore di Sistema';
    }

  }

  childDetails(child: any) {
    return this.anagraficaService.getChild(child);
  }

  openDialog() {
    this.anagraficaService.openDialogUser(this.user);
  }
}
