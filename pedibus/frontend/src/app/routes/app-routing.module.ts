import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {AuthenticationModule} from '../auth/authentication.module';
import {LoggedModule} from '../logged/logged.module';
import {HttpClientModule} from '@angular/common/http';

import {PageNotFoundComponent} from '../utilities/page-not-found/page-not-found.component';
import {PresenzeComponent} from '../logged/presenze/presenze.component';
import {SignUpComponent} from '../auth/sign-up/sign-up.component';
import {SignInComponent} from '../auth/sign-in/sign-in.component';
import {AuthGuard} from '../auth/guards/auth.guard';
import {ForgotPasswordComponent} from '../auth/forgot-password/forgot-password.component';
import {RecoverComponent} from '../auth/recover/recover.component';
import {ConfirmComponent} from '../auth/confirm/confirm.component';
import {DisponibilitaComponent} from '../logged/disponibilita/disponibilita.component';
import {TurniComponent} from '../logged/turni/turni.component';
import {AdminGuard} from '../auth/guards/admin.guard';
import {GuideGuard} from '../auth/guards/guide.guard';
import {NotificheComponent} from '../logged/notifiche/notifiche.component';
import {GenitoreComponent} from '../logged/genitore/genitore.component';
import {AnagraficaComponent} from '../logged/anagrafica/anagrafica.component';
import {NewUserComponent} from '../auth/new-user/new-user.component';
import {LineAdminComponent} from '../logged/line-admin/line-admin.component';
import {AdminGuideGuard} from '../auth/guards/admin-guide.guard';
import {NotAuthGuard} from '../auth/guards/not-auth.guard';

const appRoutes: Routes = [
  // {path: 'sign-up', component: SignUpComponent, canActivate: [AuthGuard], data: { animation: 'yes' }},
  {path: 'sign-in', component: SignInComponent, canActivate: [AuthGuard], data: {animation: 'yes'}},
  {path: 'line-admin', component: LineAdminComponent, canActivate: [AuthGuard], data: {animation: 'yes'}},
  {path: 'genitore', component: GenitoreComponent, canActivate: [AuthGuard], data: {animation: 'yes'}},
  {path: 'presenze', component: PresenzeComponent, canActivate: [AuthGuard, AdminGuideGuard], data: {animation: 'yes'}},
  {path: 'disponibilita', component: DisponibilitaComponent, canActivate: [AuthGuard, GuideGuard], data: {animation: 'yes'}},
  {path: 'notifiche', component: NotificheComponent, canActivate: [AuthGuard], data: {animation: 'yes'}},
  {path: 'turni', component: TurniComponent, canActivate: [AuthGuard, AdminGuard], data: {animation: 'yes'}},
  {path: 'anagrafica', component: AnagraficaComponent, canActivate: [AdminGuard], data: {animation: 'yes'}},
  {path: 'recover', component: ForgotPasswordComponent, canActivate: [NotAuthGuard], data: {animation: 'yes'}},
  {path: 'recover/:token', component: RecoverComponent, canActivate: [NotAuthGuard], data: {animation: 'yes'}},
  {path: 'new-user/:token', component: NewUserComponent, canActivate: [NotAuthGuard], data: {animation: 'yes'}},
  {path: 'confirm/:token', component: ConfirmComponent, canActivate: [NotAuthGuard], data: {animation: 'yes'}},
  {path: '', redirectTo: 'presenze', pathMatch: 'full', data: {animation: 'yes'}},
  {path: '404', component: PageNotFoundComponent, data: {animation: 'yes'}},
  {path: '**', redirectTo: '/404', data: {animation: 'yes'}}
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    AuthenticationModule,
    LoggedModule,
    HttpClientModule,
    RouterModule.forRoot(appRoutes)
  ], exports: [RouterModule]
})
export class AppRoutingModule {
}
