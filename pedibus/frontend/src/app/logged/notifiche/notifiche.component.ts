import { Component, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { NotificheService } from './notifiche.service';
import { Notifica } from './dtos';

@Component({
  selector: 'app-notifiche',
  templateUrl: './notifiche.component.html',
  styleUrls: ['./notifiche.component.css']
})
export class NotificheComponent implements OnInit {

  subs: Subscription[] = [];
  comunicazioni: Notifica[] = [];
  countNonLette = 0;

  constructor(private notificheService: NotificheService) {
  }

  ngOnInit() {
    this.notificheService.getNotifiche(this.comunicazioni, this.countNonLette);
    this.notificheService.watchNotifiche(this.comunicazioni, this.countNonLette);
  }

  readNotifica(idNotifica: string) {
    this.notificheService.deleteNotifica(this.comunicazioni, idNotifica);
  }

}
