import { Component, OnInit } from '@angular/core';
import {Observable} from 'rxjs';
import {ApiService} from '../api.service';

@Component({
  selector: 'app-turni',
  templateUrl: './turni.component.html',
  styleUrls: ['./turni.component.scss']
})
export class TurniComponent implements OnInit {

  linee$: Observable<string[]>;

  constructor(private mongoService: ApiService) {
    this.linee$ = this.mongoService.getLinee();

  }

  ngOnInit() {
  }

}
