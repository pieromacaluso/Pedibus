import { Component, OnInit } from '@angular/core';
import { ChildrenDTO } from './dtos';
import { GenitoreService } from './genitore.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-genitore',
  templateUrl: './genitore.component.html',
  styleUrls: ['./genitore.component.css']
})
export class GenitoreComponent implements OnInit {

  figli: Observable<ChildrenDTO[]>;
  linee: Observable<string[]>;

  constructor(private genitoreService: GenitoreService) {
    this.figli = genitoreService.getChildren();
    this.linee = genitoreService.getLinee();
  }

  ngOnInit() {
  }

}
