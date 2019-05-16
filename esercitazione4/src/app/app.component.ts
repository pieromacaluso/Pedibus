import {Component} from '@angular/core';
import {Linea} from './linea';
import {MongoService} from './mongo.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'esercitazione4';
  linee: Linea[] = [];

  constructor(private mongoService: MongoService) {
    this.linee = mongoService.getLinee();
  }

}
