import { Component, OnInit } from '@angular/core';
import {Alunno, Linea, Prenotazioni} from '../lineDetails';
import {MongoService} from '../mongo.service';

@Component({
  selector: 'app-presenze',
  templateUrl: './presenze.component.html',
  styleUrls: ['./presenze.component.css']
})
export class PresenzeComponent implements OnInit {

  opened: boolean;
  title = 'PRESENZE';
  linee: Linea[] = [];
  verso: string[] = ['Andata', 'Ritorno'];
  selectedVerso: string;
  selectedLinea: number;
  toolBarFilled: boolean;
  reservations: Prenotazioni[];
  date: Date;
  logo: any = '../assets/svg/logo.svg';
  stop: any = '../assets/svg/cross.svg';
  next: any = '../assets/svg/next.svg';
  previous: any = '../assets/svg/previous.svg';
  cross: any = '../assets/svg/cross.svg';


  constructor(private mongoService: MongoService) {
    this.linee = mongoService.getLinee();
    this.reservations = [];
    this.date = new Date();
  }

  /** Vogliamo riempire il campo prenotazione solo quando un utente selezione una line_id ed una data */
  fillPrenotazione() {
    if (this.date != null && this.selectedLinea) {
      if (!this.selectedVerso) {
        this.selectedVerso = this.verso[0];
      }
      this.toolBarFilled = true;
      this.reservations = this.mongoService.getPrenotazioneByLineaAndDateAndVerso(this.selectedLinea, this.date, this.selectedVerso);
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

  ngOnInit(): void {
  }

}