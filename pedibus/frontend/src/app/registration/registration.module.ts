import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormsModule} from '@angular/forms';

import { SignUpComponent } from './sign-up/sign-up.component';
import { SignInComponent } from './sign-in/sign-in.component';
import {MatButtonModule, MatCardModule, MatCheckboxModule, MatFormFieldModule, MatInputModule} from '@angular/material';
import {FlexLayoutModule} from '@angular/flex-layout';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import {RouterModule} from '@angular/router';
import { RecoverComponent } from './recover/recover.component';

@NgModule({
  declarations: [SignUpComponent, SignInComponent, ForgotPasswordComponent, RecoverComponent],
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatButtonModule,
    FormsModule,
    MatInputModule,
    MatCardModule,
    FlexLayoutModule,
    MatCheckboxModule,
    RouterModule
  ]
})
export class RegistrationModule { }
