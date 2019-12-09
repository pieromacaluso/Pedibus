import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { RxStompService } from '@stomp/ng2-stompjs';
import { AuthService } from 'src/app/registration/auth.service';
import { DataShareService } from 'src/app/data-share.service';
import { Subscription } from 'rxjs';
import { Message } from '@stomp/stompjs';
import { ApiService } from '../api.service';

const mapRoleNotifiche = [
  { role: 'ROLE_SYSTEM-ADMIN', notifiche: ['/user/notifiche', '/user/notifiche']},
  { role: 'ROLE_ADMIN', notifiche: ['/user/notifiche', '/user/notifiche', '/user/notifiche'] },
  { role: 'ROLE_GUIDE', notifiche: ['/user/notifiche', '/user/notifiche'] },
  { role: 'ROLE_USER', notifiche: ['/user/notifiche'] }
];

@Injectable({
  providedIn: 'root'
})
export class NotificheService {

  baseURL = environment.baseURL;
  subs: Subscription[] = [];

  constructor(private rxStompService: RxStompService, private authService: AuthService,
              private dataService: DataShareService, private apiService: ApiService) {
  }

  getNotifiche(countNonLette: number) {
    const username = this.authService.getUsername();
    console.log('username:', username);
    this.apiService.getNotificheNonLette(username).subscribe((notifiche) => {
      this.dataService.updateNotifiche(notifiche);
      console.log('nuove notifiche:', notifiche);
    }, (err) => console.log(err));
  }

  watchNotifiche(countNonLette: number) {
    for (const role of this.authService.getRoles()) {
      const element = mapRoleNotifiche.find(el => el.role === role);
      if (element) {
        element.notifiche.forEach(notifica => {
          let sub = new Subscription();
          this.subs.push(sub);
          if (sub) { sub.unsubscribe(); }
          sub = this.rxStompService.watch(notifica)
            .subscribe((message: Message) => {
              countNonLette++;
              const nuoveNotifiche = JSON.parse(message.body);
              this.dataService.updateNotifiche(nuoveNotifiche);
              console.log('nuove notifiche da broker:', nuoveNotifiche);
            });
        });
      }
    }
  }

  deleteNotifica(idNotifica: string) {
    this.apiService.deleteNotifica(idNotifica).subscribe((el) => {
       console.log(el);
       this.dataService.removeNotifica(idNotifica);
    }, (err) => console.log(err));
  }


}
