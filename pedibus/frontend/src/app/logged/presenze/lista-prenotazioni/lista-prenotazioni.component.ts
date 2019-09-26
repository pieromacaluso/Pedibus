import {Component, Inject, Injectable, Input, OnInit, SimpleChange} from '@angular/core';
import {LineReservationVerso, AlunniPerFermata, Alunno, AlunnoNotReserved, PrenotazioneRequest} from '../../line-details';
import {SyncService} from '../sync.service';
import {ApiService} from '../../api.service';
import {AuthService} from '../../../registration/auth.service';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatDialogConfig} from '@angular/material/dialog';
import {AdminBookDialogComponent} from './admin-book-dialog/admin-book-dialog.component';
import {MatSnackBar} from '@angular/material';
import {RxStompService} from '@stomp/ng2-stompjs';
import {Message} from '@stomp/stompjs';
import {Subscription} from 'rxjs';
import {DatePipe} from '@angular/common';
import { DeleteDialogComponent } from './delete-dialog/delete-dialog.component';

@Component({
  selector: 'app-lista-prenotazioni',
  templateUrl: './lista-prenotazioni.component.html',
  styleUrls: ['./lista-prenotazioni.component.scss']
})
export class ListaPrenotazioniComponent implements OnInit {

  resource: LineReservationVerso;

  cross: any = '../assets/svg/cross.svg';
  prenotazione: PrenotazioneRequest;
  countLoading = 0;
  componentMatDialogRef: MatDialogRef<AdminBookDialogComponent>;
  deleteDialogRef: MatDialogRef<DeleteDialogComponent>;
  bottomCardTitle: string;
  children: any[] = [];
  private handledSub: Subscription;
  // private openedDialog: any = 0;
  private resSub: Subscription;

  constructor(private syncService: SyncService, private apiService: ApiService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe) {
    // this.connect();
    this.syncService.prenotazioneObs$.subscribe((prenotazione) => {
      if (prenotazione.linea && prenotazione.verso && prenotazione.data) {
        this.prenotazione = null;
        // Disiscrizione dalla Broker handledSub precedente se esiste
        if (this.handledSub) {
          this.handledSub.unsubscribe();
        }
        // Iscrizione alla nuova Broker
        this.handledSub = this.rxStompService.watch('/handled' + this.pathSub(prenotazione))
          .subscribe((message: Message) => {
            const res = JSON.parse(message.body);
            console.log(res);
            // tslint:disable-next-line:max-line-length
            const al = this.resource.alunniPerFermata.find(p => p.fermata.id === res.idFermata).alunni.find(a => a.codiceFiscale === res.cfChild);
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
            const oldAlunno = this.resource.childrenNotReserved.find(a => a.codiceFiscale === res.cfChild);
            const newAlunno: Alunno = {
              codiceFiscale: oldAlunno.codiceFiscale,
              name: oldAlunno.name,
              surname: oldAlunno.surname,
              presoInCarico: false,
              arrivatoScuola: false,
              update: false
            };
            const al = this.resource.alunniPerFermata.find(p => p.fermata.id === res.idFermata).alunni.push(newAlunno);
            this.deleteNotReserved(oldAlunno);
            this.togglePresenza(res.idFermata, newAlunno);
          });
        this.prenotazione = prenotazione;
        this.countLoading++;
        this.apiService.getPrenotazioneByLineaAndDateAndVerso(prenotazione).subscribe((rese) => {
          this.resource = rese;
          this.countLoading--;
        }, (error) => console.error(error));
      }
    }, (error) => console.error(error));

    this.setBottoCardTitle();

  }

  setBottoCardTitle() {
    if (this.authService.getRoles().includes('ROLE_USER')) {
      this.bottomCardTitle = 'Prenota fermata';
      this.apiService.getChildren().subscribe((childs) => {
        this.children = childs;
        console.log('children:', childs);
      });
    }
    if (this.authService.getRoles().includes('ROLE_ADMIN')) {
      this.bottomCardTitle = 'Bambini non prenotati';
    }
  }

  private pathSub(prenotazione: PrenotazioneRequest) {
    return '/' + this.datePipe.transform(
      prenotazione.data, 'yyyy-MM-dd') + '/' + prenotazione.linea + '/' + this.apiService.versoToInt(prenotazione.verso);
  }

  isAdmin() {
    return this.authService.isAdmin();
  }

  openDialog(alu: AlunnoNotReserved) {
    if (this.canModify()) {

      this.dialog.closeAll();

      this.componentMatDialogRef = this.dialog.open(AdminBookDialogComponent, {
        hasBackdrop: true,
        data: {
          alunno: alu,
          res: this.resource.alunniPerFermata,
          data: this.prenotazione.data,
          verso: this.prenotazione.verso,
          linea: this.prenotazione.linea
        }
      });
    }
  }

  openDeleteDialog(alu: Alunno) {
    this.dialog.closeAll();

    this.deleteDialogRef = this.dialog.open(DeleteDialogComponent, {
        hasBackdrop: true,
        data: {
          alunno: alu,
          res: this.resource.alunniPerFermata,
          data: this.prenotazione.data,
          verso: this.prenotazione.verso,
          linea: this.prenotazione.linea,
          resource: this.resource
        }
      });
  }

  canModify() {
    return this.authService.isAdmin() && this.resource.canModify;
    // return true;
  }

  showLoading() {
    return this.countLoading > 0 || !this.rxStompService.connected();
  }

  showLoadingButton(alu: any) {
    return alu.update;
  }

  togglePresenza(id: number, alunno: Alunno) {
    if (this.canModify()) {
      const al = this.resource.alunniPerFermata.find(p => p.fermata.id === id).alunni.find(a => a === alunno);
      al.update = true;
      this.apiService.postPresenza(al, this.prenotazione, !al.presoInCarico).subscribe((rese) => {
        console.log('presenza subscribe emitted something');
        al.update = false;
      }, (error) => {
        console.error(error);
        al.update = false;
      });
    }
    if (this.authService.isUser()) {
      const isChild = this.children.find((c) => c.codiceFiscale === alunno.codiceFiscale);
      if (isChild) {
        this.openDeleteDialog(alunno);
      }
    }
  }

  presente(id: number, alunno: Alunno): boolean {
    return this.resource.alunniPerFermata.find(p => p.fermata.id === id).alunni.find(a => a === alunno).presoInCarico;
  }

  sortedAlunni(alu: Alunno[]) {
    return alu.sort((a, b) => {
      return (a.surname !== b.surname) ? a.surname.localeCompare(b.surname) : a.name.localeCompare(b.name);
    });
  }

  sortedNotReserved(alu: AlunnoNotReserved[]) {
    if (this.authService.getRoles().includes('ROLE_USER')) {
     // todo: 1. get children, 2. filtra, 3. ordina
      return alu.filter((a) => this.children.find((c) => c.codiceFiscale === a.codiceFiscale))
      .sort((a, b) => {
        return (a.surname !== b.surname) ? a.surname.localeCompare(b.surname) : a.name.localeCompare(b.name);
      });

  }
    if (this.authService.getRoles().includes('ROLE_ADMIN')) {
      return alu.sort((a, b) => {
        return (a.surname !== b.surname) ? a.surname.localeCompare(b.surname) : a.name.localeCompare(b.name);
      });
    }
  }

  ngOnInit(): void {
  }

  deleteNotReserved(al: AlunnoNotReserved) {
    const index: number = this.resource.childrenNotReserved.indexOf(al);
    if (index !== -1) {
      this.resource.childrenNotReserved.splice(index, 1);
    }
  }

  /*private isModifiable() {
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
    // console.log(today, preno);
    return preno.getTime() === today.getTime();
  }*/
}
