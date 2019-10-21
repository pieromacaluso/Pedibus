import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PresenzeComponent} from './presenze/presenze.component';
import {
  MatButtonModule,
  MatCardModule,
  MatDatepickerModule, MatDialogModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatNativeDateModule, MatProgressBarModule, MatProgressSpinnerModule, MatRadioModule,
  MatSelectModule
} from '@angular/material';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ToolbarComponent} from './presenze/toolbar/toolbar.component';
import {ListaPrenotazioniComponent} from './presenze/lista-prenotazioni/lista-prenotazioni.component';
import {FlexLayoutModule} from '@angular/flex-layout';
import {AdminBookDialogComponent} from './presenze/lista-prenotazioni/admin-book-dialog/admin-book-dialog.component';
import {FortmatLinePipe} from './presenze/toolbar/fortmat-line.pipe';
import {DisponibilitaComponent} from './disponibilita/disponibilita.component';
import {TurniComponent} from './turni/turni.component';
import {ComunicazioniComponent} from './comunicazioni/comunicazioni.component';
import {NotificheComponent} from './notifiche/notifiche.component';
import { DeleteDialogComponent } from './presenze/lista-prenotazioni/delete-dialog/delete-dialog.component';
import { AggiuntaDispComponent } from './disponibilita/aggiunta-disp/aggiunta-disp.component';

@NgModule({
  declarations: [
    PresenzeComponent,
    ToolbarComponent,
    ListaPrenotazioniComponent,
    AdminBookDialogComponent,
    FortmatLinePipe,
    DisponibilitaComponent,
    TurniComponent,
    ComunicazioniComponent,
    NotificheComponent,
    DeleteDialogComponent,
    AggiuntaDispComponent],
  imports: [
    CommonModule,
    MatIconModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    FormsModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    FlexLayoutModule,
    MatProgressBarModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
    MatRadioModule
  ],
  exports: [],
  entryComponents: [AdminBookDialogComponent, DeleteDialogComponent]
})
export class LoggedModule {
}
