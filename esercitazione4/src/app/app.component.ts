import {Component} from '@angular/core';
import {MongoService} from './mongo.service';
import {AlunniPerFermata, Alunno, LineReservation} from './lineDetails';
import {FormControl} from '@angular/forms';
import {Line} from 'tslint/lib/verify/lines';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'PRESENZE';
  verso: string[] = ['Andata', 'Ritorno'];
  selectedVerso: string;
  selectedLinea: string;
  toolBarFilled: boolean;
  reservations: AlunniPerFermata[];
  date: Date;
  stop: any = '../assets/svg/cross.svg';
  next: any = '../assets/svg/next.svg';
  previous: any = '../assets/svg/previous.svg';
  cross: any = '../assets/svg/cross.svg';
  linee: string[];


  constructor(private mongoService: MongoService) {
    this.reservations = [];
    this.date = new Date();
    this.mongoService.getLinee().subscribe(value => {
      this.linee = value as string[];
      this.selectedLinea = this.linee[0];
    }, error1 => {
      console.log(error1);
    });
    this.selectedVerso = 'Andata';
    this.fillPrenotazione();
  }

  /** Vogliamo riempire il campo prenotazione solo quando un utente selezione una line_id ed una data */
  fillPrenotazione() {
    if (this.date != null && this.selectedLinea) {
      if (!this.selectedVerso) {
        this.selectedVerso = this.verso[0];
      }
      this.toolBarFilled = true;
      this.mongoService.getPrenotazioneByLineaAndDateAndVerso(this.selectedLinea, this.date).subscribe(value => {
        let lineReservation: LineReservation;
        lineReservation = value as LineReservation;
        // tslint:disable-next-line:max-line-length
        this.reservations = this.selectedVerso === 'Andata' ? lineReservation.alunniPerFermataAndata : lineReservation.alunniPerFermataRitorno;
      }, error1 => {
        console.log(error1);
      });
    }
  }

  togglePresenza(id: number, alunno: Alunno) {
    const al = this.reservations.find(p => p.fermata.id === id).alunni.find(a => a === alunno);
    al.presenza = !al.presenza;
  }

  presente(id: number, alunno: Alunno): boolean {
    return this.reservations.find(p => p.fermata.id === id).alunni.find(a => a === alunno).presenza;
  }

  modifyDate(days: number) {
    const nextDate = new Date(this.date);
    nextDate.setDate(nextDate.getDate() + days);
    this.date = nextDate;
    this.fillPrenotazione();
  }

  sortedAlunni(alu: Alunno[]) {
    return alu.sort((a, b) => {
      if (a.surname < b.surname) {
        return -1;
      } else if (b.surname < a.surname) {
        return +1;
      } else {
        if (a.name < b.name) {
          return -1;
        } else if (b.name < a.name) {
          return +1;
        }
        return 0;
      }
    });
  }
}
