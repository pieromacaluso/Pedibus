import { Component, OnInit, Input } from '@angular/core';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Notifica } from '../dtos';
import { NotificheService } from '../notifiche.service';

@Component({
  selector: 'app-notifica',
  templateUrl: './notifica.component.html',
  styleUrls: ['./notifica.component.scss']
})
export class NotificaComponent implements OnInit {

  loading: boolean;
  @Input() messageNumber: number;
  @Input() notifica: Notifica;

  constructor(private rxStompService: RxStompService, private notificheService: NotificheService) { 

  }

  ngOnInit() {
  }

  showLoading() {
    return this.loading || !this.rxStompService.connected();
  }

  readNotifica() {
    this.notificheService.deleteNotifica(this.notifica.idNotifica);
  }

}
