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
import {AppRoutingModule} from './routes/app-routing.module';
import {FlexLayoutModule} from '@angular/flex-layout';
import {DatePipe, registerLocaleData} from '@angular/common';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {HttpExceptionsInterceptor} from './interceptors/http-exceptions-interceptor';
import {AuthInterceptor} from './interceptors/auth-interceptor';
import {InjectableRxStompConfig, RxStompService, rxStompServiceFactory} from '@stomp/ng2-stompjs';
import {myRxStompConfig} from './configuration/my-rx-stomp.config';
import localeitIT from '@angular/common/locales/it';
import {UtilitiesModule} from './utilities/utilities.module';

registerLocaleData(localeitIT);

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    AgmCoreModule.forRoot({
      apiKey: '{API_TOKEN_GMAPS}'
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
    MatCardModule,
    UtilitiesModule,
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
