import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {ApiService} from '../api.service';

@Component({
  selector: 'app-disponibilita',
  templateUrl: './disponibilita.component.html',
  styleUrls: ['./disponibilita.component.scss']
})
export class DisponibilitaComponent implements OnInit {

  linee$: Observable<string[]>;

  constructor(private mongoService: ApiService) {
    this.linee$ = this.mongoService.getLinee();

  }

  ngOnInit() {
  }

}
