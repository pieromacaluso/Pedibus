import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HeaderComponent} from './header/header.component';
import {
  MatButtonModule,
  MatCardModule,
  MatDialogModule,
  MatIconModule,
  MatListModule,
  MatSidenavModule,
  MatToolbarModule
} from '@angular/material';
import {AppRoutingModule} from '../routes/app-routing.module';
import {RouterModule} from '@angular/router';
import {ExtendedModule, FlexModule} from '@angular/flex-layout';
import {ConfirmationDialogComponent} from './confimation-dialog/confirmation-dialog.component';
import {MapDialogComponent} from './map-dialog/map-dialog.component';
import {StopMapComponent} from './stop-map/stop-map.component';
import {StopsMapComponent} from './stops-map/stops-map.component';
import {AgmCoreModule} from '@agm/core';
import {PageNotFoundComponent} from './page-not-found/page-not-found.component';


@NgModule({
  declarations: [
    HeaderComponent,
    ConfirmationDialogComponent,
    MapDialogComponent,
    StopMapComponent,
    StopsMapComponent,
    PageNotFoundComponent
  ],
  exports: [
    HeaderComponent,
    ConfirmationDialogComponent,
    MapDialogComponent,
    StopMapComponent,
    StopsMapComponent
  ],
  imports: [
    CommonModule,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    RouterModule,
    FlexModule,
    ExtendedModule,
    MatButtonModule,
    MatDialogModule,
    AgmCoreModule,
    MatCardModule
  ],
  entryComponents: [
    MapDialogComponent,
    ConfirmationDialogComponent
  ]

})
export class UtilitiesModule {

}
