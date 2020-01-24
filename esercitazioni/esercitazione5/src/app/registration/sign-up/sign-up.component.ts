import { Component, OnInit } from '@angular/core';
import {SignUpModel} from '../models';
import {AuthService} from '../auth.service';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.css']
})
export class SignUpComponent implements OnInit {

  model: SignUpModel;
  serverErrors: string;

  constructor(private auth: AuthService) {
    this.model = {email: '', password: '', passMatch: ''};
  }

  ngOnInit() {
  }

  submit(event) {
    if (event.isTrusted) {
      let formValid = true;
      for (let count = 0; count < 4; count++) {
        if (!event.target[count].validity.valid) {
          formValid = false;
        }
      }
      if (formValid) {
        this.auth.signUp(this.model).subscribe((value => {
          console.log(value);
        } ), error1 => {
          this.serverErrors = (error1 as HttpErrorResponse).error.errorMessage;
        });
      }
    }
  }

}
