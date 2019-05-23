import {Component} from '@angular/core';
import {MongoService} from './mongo.service';
import {Alunno, Linea, LineDetails, Prenotazioni} from './lineDetails';
import {FormControl} from '@angular/forms';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  opened: boolean;
  title = 'PRESENZE';
  linee: Linea[] = [];
  verso: string[] = ['Andata', 'Ritorno'];
  selectedVerso: string;
  selectedLinea: number;
  toolBarFilled: boolean;
  reservations: Prenotazioni[];
  date: Date;

  constructor(private mongoService: MongoService) {
    this.linee = mongoService.getLinee();
    this.reservations = [];
    this.date = new Date();
  }

  /** Vogliamo riempire il campo prenotazione solo quando un utente selezione una line_id ed una data */
  fillPrenotazione() {
    if (this.date != null && this.selectedLinea) {
      this.toolBarFilled = true;
      this.selectedVerso = this.verso[0];
      this.reservations = this.mongoService.getPrenotazioneByLineaAndDateAndVerso(this.selectedLinea, this.date, this.selectedVerso);
    }
  }

  togglePresenza(id: number, alunno: Alunno) {
    const al = this.reservations.find( p => p.fermata.id === id).alunni.find(a => a === alunno);
    al.presenza = !al.presenza;
  }

  presente(id: number, alunno: Alunno): boolean {
    return this.reservations.find( p => p.fermata.id === id).alunni.find(a => a === alunno).presenza;
  }

}
