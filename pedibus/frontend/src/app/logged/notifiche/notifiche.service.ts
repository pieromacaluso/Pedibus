import {Injectable} from '@angular/core';
import {environment} from 'src/environments/environment';
import {RxStompService} from '@stomp/ng2-stompjs';
import {AuthService} from 'src/app/registration/auth.service';
import {DataShareService} from 'src/app/data-share.service';
import {Subscription} from 'rxjs';
import {Message} from '@stomp/stompjs';
import {ApiService} from '../api.service';
import {switchMap, take, tap} from 'rxjs/operators';
import {Notifica} from './dtos';

const mapRoleNotifiche = [
  {role: 'ROLE_SYSTEM-ADMIN', notifiche: ['/user/notifiche', '/user/notifiche']},
  {role: 'ROLE_ADMIN', notifiche: ['/user/notifiche', '/user/notifiche', '/user/notifiche']},
  {role: 'ROLE_GUIDE', notifiche: ['/user/notifiche', '/user/notifiche']},
  {role: 'ROLE_USER', notifiche: ['/user/notifiche']}
];

@Injectable({
  providedIn: 'root'
})
export class NotificheService {

  baseURL = environment.baseURL;
  subs: Subscription[] = [];
  private websocketNotification: Subscription;
  private websocketNotificationTrash: Subscription;

  constructor(private rxStompService: RxStompService, private authService: AuthService,
              private dataService: DataShareService, private apiService: ApiService) {
    console.log('NOTIFICHE SERVICE STARTS', 'red');
    this.authService.newSession.subscribe((res) => {
      if (res !== null) {
        this.getNotifiche();
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

  getNotifiche() {
    const username = this.authService.getUsername();
    console.log('username:', username);
    this.apiService.getNotificheNonLette(username).pipe(take(1)).subscribe((notifiche) => {
      this.dataService.updateNotifiche(notifiche);
      console.log('nuove notifiche:', notifiche);
    }, (err) => console.log(err));
  }

  watchNotifiche() {
    this.websocketNotification = this.rxStompService.watch('/user/notifiche').subscribe(message => {
      const nuoveNotifiche = JSON.parse(message.body);
      this.dataService.updateNotifiche(nuoveNotifiche);
      console.log('nuove notifiche da broker:', nuoveNotifiche);
    });
    this.websocketNotificationTrash = this.rxStompService.watch('/user/notifiche/trash').subscribe(message => {
      const notificaToBeTrashed: Notifica = JSON.parse(message.body);
      this.dataService.removeNotifica(notificaToBeTrashed.idNotifica);
      console.log('eliminazione notifica da broker:', notificaToBeTrashed);
    });
  }

  deleteNotifica(idNotifica: string) {
    this.apiService.deleteNotifica(idNotifica).subscribe((el) => {
      console.log(el);
      this.dataService.removeNotifica(idNotifica);
    }, (err) => console.log(err));
  }


}
