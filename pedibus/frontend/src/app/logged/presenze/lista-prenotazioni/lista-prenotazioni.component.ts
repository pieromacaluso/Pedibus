import {Component, OnDestroy, OnInit} from '@angular/core';
import {LineReservationVerso, Alunno, AlunnoNotReserved, PrenotazioneRequest} from '../../line-details';
import {SyncService} from '../sync.service';
import {ApiService} from '../../api.service';
import {AuthService} from '../../../registration/auth.service';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {AdminBookDialogComponent} from './admin-book-dialog/admin-book-dialog.component';
import {MatSnackBar} from '@angular/material';
import {RxStompService} from '@stomp/ng2-stompjs';
import {Observable, Subscription} from 'rxjs';
import {DatePipe} from '@angular/common';
import {DeleteDialogComponent} from './delete-dialog/delete-dialog.component';
import {catchError, switchMap, tap} from 'rxjs/operators';

@Component({
  selector: 'app-lista-prenotazioni',
  templateUrl: './lista-prenotazioni.component.html',
  styleUrls: ['./lista-prenotazioni.component.scss']
})
export class ListaPrenotazioniComponent implements OnInit, OnDestroy {

  resource: LineReservationVerso;

  cross: any = '../assets/svg/cross.svg';
  prenotazione: PrenotazioneRequest;
  componentMatDialogRef: MatDialogRef<AdminBookDialogComponent>;
  deleteDialogRef: MatDialogRef<DeleteDialogComponent>;
  bottomCardTitle: string;
  children: any[] = [];
  private loading: boolean;
  private handledSub: Subscription;
  private reservationSub: Subscription;

  constructor(private syncService: SyncService, private apiService: ApiService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe) {
    // First Observable
    this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(
        pren => {
          console.log('ciao', pren);
          this.prenotazione = pren;
          return this.apiService.getPrenotazioneByLineaAndDateAndVerso(pren).pipe(
            catchError((err, caught) => {
              this.resource = null;
              this.loading = false;
              return null;
            })
          );
        }
      ),
      tap(() => this.loading = false)
    ).subscribe(res => {
        console.log('first', res);
        if ('alunniPerFermata' in res) {
          this.resource = res;
        }
      }
    );

    // Message Handled
    this.handledSub = this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(
        pren => {
          return this.rxStompService.watch('/handled' + this.pathSub(pren));
        }
      ),
      tap(() => this.loading = false)
    ).subscribe(message => {
      const res = JSON.parse(message.body);
      console.log('ws', res);
      // tslint:disable-next-line:max-line-length
      const al = this.resource.alunniPerFermata.find(p => p.fermata.id === res.idFermata).alunni.find(a => a.codiceFiscale === res.cfChild);
      al.presoInCarico = res.isSet;
    });

    // Message reservation
    this.reservationSub = this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(
        pren => {
          return this.rxStompService.watch('/reservation' + this.pathSub(pren));
        }
      ),
      tap(() => this.loading = false)
    ).subscribe(message => {
      const res = JSON.parse(message.body);
      const oldAlunno = this.resource.childrenNotReserved.find(a => a.codiceFiscale === res.cfChild);
      let newAlunno: Alunno;
      if (!oldAlunno) {
        let searchAlunno;
        for (const fe of this.resource.alunniPerFermata) {
          searchAlunno = fe.alunni.find(a => a.codiceFiscale === res.cfChild);
          if (searchAlunno) {
            newAlunno = {
              codiceFiscale: searchAlunno.codiceFiscale,
              name: searchAlunno.name,
              surname: searchAlunno.surname,
              presoInCarico: false,
              arrivatoScuola: false,
              update: false
            };
            const index: number = fe.alunni.indexOf(searchAlunno);
            if (index !== -1) {
              fe.alunni.splice(index, 1);
            }
            break;
          }
        }

      } else {
        newAlunno = {
          codiceFiscale: oldAlunno.codiceFiscale,
          name: oldAlunno.name,
          surname: oldAlunno.surname,
          presoInCarico: false,
          arrivatoScuola: false,
          update: false
        };
        this.deleteNotReserved(oldAlunno);
      }
      if (newAlunno) {
        const al = this.resource.alunniPerFermata.find(p => p.fermata.id === res.idFermata).alunni.push(newAlunno);
        if (oldAlunno) {
          this.togglePresenza(res.idFermata, newAlunno);
        }
      }
    });
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
    if (this.canModify() || (this.authService.isUser() && (this.children.find((c) => c.codiceFiscale === alu.codiceFiscale)))) {

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
    return this.loading || !this.rxStompService.connected();
  }

  showLoadingButton(alu: any) {
    return alu.update;
  }

  togglePresenza(id: number, alunno: Alunno) {
    console.log(id, alunno);
    if (this.canModify()) {
      const al = this.resource.alunniPerFermata.find(p => p.fermata.id === id).alunni.find(a => a === alunno);
      console.log(al);

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
        console.log('Opened delete dialog');
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

    } else {
      return alu.sort((a, b) => {
        return (a.surname !== b.surname) ? a.surname.localeCompare(b.surname) : a.name.localeCompare(b.name);
      });
    }
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.handledSub.unsubscribe();
    this.reservationSub.unsubscribe();
  }

  deleteNotReserved(al: AlunnoNotReserved) {
    const index: number = this.resource.childrenNotReserved.indexOf(al);
    if (index !== -1) {
      this.resource.childrenNotReserved.splice(index, 1);
    }
  }
}
