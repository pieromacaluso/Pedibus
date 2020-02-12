import {Component, OnInit, Input} from '@angular/core';
import {RxStompService} from '@stomp/ng2-stompjs';
import {Notifica} from '../dtos';
import {NotificheService} from '../notifiche.service';
import {AuthService} from '../../../auth/auth.service';

@Component({
  selector: 'app-notifica',
  templateUrl: './notifica.component.html',
  styleUrls: ['./notifica.component.scss']
})
export class NotificaComponent implements OnInit {

  loading: boolean;
  @Input() messageNumber: number;
  @Input() notifica: Notifica;
  tick: any = '../assets/svg/tick.svg';
  public now: Date = new Date();
  public notifDate: Date;


  constructor(private rxStompService: RxStompService, private notificheService: NotificheService, private authService: AuthService) {
  }

  ngOnInit() {
    this.notifDate = new Date(this.notifica.data);
    setInterval(() => {
      this.now = new Date();
    }, 1000);

  }

  showLoading() {
    return this.loading || !this.rxStompService.connected();
  }

  readNotifica() {
    this.notificheService.deleteNotifica(this.notifica.idNotifica);
  }

  islessThanAMinute(date: Date) {
    return this.now.getTime() - date.getTime() < (1000 * 60);
  }

  islessThanAnHour(date: Date) {
    return this.now.getTime() - date.getTime() < (1000 * 60 * 60);
  }

  islessThanADay(date: Date) {
    return this.now.getTime() - date.getTime() < (1000 * 60 * 60 * 24);
  }

  getDiffMin(date: Date) {
    return Math.floor((this.now.getTime() - date.getTime()) / (1000 * 60));
  }

  getDiffHour(date: Date) {
    return Math.floor((this.now.getTime() - date.getTime()) / (1000 * 60 * 60));
  }

}
