import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { RxStompService } from '@stomp/ng2-stompjs';
import { AuthService } from 'src/app/registration/auth.service';
import { DataShareService } from 'src/app/data-share.service';
import { Subscription } from 'rxjs';
import { Message } from '@stomp/stompjs';
import { ApiService } from '../api.service';
import { Notifica } from './dtos';

const mapRoleNotifiche = [
  { role: "ROLE_SYSTEM-ADMIN", notifiche: ["disponibilita", "reminder-turno"]},
  { role: "ROLE_ADMIN", notifiche: ["turno-confermato", "turno-chiuso", "turno-aperto"] },
  { role: "ROLE_GUIDE", notifiche: ["turno-confermato", "turno-chiuso", "turno-aperto"] },
  { role: "ROLE_USER", notifiche: ["handled"] }
]

@Injectable({
  providedIn: 'root'
})
export class NotificheService {

  baseURL = environment.baseURL;
  subs: Subscription[];

  constructor(private rxStompService: RxStompService, private authService: AuthService,
    private dataService: DataShareService, private apiService: ApiService) {
  }

  getNotifiche(comunicazioni: Notifica[], countNonLette: number) {
    this.apiService.getNotificheNonLette(this.authService.getUsername()).subscribe((notifiche) => {
      comunicazioni.concat(notifiche); }, (err) => console.log(err));
  }

  watchNotifiche(comunicazioni: Notifica[], countNonLette: number) {
    for (let role of this.authService.getRoles()) {
      const element = mapRoleNotifiche.find(el => el.role === role);
      if (element) {
        element.notifiche.forEach(notifica => {
          let sub = new Subscription();
          this.subs.push(sub);
          if (sub) sub.unsubscribe();
          sub = this.rxStompService.watch(notifica)
            .subscribe((message: Message) => {
              countNonLette++;
              comunicazioni.push(JSON.parse(message.body));
              this.dataService.updateNotifiche(comunicazioni);
              console.log("Messaggio da broker:", JSON.parse(message.body));
            });
        });
      }
    }
  }

  deleteNotifica(comunicazioni: Notifica[], idNotifica: string) {
    this.apiService.deleteNotifica(idNotifica);
    const indexNotifica = comunicazioni.findIndex(n => n.id === idNotifica);
    comunicazioni.splice(indexNotifica, 1);
  }

  
}
