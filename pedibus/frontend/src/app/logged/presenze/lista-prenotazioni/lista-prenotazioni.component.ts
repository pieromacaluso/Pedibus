import {Component, Inject, Injectable, Input, OnInit, SimpleChange} from '@angular/core';
import {AlunniPerFermata, Alunno, AlunnoNotReserved, PrenotazioneRequest} from '../../line-details';
import {SyncService} from '../sync.service';
import {ApiService} from '../../api.service';
import {AuthService} from '../../../registration/auth.service';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatDialogConfig} from '@angular/material/dialog';
import {AdminBookDialogComponent, DialogData} from './admin-book-dialog/admin-book-dialog.component';
import {filter} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material';
import {RxStompService} from '@stomp/ng2-stompjs';
import {Message} from '@stomp/stompjs';
import {Subscription} from 'rxjs';
import {DatePipe} from '@angular/common';
import {$} from 'protractor';

@Component({
  selector: 'app-lista-prenotazioni',
  templateUrl: './lista-prenotazioni.component.html',
  styleUrls: ['./lista-prenotazioni.component.scss']
})
export class ListaPrenotazioniComponent implements OnInit {

  reservations: AlunniPerFermata[];

  cross: any = '../assets/svg/cross.svg';
  prenotazione: PrenotazioneRequest;
  countLoading: any = 0;
  private notReserved: AlunnoNotReserved[];
  componentMatDialogRef: MatDialogRef<AdminBookDialogComponent>;
  private handledSub: Subscription;
  private openedDialog: any = 0;
  private resSub: Subscription;
  private modEnabled: boolean;

  constructor(private syncService: SyncService, private apiService: ApiService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe) {
    // this.connect();
    this.syncService.prenotazioneObs$.subscribe((prenotazione) => {
      if (prenotazione.linea && prenotazione.verso && prenotazione.data) {

        // Disiscrizione dalla Broker handledSub precedente se esiste
        if (this.handledSub) {
          this.handledSub.unsubscribe();
        }
        // Iscrizione alla nuova Broker
        this.handledSub = this.rxStompService.watch('/handled' + this.pathSub(prenotazione))
          .subscribe((message: Message) => {
            const res = JSON.parse(message.body);
            console.log(res);
            const al = this.reservations.find(p => p.fermata.id === res.idFermata).alunni.find(a => a.codiceFiscale === res.cfChild);
            al.presoInCarico = res.isSet;
          });

        // Disiscrizione dalla Broker reservationSub precedente se esiste
        if (this.resSub) {
          this.resSub.unsubscribe();
        }
        // Iscrizione alla nuova Broker
        this.resSub = this.rxStompService.watch('/reservation' + this.pathSub(prenotazione))
          .subscribe((message: Message) => {
            const res = JSON.parse(message.body);
            const oldAlunno = this.notReserved.find(a => a.codiceFiscale === res.cfChild);
            const newAlunno: Alunno = {
              codiceFiscale: oldAlunno.codiceFiscale,
              name: oldAlunno.name,
              surname: oldAlunno.surname,
              presoInCarico: false,
              arrivatoScuola: false,
              update: false
            };
            const al = this.reservations.find(p => p.fermata.id === res.idFermata).alunni.push(newAlunno);
            this.deleteNotReserved(oldAlunno);
            this.togglePresenza(res.idFermata, newAlunno);
          });
        this.prenotazione = prenotazione;
        this.countLoading++;
        this.apiService.getPrenotazioneByLineaAndDateAndVerso(prenotazione).subscribe((rese) => {
          this.reservations = this.prenotazione.verso === 'Andata' ? rese.alunniPerFermataAndata : rese.alunniPerFermataRitorno;
          this.notReserved = rese.childrenNotReserved;
          this.modEnabled = rese.canModify;
          this.countLoading--;
        }, (error) => console.error(error));
      }
    }, (error) => console.error(error));

  }

  private pathSub(prenotazione: PrenotazioneRequest) {
    return '/' + this.datePipe.transform(
      prenotazione.data, 'yyyy-MM-dd') + '/' + prenotazione.linea + '/' + this.apiService.versoToInt(prenotazione.verso);
  }

  isAdmin(){
    return this.authService.isAdmin();
  }

  openDialog(alu: AlunnoNotReserved) {
    if (this.canModify()) {

      this.dialog.closeAll();

      this.componentMatDialogRef = this.dialog.open(AdminBookDialogComponent, {
        hasBackdrop: true,
        data: {
          alunno: alu,
          res: this.reservations,
          data: this.prenotazione.data,
          verso: this.prenotazione.verso,
          linea: this.prenotazione.linea
        }
      });
    }
  }
  canModify() {
    return this.authService.isAdmin() && this.isModifiable() && this.modEnabled;
  }
  showLoading() {
    return this.countLoading > 0 || !this.rxStompService.connected();
  }

  showLoadingButton(alu: any) {
    return alu.update;
  }

  togglePresenza(id: number, alunno: Alunno) {
    if (this.canModify()) {
      const al = this.reservations.find(p => p.fermata.id === id).alunni.find(a => a === alunno);
      al.update = true;
      this.apiService.postPresenza(al, this.prenotazione, !al.presoInCarico).subscribe((rese) => {
        console.log('presenza subscribe emitted something');
        al.update = false;
      }, (error) => {
        console.error(error);
        al.update = false;
      });
    }
  }

  presente(id: number, alunno: Alunno): boolean {
    return this.reservations.find(p => p.fermata.id === id).alunni.find(a => a === alunno).presoInCarico;
  }

  sortedAlunni(alu: Alunno[]) {
    return alu.sort((a, b) => {
      return (a.surname !== b.surname) ? a.surname.localeCompare(b.surname) : a.name.localeCompare(b.name);
    });
  }

  sortedNotReserved(alu: AlunnoNotReserved[]) {
    return alu.sort((a, b) => {
      return (a.surname !== b.surname) ? a.surname.localeCompare(b.surname) : a.name.localeCompare(b.name);
    });
  }

  ngOnInit(): void {
  }

  deleteNotReserved(al: AlunnoNotReserved) {
    const index: number = this.notReserved.indexOf(al);
    if (index !== -1) {
      this.notReserved.splice(index, 1);
    }
  }

  private isModifiable() {
    const today = new Date();
    const preno = this.prenotazione.data;
    preno.setHours(0);
    preno.setMinutes(0);
    preno.setSeconds(0);
    preno.setMilliseconds(0);
    today.setHours(0);
    today.setMinutes(0);
    today.setSeconds(0);
    today.setMilliseconds(0);
    return preno.getTime() >= today.getTime();
  }
}
