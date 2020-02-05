import {Component, OnInit, ViewChild} from '@angular/core';
import {ApiService} from '../api.service';
import {Observable, Subscription} from 'rxjs';
import {StopsByLine} from '../line-details';
import {UserDTO} from '../anagrafica/dtos';
import {MatSelect, MatSelectChange} from '@angular/material';
import {AnagraficaService} from '../anagrafica/anagrafica.service';
import {finalize, tap} from 'rxjs/operators';

@Component({
  selector: 'app-line-admin',
  templateUrl: './line-admin.component.html',
  styleUrls: ['./line-admin.component.css']
})
export class LineAdminComponent implements OnInit {
  // lines$: Observable<Map<string, StopsByLine>>;
  guide$: Observable<UserDTO[]>;
  selectedLine: StopsByLine;
  private updatesSub: Subscription;
  idSelectedLine: any;
  @ViewChild('matSelect', {static: false}) matSelect;
  lines: Map<string, StopsByLine>;

  constructor(private apiService: ApiService, private anagraficaService: AnagraficaService) {
  }

  ngOnInit() {
    this.apiService.getLineAdmin().subscribe((res) => {
      this.lines = res;
      this.selectedLine = Array.from(res.values())[0];
      if (this.matSelect) {
        this.matSelect.value = this.selectedLine.id;
      }
    });
    this.guide$ = this.apiService.getGuides();
    this.updatesSub = this.anagraficaService.watchUpdates().subscribe((res) => {
      this.guide$ = this.apiService.getGuides();
    });
  }

  lineChange($event: any, lines: Map<string, StopsByLine>) {
    this.selectedLine = lines.get($event);
  }

  isAlreadyAdmin(guide: UserDTO) {
    return guide.roleIdList.includes('ROLE_ADMIN') && guide.lineaIdList.includes(this.selectedLine.id);
  }

  isSysAdmin(guide: UserDTO) {
    return guide.roleIdList.includes('ROLE_SYSTEM-ADMIN');
  }

  getArrayLine(lines: Map<string, StopsByLine>) {
    return Array.from(lines.values());
  }

  isMaster(admin: UserDTO) {
    return this.selectedLine.master === admin.userId;
  }
}
