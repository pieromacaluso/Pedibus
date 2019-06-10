import { Component, OnInit } from '@angular/core';
import {SignInModel, SignUpModel} from '../models';
import {AuthService} from '../auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.scss']
})
export class SignInComponent implements OnInit {

  model: SignInModel;
  serverErrors: string;

  constructor(private auth: AuthService, private router: Router) {
    this.model = {email: '', password: ''};
  }

  ngOnInit() {
  }

  submit(event) {
    if (event.isTrusted) {
      let formValid = true;
      for (let count = 0; count < 2; count++) {
        if (!event.target[count].validity.valid) {
          formValid = false;
        }
      }
      if (formValid) {
        this.auth.signIn(this.model).subscribe(response => {
          console.log('called');
          console.log('response: ' + response);
          this.router.navigate(['presenze']);
        }, (error) => {
          this.serverErrors = error.errorMessage;
        });
      }
    }
  }

}
