import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {RegistrationModule} from './registration/registration.module';
import {LoggedModule} from './logged/logged.module';
import {HttpClientModule} from '@angular/common/http';

import {PageNotFoundComponent} from './page-not-found/page-not-found.component';
import {PresenzeComponent} from './logged/presenze/presenze.component';
import {SignUpComponent} from './registration/sign-up/sign-up.component';
import {SignInComponent} from './registration/sign-in/sign-in.component';

const appRoutes: Routes = [
  {path: 'sign-up', component: SignUpComponent},
  {path: 'sign-in', component: SignInComponent},
  {path: 'presenze', component: PresenzeComponent},
  {path: '', redirectTo: 'sign-up', pathMatch: 'full'},
  {path: '404', component: PageNotFoundComponent},
  {path: '**', redirectTo: '/404'}
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    RegistrationModule,
    LoggedModule,
    HttpClientModule,
    RouterModule.forRoot(appRoutes)
  ], exports: [RouterModule]
})
export class AppRoutingModule { }
