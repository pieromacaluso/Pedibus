import { Component, OnInit } from '@angular/core';
import { NotificheService } from './notifiche.service';
import { Notifica } from './dtos';
import { Observable } from 'rxjs';
import { DataShareService } from 'src/app/data-share.service';
import {PageEvent} from '@angular/material';

@Component({
  selector: 'app-notifiche',
  templateUrl: './notifiche.component.html',
  styleUrls: ['./notifiche.component.scss']
})
export class NotificheComponent implements OnInit {

  notifiche$: Observable<Notifica[]>;
  countNonLette = 0;


  constructor(public notificheService: NotificheService, private dataService: DataShareService) {
    this.notifiche$ = dataService.comunicazioni;
  }

  ngOnInit() {

  }

  readNotifica(idNotifica: string) {
    this.notificheService.deleteNotifica(idNotifica);
  }

}
