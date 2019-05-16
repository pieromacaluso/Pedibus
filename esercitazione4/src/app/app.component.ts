import {Component} from '@angular/core';
import {MongoService} from './mongo.service';
import {Prenotazione} from './prenotazione';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'esercitazione4';
  linee: string[] = [];
  reservations: Prenotazione;

  constructor(private mongoService: MongoService) {
    this.linee = mongoService.getLinee();
    this.reservations = mongoService.getReservation();
  }

}
