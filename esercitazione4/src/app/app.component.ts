import {Component} from '@angular/core';
import {MongoService} from './mongo.service';
import {Prenotazione} from './prenotazione';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  title = 'PRESENZE';
  linee: string[] = [];
  verso: string[] = ['andata', 'ritorno'];
  selectedVerso: string;
  reservations: Prenotazione;
  presenze: { fermata: string, alunni: string[] }[] = [];

  constructor(private mongoService: MongoService) {
    this.linee = mongoService.getLinee();
    this.reservations = mongoService.getReservation();
  }

  togglePresenza(fermata: string, nomeAlunno: string) {
    // presenze ancora vuote
    if (this.presenze.length === 0) {
      this.presenze.push({
        fermata, alunni: [nomeAlunno]
      });
      return;
    }
    let trovoFermata = false;
    for (const presenza of this.presenze) {
      if (presenza.fermata === fermata) {
        trovoFermata = true;
        if (!this.presente(nomeAlunno)) {
          presenza.alunni.push(nomeAlunno);
        } else {
          const index = presenza.alunni.indexOf(nomeAlunno);
          presenza.alunni.splice(index, 1);
        }
      }
    }
    if (!trovoFermata) {
      this.presenze.push({
        fermata, alunni: [nomeAlunno]
      });
    }
  }

  presente(nomeAlunno: string): boolean {
    let trovato = false;
    for (const presenza of this.presenze) {
      for (const alunno of presenza.alunni) {
        if (alunno === nomeAlunno) {
          trovato = true;
        }
      }
    }
    return trovato;
  }


}
