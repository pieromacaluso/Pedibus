import {Injectable, OnInit} from '@angular/core';
import {environment} from 'src/environments/environment';
import {RxStompService} from '@stomp/ng2-stompjs';
import {AuthService} from 'src/app/auth/auth.service';
import {DataShareService} from 'src/app/data-share.service';
import {Subscription} from 'rxjs';
import {Message} from '@stomp/stompjs';
import {ApiService} from '../api.service';
import {switchMap, take, tap} from 'rxjs/operators';
import {Notifica} from './dtos';
import {PageEvent} from '@angular/material';

const mapRoleNotifiche = [
  {role: 'ROLE_SYSTEM-ADMIN', notifiche: ['/user/notifiche', '/user/notifiche']},
  {role: 'ROLE_ADMIN', notifiche: ['/user/notifiche', '/user/notifiche', '/user/notifiche']},
  {role: 'ROLE_GUIDE', notifiche: ['/user/notifiche', '/user/notifiche']},
  {role: 'ROLE_USER', notifiche: ['/user/notifiche']}
];

@Injectable({
  providedIn: 'root'
})
export class NotificheService implements OnInit {

  baseURL = environment.baseURL;
  subs: Subscription[] = [];
  private websocketNotification: Subscription;
  private websocketNotificationTrash: Subscription;
  len: number;
  pageIndex = 0;

  constructor(private rxStompService: RxStompService, private authService: AuthService,
              private dataService: DataShareService, private apiService: ApiService) {
    this.authService.newSession.subscribe((res) => {
      if (res !== '' && this.authService.isLoggedIn()) {
        this.getNotifiche(this.pageIndex);
        if (this.websocketNotification) {
          this.websocketNotification.unsubscribe();
          this.websocketNotificationTrash.unsubscribe();

        }
        this.watchNotifiche();
      } else {
        this.dataService.reset();
      }
    });
  }

  getNotifiche(page: number) {
    const username = this.authService.getUsername();
    this.apiService.getNotificheNonLette(username, page).pipe(take(1)).subscribe((notifiche) => {
      this.dataService.updateNotifiche(notifiche.content);
      this.len = notifiche.totalElements;
      this.dataService.updateTotal(this.len);
    }, (err) => {});
  }

  watchNotifiche() {
    this.websocketNotification = this.rxStompService.watch('/user/notifiche').subscribe(message => {
      this.getNotifiche(this.pageIndex);
    });
    this.websocketNotificationTrash = this.rxStompService.watch('/user/notifiche/trash').subscribe(message => {
      this.getNotifiche(this.pageIndex);
    });
  }

  deleteNotifica(idNotifica: string) {
    this.apiService.deleteNotifica(idNotifica).subscribe((el) => {
      this.getNotifiche(this.pageIndex);
    }, (err) => {});
  }


  cambiaPagina($event: PageEvent) {
    this.pageIndex = $event.pageIndex;
    this.getNotifiche(this.pageIndex);
  }

  ngOnInit(): void {
  }
}
