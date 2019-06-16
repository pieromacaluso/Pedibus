import {Component, OnInit} from '@angular/core';
import {SignInModel, SignUpModel} from '../models';
import {AuthService} from '../auth.service';
import {Router} from '@angular/router';
import {AbstractControl, FormBuilder, FormControl, FormGroup, ValidatorFn, Validators} from '@angular/forms';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.scss']
})
export class SignInComponent implements OnInit {

  serverErrors: string;
  forgotLink = '../recover';
  form: FormGroup;

  constructor(private auth: AuthService, private router: Router) {
    this.form = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required,
        Validators.pattern(/^((?=.*[0-9])|(?=.*[@#$%^&+!=]))((?=.*[a-z])|(?=.*[A-Z]))(?=\S+$).{8,}$/)])
    });
  }

  ngOnInit() {
  }

  submit() {
    if (this.form.valid) {
      const model = {email: this.form.controls.email.value, password:  this.form.controls.password.value};
      this.auth.signIn(model).subscribe(response => {
        console.log('called');
        console.log('response: ' + response);
        this.router.navigate(['presenze']);
      }, (error) => {
        this.serverErrors = error.errorMessage;
      });
    }


  }
}
