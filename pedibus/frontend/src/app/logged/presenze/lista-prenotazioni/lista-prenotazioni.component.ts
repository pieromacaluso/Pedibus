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
  private topicSubscription: Subscription;


  constructor(private syncService: SyncService, private apiService: ApiService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe) {
    // this.connect();
    this.syncService.prenotazioneObs$.subscribe((prenotazione) => {
      if (prenotazione.linea && prenotazione.verso && prenotazione.data) {
        // Disiscrizione dalla Broker precedente se esiste
        if (this.topicSubscription) {
          this.topicSubscription.unsubscribe();
        }
        // Iscrizione alla nuova Broker
        this.topicSubscription = this.rxStompService.watch('/handled' + this.pathSub(prenotazione))
          .subscribe((message: Message) => {
            const res = JSON.parse(message.body);
            console.log(res);
            const al = this.reservations.find(p => p.fermata.id === res.idFermata).alunni.find(a => a.codiceFiscale === res.cfChild);
            al.presoInCarico = res.isSet;
          });
        this.prenotazione = prenotazione;
        this.countLoading++;
        this.apiService.getPrenotazioneByLineaAndDateAndVerso(prenotazione).subscribe((rese) => {
          this.reservations = this.prenotazione.verso === 'Andata' ? rese.alunniPerFermataAndata : rese.alunniPerFermataRitorno;
          this.notReserved = rese.childrenNotReserved;
          this.countLoading--;
        }, (error) => console.error(error));
      }
    }, (error) => console.error(error));

  }

  private pathSub(prenotazione: PrenotazioneRequest) {
    return '/' + this.datePipe.transform(
      prenotazione.data, 'yyyy-MM-dd') + '/' + prenotazione.linea + '/' + this.apiService.versoToInt(prenotazione.verso);
  }

  openDialog(alu: AlunnoNotReserved) {
    this.componentMatDialogRef = this.dialog.open(AdminBookDialogComponent, {
      hasBackdrop: false,
      data: {
        alunno: alu,
        res: this.reservations,
        data: this.prenotazione.data,
        verso: this.prenotazione.verso,
        linea: this.prenotazione.linea
      }
    });
    this.componentMatDialogRef
      .afterClosed()
      .pipe(filter(idFermataRes => idFermataRes))
      .subscribe(idFermataRes => {
        // TODO: implementazione aggiunta Prenotazione da parte di admin
        this.snackBar.open('Da implementare POST:\n' +
          this.prenotazione.data.toLocaleDateString() + ' '
          + this.prenotazione.linea + ' ' + alu.codiceFiscale + ' ' + idFermataRes + ' ' + this.prenotazione.verso, '', {
          duration: 10000,
        });
        console.log(this.prenotazione.data.toLocaleDateString()
          + ' ' + this.prenotazione.linea + ' ' + alu.codiceFiscale + ' ' + idFermataRes + ' ' + this.prenotazione.verso);
      });
  }

  showLoading() {
    return this.countLoading > 0 || !this.rxStompService.connected();
  }
  showLoadingButton(alu: Alunno) {
    return alu.update;
  }

  ngOnInit() {
  }

  togglePresenza(id: number, alunno: Alunno) {
    console.log('outside');
    if (this.authService.isAdmin()) {
      console.log('inside');
      const al = this.reservations.find(p => p.fermata.id === id).alunni.find(a => a === alunno);
      console.log(al);
      al.update = true;
      this.apiService.postPresenza(al, this.prenotazione, !al.presoInCarico).subscribe((rese) => {
        al.update = false;
      }, (error) => console.error(error));
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

}
