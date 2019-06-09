import {Component, Input, OnInit, SimpleChange} from '@angular/core';
import {AlunniPerFermata, Alunno, PrenotazioneRequest} from '../../line-details';
import {SyncService} from '../sync.service';
import {ApiService} from '../../api.service';

@Component({
  selector: 'app-lista-prenotazioni',
  templateUrl: './lista-prenotazioni.component.html',
  styleUrls: ['./lista-prenotazioni.component.scss']
})
export class ListaPrenotazioniComponent implements OnInit {

  reservations: AlunniPerFermata[];
  cross: any = '../assets/svg/cross.svg';
  selectedVerso: string;

  constructor(private syncService: SyncService, private apiService: ApiService) {
    this.syncService.prenotazioneObs$.subscribe((prenotazione) => {
      if (prenotazione.linea && prenotazione.verso && prenotazione.data) {
        this.apiService.getPrenotazioneByLineaAndDateAndVerso(prenotazione.linea, prenotazione.data).subscribe((rese) => {
          this.selectedVerso = prenotazione.verso;
          this.reservations = this.selectedVerso === 'Andata' ? rese.alunniPerFermataAndata : rese.alunniPerFermataRitorno;
        }, (error) => console.error(error));
      }
    }, (error) => console.error(error));
  }

  ngOnInit() {
  }

  togglePresenza(id: number, alunno: Alunno) {
    const al = this.reservations.find(p => p.fermata.id === id).alunni.find(a => a === alunno);
    al.presenza = !al.presenza;
  }

  presente(id: number, alunno: Alunno): boolean {
    return this.reservations.find(p => p.fermata.id === id).alunni.find(a => a === alunno).presenza;
  }

  sortedAlunni(alu: Alunno[]) {
    return alu.sort((a, b) => {
      return (a.surname !== b.surname) ? a.surname.localeCompare(b.surname) : a.name.localeCompare(b.name);
    });
  }

}
