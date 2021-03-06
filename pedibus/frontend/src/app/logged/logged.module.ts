import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PresenzeComponent} from './presenze/presenze.component';
import {
  MatButtonModule,
  MatCardModule, MatCheckboxModule,
  MatDatepickerModule, MatDialogModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule, MatListModule,
  MatNativeDateModule, MatPaginatorModule, MatProgressBarModule, MatProgressSpinnerModule, MatRadioModule,
  MatSelectModule, MatSlideToggleModule, MatTabsModule
} from '@angular/material';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ToolbarComponent} from './presenze/toolbar/toolbar.component';
import {ListaPrenotazioniComponent} from './presenze/lista-prenotazioni/lista-prenotazioni.component';
import {FlexLayoutModule} from '@angular/flex-layout';
import {AdminBookDialogComponent} from './presenze/lista-prenotazioni/admin-book-dialog/admin-book-dialog.component';
import {FortmatLinePipe} from './presenze/toolbar/fortmat-line.pipe';
import {DisponibilitaComponent} from './disponibilita/disponibilita.component';
import {TurniComponent} from './turni/turni.component';
import {NotificheComponent} from './notifiche/notifiche.component';
import {AggiuntaDispComponent} from './disponibilita/aggiunta-disp/aggiunta-disp.component';
import {ElencoDispComponent} from './turni/elenco-disp/elenco-disp.component';
import {NotificaComponent} from './notifiche/notifica/notifica.component';
import {UpdateDispDialogComponent} from './turni/elenco-disp/update-disp-dialog/update-disp-dialog.component';
import {GenitoreComponent} from './genitore/genitore.component';
import {SchedaBambinoComponent} from './genitore/scheda-bambino/scheda-bambino.component';
import {DialogAnagraficaComponent} from './genitore/dialog-anagrafica/dialog-anagrafica.component';
import {DialogPrenotazioneComponent} from './genitore/dialog-prenotazione/dialog-prenotazione.component';
import {AgmCoreModule} from '@agm/core';
import {PresenceDialogComponent} from './presenze/lista-prenotazioni/presence-dialog/presence-dialog.component';
import {AnagraficaComponent} from './anagrafica/anagrafica.component';
import {TabComponent} from './anagrafica/tab/tab.component';
import {EntryUserComponent} from './anagrafica/tab/entry-user/entry-user.component';
import {EntryChildComponent} from './anagrafica/tab/entry-child/entry-child.component';
import {ChildDialogComponent} from './anagrafica/child-dialog/child-dialog.component';
import {UserDialogComponent} from './anagrafica/user-dialog/user-dialog.component';
import {LineAdminComponent} from './line-admin/line-admin.component';
import {GuideEntryComponent} from './line-admin/guide-entry/guide-entry.component';
import {UtilitiesModule} from '../utilities/utilities.module';
import {DateToolbarComponent} from './genitore/scheda-bambino/date-toolbar/date-toolbar.component';


@NgModule({
  declarations: [
    PresenzeComponent,
    ToolbarComponent,
    ListaPrenotazioniComponent,
    AdminBookDialogComponent,
    FortmatLinePipe,
    DisponibilitaComponent,
    TurniComponent,
    NotificheComponent,
    AggiuntaDispComponent,
    ElencoDispComponent,
    NotificaComponent,
    UpdateDispDialogComponent,
    GenitoreComponent,
    SchedaBambinoComponent,
    DialogAnagraficaComponent,
    DialogPrenotazioneComponent,
    PresenceDialogComponent,
    AnagraficaComponent,
    TabComponent,
    EntryUserComponent,
    EntryChildComponent,
    ChildDialogComponent,
    UserDialogComponent,
    LineAdminComponent,
    GuideEntryComponent,
    DateToolbarComponent,
  ],
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
    MatRadioModule,
    MatSlideToggleModule,
    MatListModule,
    AgmCoreModule,
    MatTabsModule,
    MatPaginatorModule,
    MatCheckboxModule,
    UtilitiesModule,
  ],
  exports: [],
  entryComponents: [
    AdminBookDialogComponent,
    DialogAnagraficaComponent,
    DialogPrenotazioneComponent,
    UpdateDispDialogComponent,
    PresenceDialogComponent,
    UserDialogComponent,
    ChildDialogComponent
  ]
})
export class LoggedModule {
}
