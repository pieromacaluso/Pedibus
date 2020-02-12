import {Component, OnInit} from '@angular/core';
import {SignUpModel} from '../models';
import {AuthService} from '../auth.service';
import {HttpErrorResponse} from '@angular/common/http';
import {MatSnackBar} from '@angular/material';
import {AbstractControl, AsyncValidatorFn, FormControl, FormGroup, ValidationErrors, Validators} from '@angular/forms';
import {map} from 'rxjs/operators';
import {Observable} from 'rxjs';

/**
 * @deprecated vecchio flusso dell'applicazione
 */
@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.scss']
})
/**
 * SignUpComponent
 *
 * Componente per la registrazione dell'utente.
 */
export class SignUpComponent implements OnInit {


  constructor(private auth: AuthService, private snackBar: MatSnackBar) {
    this.model = {email: '', password: '', passMatch: '', terms: false};
    this.form = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email], this.userValidator()),
      password: new FormControl('', [Validators.required,
        Validators.pattern(/^((?=.*[0-9])|(?=.*[@#$%^&+!=]))((?=.*[a-z])|(?=.*[A-Z]))(?=\S+$).{8,}$/)]),
      passMatch: new FormControl('', [Validators.required,
        Validators.pattern(/^((?=.*[0-9])|(?=.*[@#$%^&+!=]))((?=.*[a-z])|(?=.*[A-Z]))(?=\S+$).{8,}$/)]),
      terms: new FormControl('', [Validators.required])
    }, [SignUpComponent.passwordConfirming]);
  }

  model: SignUpModel;
  serverErrors: string;
  success = false;
  form: FormGroup;
  hidePass = true;
  hidePassAgain = true;


  static removeError(control: AbstractControl, error: string) {
    const err = control.errors; // get control errors
    if (err) {
      delete err[error]; // delete your own error
      if (!Object.keys(err).length) { // if no errors left
        control.setErrors(null); // set control errors to null making it VALID
      } else {
        control.setErrors(err); // controls got other errors so set them back
      }
    }
  }

  static addError(control: AbstractControl, error: string) {
    const err = control.errors; // get control errors
    if (err) {
      err[error] = true; // create your error on the already existing vector
      control.setErrors(err); // controls got other errors so set them back
      return true;
    } else {
      return false;
    }
  }

  static passwordConfirming(control: AbstractControl) {
    const password = control.get('password');
    const passMatch = control.get('passMatch');
    if (password && passMatch && password.value !== passMatch.value) {
      if (!SignUpComponent.addError(control.get('password'), 'notMatching')) {
        control.get('password').setErrors({notMatching: true});
      }
    } else {
      SignUpComponent.removeError(control.get('password'), 'notMatching');
    }
    return password && passMatch && password.value !== passMatch.value ? {notMatching: true} : null;
  }


  ngOnInit() {
  }

  submit() {
    if (this.form.valid) {
      const model = {
        email: this.form.controls.email.value, password: this.form.controls.password.value,
        passMatch: this.form.controls.passMatch.value, terms: this.form.controls.terms.value
      };
      this.auth.signUp(model).subscribe((value => {
        // this.snackBar.open('An email has been sent to your account', 'Undo', {duration: 7000});
        this.success = true;
      }), error1 => {
        this.serverErrors = (error1 as HttpErrorResponse).error.errorMessage;
      });
    }
  }

  userValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> => {
      return this.auth.checkDuplicate(control.value)
        .pipe(
          map(duplicate => (duplicate) ? {duplicate: true} : null)
        );
    };
  }
}
