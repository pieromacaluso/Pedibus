import { Component, OnInit } from '@angular/core';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Message } from '@stomp/stompjs';
import { Subscription } from 'rxjs';
import { AuthService } from 'src/app/registration/auth.service';
import { DataShareService } from 'src/app/data-share.service';

const mapRoleNotifiche = [
  { role: "ROLE_ADMIN", notifiche: ["turno-confermato"] },
  { role: "", notifiche: [] }
]

/* COMPONENTE CHE SOSTANZIALMENTE DEVE SOMIGLIARE ALLA CAMPANELLINA
   DI NOTIFICHE DI FACEBOOK 
*/

@Component({
  selector: 'app-notifiche',
  templateUrl: './notifiche.component.html',
  styleUrls: ['./notifiche.component.css']
})
export class NotificheComponent implements OnInit {

  subs: Subscription[];
  comunicazioni: string[];
  countNonLette = 0;

  constructor(private rxStompService: RxStompService, private authService: AuthService,
      private dataService: DataShareService) {
    this.watchNotifiche();
  }

  ngOnInit() {
  }

  watchNotifiche() {
    for (let role of this.authService.getRoles()) {
      const element = mapRoleNotifiche.find(el => el.role === role);
      if (element) {
        element.notifiche.forEach(notifica => {
          let sub = new Subscription();
          this.subs.push(sub);
          if (sub) sub.unsubscribe();
          sub = this.rxStompService.watch(notifica)
            .subscribe((message: Message) => {
              this.countNonLette++;
              this.comunicazioni.push(JSON.parse(message.body));
              this.dataService.updateNotifiche(this.comunicazioni);
              console.log("Messaggio da broker:", JSON.parse(message.body));
            });
        });
      }
    }
  }

  /**
   * Questo metodo viene invocato quando un utente legge una
   * (o tutte?) notifica nella pagina comunicazioni. Il metodo
   * deve essere collegato quindi ad un opportuno metodo di
   * Output di @ComunicazioniComponent 
   * @param event evento lanciato da @ComunicazioniComponent
   */

  notificaLetta(event: any) {
    this.countNonLette--; // settare a 0 in caso di lettura notifiche in un sol colpo
    // TODO: implementare logica di notifica letta qui
  }

}
