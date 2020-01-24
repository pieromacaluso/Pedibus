import {Component, OnInit} from '@angular/core';
import {ApiService} from '../api.service';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-presenze',
  templateUrl: './presenze.component.html',
  styleUrls: ['./presenze.component.css']
})
export class PresenzeComponent implements OnInit {

  // refactored
  linee$: Observable<string[]>;

  constructor(private mongoService: ApiService) {
    this.linee$ = this.mongoService.getLinee();
  }

  ngOnInit() {
  }
}
