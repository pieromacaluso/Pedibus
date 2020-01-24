import {Component, OnInit} from '@angular/core';
import {AuthService} from '../auth.service';
import {Router} from '@angular/router';
import {FormControl, FormGroup, Validators} from '@angular/forms';

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
        this.router.navigate([this.auth.getHome()]);
      }, (error) => {
        this.serverErrors = error.errorMessage;
      });
    }


  }
}
