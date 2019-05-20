import {Component} from '@angular/core';
import {MongoService} from './mongo.service';
import {Prenotazione} from './prenotazione';
import {FormControl} from '@angular/forms';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  defaultValueIfNullPipe = 'NessunaCorsa';
  title = 'PRESENZE';
  linee: string[] = [];
  verso: string[] = ['andata', 'ritorno'];
  selectedVerso: string = this.verso[0];
  selectedLinea: string;
  reservations: Prenotazione;
  presenze: { id: number, alunni: string[] }[] = [];
  date: Date;

  constructor(private mongoService: MongoService) {
    this.linee = mongoService.getLinee();
    this.reservations = mongoService.getReservation();
    this.date = new Date();
  }

  /** Vogliamo riempire il campo prenotazione solo quando un utente selezione una linea ed una data */
  public fillPrenotazione() {
    if (this.date != null && this.selectedLinea) {
      // todo: memorizzare return in prenotazione
      this.mongoService.getPrenotazioneByLineaAndDate(this.selectedLinea, this.date);
    }
  }

  public togglePresenza(id: number, nomeAlunno: string) {
    // se trovo id ed alunno
    console.log(id);
    const index = this.presenze.findIndex(x => x.id === id && x.alunni.includes(nomeAlunno));
    if (index > -1) {
      console.log('>-1');
      const presenza = this.presenze[index];
      const alunnoIndex = presenza.alunni.indexOf(nomeAlunno);
      if (alunnoIndex > -1) {
        presenza.alunni.splice(alunnoIndex, 1);
      } else {
        presenza.alunni.push(nomeAlunno);
      }
    } else {
      console.log('-1');
      this.presenze.push({id, alunni: [nomeAlunno]});
    }
  }

  public presente(id: number, nomeAlunno: string): boolean {
    return this.presenze.some(x => x.id === id && x.alunni.includes(nomeAlunno));
  }


}
