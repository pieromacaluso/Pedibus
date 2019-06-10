import {Component, Input, OnInit, SimpleChange} from '@angular/core';
import {AlunniPerFermata, Alunno, AlunnoNotReserved, PrenotazioneRequest} from '../../line-details';
import {SyncService} from '../sync.service';
import {ApiService} from '../../api.service';
import {AuthService} from '../../../registration/auth.service';

@Component({
  selector: 'app-lista-prenotazioni',
  templateUrl: './lista-prenotazioni.component.html',
  styleUrls: ['./lista-prenotazioni.component.scss']
})
export class ListaPrenotazioniComponent implements OnInit {

  reservations: AlunniPerFermata[];
  cross: any = '../assets/svg/cross.svg';
  prenotazione: PrenotazioneRequest;
  selectedVerso: string;
  countLoading: any = 0;
  private notReserved: AlunnoNotReserved[];

  constructor(private syncService: SyncService, private apiService: ApiService, private authService: AuthService) {
    this.syncService.prenotazioneObs$.subscribe((prenotazione) => {
      if (prenotazione.linea && prenotazione.verso && prenotazione.data) {
        console.log('inizio');
        this.prenotazione = prenotazione;
        this.countLoading++;
        this.apiService.getPrenotazioneByLineaAndDateAndVerso(prenotazione.linea, prenotazione.data).subscribe((rese) => {
          this.selectedVerso = prenotazione.verso;
          this.reservations = this.selectedVerso === 'Andata' ? rese.alunniPerFermataAndata : rese.alunniPerFermataRitorno;
          this.countLoading--;
        }, (error) => console.error(error));
        this.countLoading++;
        this.apiService.getNonPrenotati(prenotazione.data, prenotazione.verso).subscribe((rese) => {
          this.notReserved = rese.childrenNotReserved;
          console.log(rese.childrenNotReserved[1]);
          this.countLoading--;
        }, (error) => console.error(error));
      }
    }, (error) => console.error(error));

  }

  showLoading() {
    return this.countLoading > 0;
  }

  ngOnInit() {
  }

  togglePresenza(id: number, alunno: Alunno) {
    if (this.authService.isAdmin()) {
      const al = this.reservations.find(p => p.fermata.id === id).alunni.find(a => a === alunno);
      al.presenza = !al.presenza;
      this.apiService.postPresenza(al, this.prenotazione, al.presenza);
    }
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
