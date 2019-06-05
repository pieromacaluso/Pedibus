import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule} from '@angular/core';
import {FlexLayoutModule} from '@angular/flex-layout';
import {HttpClientModule} from '@angular/common/http';

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
import {LayoutModule} from '@angular/cdk/layout';
import {HeaderComponent} from './header/header.component';
import {AppRoutingModule} from './app-routing.module';
import {PresenzeComponent} from './presenze/presenze.component';
import {RegisterComponent} from './register/register.component';
import {LoginComponent} from './login/login.component';
import {PageNotFoundComponent} from './page-not-found/page-not-found.component';

@NgModule({
  declarations: [
    AppComponent,
    PresenzeComponent,
    HeaderComponent,
    RegisterComponent,
    LoginComponent,
    PageNotFoundComponent
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
