import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule} from '@angular/core';


import {AppComponent} from './app.component';
import {
  MatCardModule,
  MatDatepickerModule,
  MatListModule,
  MatSelectModule,
  MatToolbarModule,
  MatNativeDateModule,
  MatFormFieldModule,
  MatInputModule, MatButtonModule, MatSidenavModule, MatIconModule, MAT_DATE_LOCALE, MatTabsModule
} from '@angular/material';
import {FormsModule} from '@angular/forms';


@NgModule({
  declarations: [
    AppComponent,

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
    FlexLayoutModule,
    AppRoutingModule,
    MatTabsModule
  ],
  providers: [{provide: MAT_DATE_LOCALE, useValue: 'it-IT'}],
  bootstrap: [AppComponent]
})
export class AppModule {
}
