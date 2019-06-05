import { Component, OnInit } from '@angular/core';
import {RegisterModel} from './reg-interfaces';
import {RegisterService} from '../register.service';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  model: RegisterModel;
  serverErrors: string;

  constructor(private registerService: RegisterService) {
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
        this.registerService.register(this.model).subscribe((value => {
          // todo: do something on ok
          console.log(this.serverErrors);
        } ), error1 => {
          this.serverErrors = (error1 as HttpErrorResponse).error.errorMessage;
        });
      }
    }
  }

}
