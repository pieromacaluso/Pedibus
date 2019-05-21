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
  title = 'PRESENZE';
  linee: Linea[] = [];
  verso: string[] = ['andata', 'ritorno'];
  selectedVerso: string = this.verso[0];
  selectedLinea: number;
  reservations: Prenotazioni[];
  presenze: { id: number, alunni: string[] }[] = [];
  date: Date;

  constructor(private mongoService: MongoService) {
    this.linee = mongoService.getLinee();
    // this.reservations = mongoService.getReservation();
    this.reservations = [];
    this.date = new Date();
  }

  /** Vogliamo riempire il campo prenotazione solo quando un utente selezione una line_id ed una data */
  fillPrenotazione() {
    if (this.date != null && this.selectedLinea && this.selectedVerso) {
      // todo: memorizzare return in prenotazione
      this.reservations = this.mongoService.getPrenotazioneByLineaAndDateAndVerso(this.selectedLinea, this.date, this.selectedVerso);
    }
  }

  togglePresenza(id: number, alunno: Alunno) {
    // se trovo id ed alunno
    console.log(id);
    const fermata = this.reservations.find(x => x.fermata.id === id);
    if (fermata !== undefined) {
      console.log(fermata.fermata.nome);
      const al = fermata.alunni.find(a => a === alunno);
      if (al !== undefined) {
        console.log(al.name + ' ' + al.presenza);
        al.presenza = !al.presenza;
      }
    }
  }

  presente(id: number, alunno: Alunno): boolean {
    const fermata = this.reservations.find(x => x.fermata.id === id);
    if (fermata !== undefined) {
      console.log(fermata.fermata.nome);
      const al = fermata.alunni.find(a => a === alunno);
      if (al !== undefined) {
        return al.presenza;
      }
    }
  }

}
