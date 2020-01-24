import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PresenzeComponent } from './presenze/presenze.component';
import {
  MatButtonModule,
  MatCardModule,
  MatDatepickerModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatNativeDateModule,
  MatSelectModule
} from '@angular/material';
import {FormsModule} from '@angular/forms';
import { ToolbarComponent } from './presenze/toolbar/toolbar.component';
import { ListaPrenotazioniComponent } from './presenze/lista-prenotazioni/lista-prenotazioni.component';

@NgModule({
  declarations: [PresenzeComponent, ToolbarComponent, ListaPrenotazioniComponent],
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
    MatButtonModule
  ]
})
export class LoggedModule { }
