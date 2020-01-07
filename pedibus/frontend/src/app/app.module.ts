import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {LOCALE_ID, NgModule} from '@angular/core';
import { AgmCoreModule } from '@agm/core';

import {AppComponent} from './app.component';
import {
  MatButtonModule, MatCardModule,
  MatIconModule,
  MatListModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatTabsModule,
  MatToolbarModule,
  MAT_DATE_LOCALE
} from '@angular/material';
import {PageNotFoundComponent} from './page-not-found/page-not-found.component';
import {AppRoutingModule} from './app-routing.module';
import {FlexLayoutModule} from '@angular/flex-layout';
import { HeaderComponent } from './header/header.component';
import {DatePipe, registerLocaleData} from '@angular/common';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {HttpExceptionsInterceptor} from './http-exceptions-interceptor';
import {AuthInterceptor} from './auth-interceptor';
import {InjectableRxStompConfig, RxStompService, rxStompServiceFactory} from '@stomp/ng2-stompjs';
import {myRxStompConfig} from './my-rx-stomp.config';
import {LoggedModule} from './logged/logged.module';
import localeitIT from '@angular/common/locales/it';

registerLocaleData(localeitIT);

@NgModule({
  declarations: [
    AppComponent,
    PageNotFoundComponent,
    HeaderComponent
  ],
  imports: [
    BrowserModule,
    AgmCoreModule.forRoot({
      apiKey: 'AIzaSyBMo_rSgOdamFX_yHIbnmd24A-U7D7f2-0'
    }),
    BrowserAnimationsModule,
    MatTabsModule,
    AppRoutingModule,
    MatToolbarModule,
    MatIconModule,
    MatSidenavModule,
    MatListModule,
    FlexLayoutModule,
    MatButtonModule,
    MatSnackBarModule,
    MatCardModule
  ],
  providers: [DatePipe,
    {provide: MAT_DATE_LOCALE, useValue: 'it-IT'},
    { provide: LOCALE_ID, useValue: 'it-IT' },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpExceptionsInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      provide: InjectableRxStompConfig,
      useValue: myRxStompConfig
    },
    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
      deps: [InjectableRxStompConfig]
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
