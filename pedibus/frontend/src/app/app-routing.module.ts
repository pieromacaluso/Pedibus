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
import {AuthGuard} from './auth.guard';
import {ForgotPasswordComponent} from './registration/forgot-password/forgot-password.component';
import {RecoverComponent} from './registration/recover/recover.component';
import {ConfirmComponent} from './registration/confirm/confirm.component';
import { DisponibilitaComponent } from './logged/disponibilita/disponibilita.component';
import { TurniComponent } from './logged/turni/turni.component';
import { AdminGuard } from './admin.guard';
import {GuideGuard} from './guide.guard';
import { NotificheComponent } from './logged/notifiche/notifiche.component';

const appRoutes: Routes = [
  {path: 'sign-up', component: SignUpComponent, canActivate: [AuthGuard], data: { animation: 'yes' }},
  {path: 'sign-in', component: SignInComponent,  canActivate: [AuthGuard], data: { animation: 'yes' }},
  {path: 'presenze', component: PresenzeComponent, canActivate: [AuthGuard], data: { animation: 'yes' }},
  {path: 'disponibilita', component: DisponibilitaComponent, canActivate: [AuthGuard, GuideGuard], data: { animation: 'yes' }},
  {path: 'notifiche', component: NotificheComponent, canActivate: [AuthGuard], data: { animation: 'yes' }},
  {path: 'turni', component: TurniComponent, canActivate: [AuthGuard, AdminGuard], data: { animation: 'yes' }},
  {path: 'recover', component: ForgotPasswordComponent, data: { animation: 'yes' }},
  {path: 'recover/:token', component: RecoverComponent, data: { animation: 'yes' }},
  {path: 'confirm/:token', component: ConfirmComponent, data: { animation: 'yes' }},
  {path: '', redirectTo: 'presenze', pathMatch: 'full', data: { animation: 'yes' }},
  {path: '404', component: PageNotFoundComponent, data: { animation: 'yes' }},
  {path: '**', redirectTo: '/404', data: { animation: 'yes' }}
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
