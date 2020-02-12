import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

import {SignUpComponent} from './sign-up/sign-up.component';
import {SignInComponent} from './sign-in/sign-in.component';
import {
  MatButtonModule,
  MatCardModule,
  MatCheckboxModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatProgressSpinnerModule
} from '@angular/material';
import {FlexLayoutModule} from '@angular/flex-layout';
import {ForgotPasswordComponent} from './forgot-password/forgot-password.component';
import {RouterModule} from '@angular/router';
import {RecoverComponent} from './recover/recover.component';
import {ConfirmComponent} from './confirm/confirm.component';
import {AgmCoreModule} from '@agm/core';
import {NewUserComponent} from './new-user/new-user.component';

@NgModule({
  declarations: [SignUpComponent, SignInComponent, ForgotPasswordComponent, RecoverComponent, ConfirmComponent, NewUserComponent],
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatButtonModule,
    FormsModule,
    MatInputModule,
    MatCardModule,
    FlexLayoutModule,
    MatCheckboxModule,
    RouterModule,
    MatIconModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule,
    AgmCoreModule
  ]
})
export class AuthenticationModule {
}
