import {Component, OnDestroy, OnInit} from '@angular/core';
import {LineReservationVerso, Alunno, AlunnoNotReserved, PrenotazioneRequest} from '../../line-details';
import {SyncService} from '../sync.service';
import {ApiService} from '../../api.service';
import {AuthService} from '../../../auth/auth.service';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {AdminBookDialogComponent} from './admin-book-dialog/admin-book-dialog.component';
import {MatBottomSheet, MatSnackBar} from '@angular/material';
import {RxStompService} from '@stomp/ng2-stompjs';
import {Observable, Subscription} from 'rxjs';
import {DatePipe} from '@angular/common';
import {catchError, switchMap, tap} from 'rxjs/operators';
import {MapService} from '../../../utilities/map.service';
import {ReservationDTO} from '../../genitore/dtos';
import {PresenceDialogComponent} from './presence-dialog/presence-dialog.component';

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
  children: any[] = [];
  private loading: boolean;
  private reservationSub: Subscription;
  private firstSub: Subscription;

  constructor(private syncService: SyncService, private apiService: ApiService,
              private authService: AuthService, private dialog: MatDialog, private snackBar: MatSnackBar,
              private rxStompService: RxStompService, private datePipe: DatePipe, private mapService: MapService) {
    /**
     * First Observable
     * Ottieni gli alunni per fermata
     */
    this.firstSub = this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(
        pren => {
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
        if ('alunniPerFermata' in res) {
          this.resource = res;
        }
      }
    );

    /**
     * WebSocket per monitoraggio aggiornamenti prenotazioni
     */
    this.reservationSub = this.syncService.prenotazioneObs$.pipe(
      tap(() => this.loading = true),
      switchMap(
        pren => {
          return this.rxStompService.watch('/reservation' + this.pathSub(pren));
        }
      ),
      tap(() => this.loading = false)
    ).subscribe(message => {
      const res: ReservationDTO = JSON.parse(message.body);
      // Se idFermata è null, si tratta di una cancellazione
      if (res.idFermata === null) {
        let searchAlunno;
        for (const fe of this.resource.alunniPerFermata) {
          searchAlunno = fe.alunni.find(a => a.codiceFiscale === res.cfChild);
          if (searchAlunno) {
            const index: number = fe.alunni.indexOf(searchAlunno);
            if (index !== -1) {
              fe.alunni.splice(index, 1);
            }
            break;
          }
        }
        this.resource.childrenNotReserved.push(searchAlunno);
        return;
      }
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
              presoInCarico: res.presoInCarico,
              arrivatoScuola: res.arrivatoScuola,
              assente: res.assente,
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
          presoInCarico: res.presoInCarico,
          arrivatoScuola: res.arrivatoScuola,
          assente: res.assente,
          update: false
        };
        this.deleteNotReserved(oldAlunno);
      }
      if (newAlunno) {
        const al = this.resource.alunniPerFermata.find(p => p.fermata.id === res.idFermata).alunni.push(newAlunno);
      }
    });
  }

  /**
   * Da prenotazione a path per richieste
   * @param prenotazione prenotazione
   */
  private pathSub(prenotazione: PrenotazioneRequest) {
    return '/' + this.datePipe.transform(
      prenotazione.data, 'yyyy-MM-dd') + '/' + prenotazione.linea + '/' + this.apiService.versoToInt(prenotazione.verso);
  }

  /**
   * Check if the user is admin
   */
  isAdmin() {
    return this.authService.isAdmin();
  }

  /**
   * Open Dialog di prenotazione alunno non prenotato dal genitore
   * @param alu alunno non prenotato
   */
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

  /**
   * Posso modificare la risorsa? Sì se mi arriva un dato tale dal backend e se sono SysAdmin
   */
  canModify() {
    return this.authService.isSysAdmin() || this.resource.canModify;
    // return true;
  }

  showLoading() {
    return this.loading || !this.rxStompService.connected();
  }

  showLoadingButton(alu: any) {
    return alu.update;
  }

  /**
   * Toggle presenza
   * @param id fermata
   * @param alunno alunno
   */
  togglePresenza(id: number, alunno: Alunno) {
    if (this.canModify()) {
      this.dialog.open(PresenceDialogComponent, {
        hasBackdrop: true,
        data: {
          alunno,
          prenotazione: this.prenotazione,
          res: this.resource.alunniPerFermata,
          resource: this.resource
        }
      });
    }
  }

  /**
   * Ritorna se il bambino è presente
   * @param id id fermata
   * @param alunno alunno
   */
  presente(id: number, alunno: Alunno): boolean {
    return this.resource.alunniPerFermata.find(p => p.fermata.id === id).alunni.find(a => a === alunno).presoInCarico;
  }

  /**
   * Ritorna gli alunni riordinati
   * @param alu array di alunni
   */
  sortedAlunni(alu: Alunno[]) {
    return alu.sort((a, b) => {
      return (a.surname !== b.surname) ? a.surname.localeCompare(b.surname) : a.name.localeCompare(b.name);
    });
  }

  /**
   * Ritorna gli alunni non prenotati riordinati
   * @param alu array di alunni non prenotati
   */
  sortedNotReserved(alu: AlunnoNotReserved[]) {
    return alu.sort((a, b) => {
      return (a.surname !== b.surname) ? a.surname.localeCompare(b.surname) : a.name.localeCompare(b.name);
    });
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.reservationSub.unsubscribe();
    this.firstSub.unsubscribe();
  }

  /**
   * Cancella alunno non prenotato dalla lista
   * @param al alunno non prenotato
   */
  deleteNotReserved(al: AlunnoNotReserved) {
    const index: number = this.resource.childrenNotReserved.indexOf(al);
    if (index !== -1) {
      this.resource.childrenNotReserved.splice(index, 1);
    }
  }

  /**
   * Apri la mappa della fermata
   * @param idFermata id della fermata
   */
  openMapDialog(idFermata: number) {
    this.mapService.openMapDialog(idFermata).subscribe();
  }

  /**
   * Scarica il JSON delle fermate
   */
  downloadJson() {
    this.apiService.downloadJson(this.prenotazione).subscribe((res) => {
      this.downloadFile(res);
    });
  }

  /**
   * Funzione che dai dati in JSON permette lo scaricamento del file con un nome ed una procedura user-friendly
   * @param data dati JSON
   */
  downloadFile(data: any) {
    const sJson = JSON.stringify(data, null, '\t');
    const element = document.createElement('a');
    element.setAttribute('href', 'data:text/json;charset=UTF-8,' + encodeURIComponent(sJson));
    element.setAttribute('download', 'reservations_' +
      this.prenotazione.linea + '_' +
      this.prenotazione.verso.toLowerCase() + '_' +
      this.datePipe.transform(this.prenotazione.data, 'yyyy-MM-dd') + '.json');
    element.style.display = 'none';
    document.body.appendChild(element);
    element.click(); // simulate click
    document.body.removeChild(element);
  }

  /**
   * Abilita il download solo per SysAdmin e Admin di linea
   */
  enableDownload() {
    return this.authService.isSysAdmin() || this.authService.isAdmin();
  }
}
