import { Component, OnInit } from '@angular/core';
import { NotificheService } from './notifiche.service';
import { Notifica } from './dtos';
import { Observable } from 'rxjs';
import { DataShareService } from 'src/app/data-share.service';

@Component({
  selector: 'app-notifiche',
  templateUrl: './notifiche.component.html',
  styleUrls: ['./notifiche.component.scss']
})
export class NotificheComponent implements OnInit {

  notifiche: Observable<Notifica[]>;
  countNonLette = 0;

  constructor(private notificheService: NotificheService, private dataService: DataShareService) {
    this.notifiche = this.dataService.comunicazioni;
  }

  ngOnInit() {
    this.notificheService.getNotifiche(this.countNonLette);
    this.notificheService.watchNotifiche(this.countNonLette);
  }

  readNotifica(idNotifica: string) {
    this.notificheService.deleteNotifica(idNotifica);
  }

}
