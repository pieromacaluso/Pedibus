import {Component, Input, OnInit} from '@angular/core';
import {UserDTO} from '../../anagrafica/dtos';
import {ApiService} from '../../api.service';
import {AnagraficaService} from '../../anagrafica/anagrafica.service';
import {AuthService} from '../../../auth/auth.service';

export enum AdminGuide {
  Admin,
  Guide
}

@Component({
  selector: 'app-guide-entry',
  templateUrl: './guide-entry.component.html',
  styleUrls: ['./guide-entry.component.css']
})
export class GuideEntryComponent implements OnInit {

  @Input() user: UserDTO;
  @Input() type: AdminGuide;
  @Input() interaction: boolean;
  @Input() lineID: string;

  constructor(private anagraficaService: AnagraficaService, private authService: AuthService) {
  }

  ngOnInit() {
  }

  isAdmin() {
    return this.type === AdminGuide.Admin;
  }

  isGuide() {
    return this.type === AdminGuide.Guide;
  }

  deleteAdmin() {
    this.anagraficaService.changeAdmin(this.user, this.lineID, false)
      .subscribe((res) => {
        // TODO: gestisci successo
      }, error => {
        // TODO: gestisci fallimento
      });
  }

  addAdmin() {
    this.anagraficaService.changeAdmin(this.user, this.lineID, true)
      .subscribe((res) => {
        // TODO: gestisci successo
      }, error => {
        // TODO: gestisci fallimento
      });
  }

  isSelf() {
    return this.user.userId === this.authService.getUsername();
  }
}
