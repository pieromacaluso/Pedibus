import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule} from '@angular/core';
import {FlexLayoutModule} from '@angular/flex-layout';

import {AppComponent} from './app.component';
import {
  MatCardModule,
  MatDatepickerModule,
  MatListModule,
  MatSelectModule,
  MatToolbarModule,
  MatNativeDateModule,
  MatFormFieldModule,
  MatInputModule, MatButtonModule, MatSidenavModule, MatIconModule, MAT_DATE_LOCALE
} from '@angular/material';
import {FormsModule} from '@angular/forms';
import {LayoutModule} from '@angular/cdk/layout';
import {HeaderComponent} from './header/header.component';
import {HttpClientModule} from '@angular/common/http';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatListModule,
    MatDatepickerModule,
    MatToolbarModule,
    MatSelectModule,
    MatCardModule,
    MatNativeDateModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    LayoutModule,
    MatButtonModule,
    MatSidenavModule,
    MatIconModule,
    FlexLayoutModule
  ],
  providers: [{provide: MAT_DATE_LOCALE, useValue: 'it-IT'}],
  bootstrap: [AppComponent]
})
export class AppModule {
}
