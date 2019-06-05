import {Component, OnInit} from '@angular/core';
import {LoginModel} from '../register/reg-interfaces';
import {RegisterService} from '../register.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  model: LoginModel;
  serverErrors: string;

  constructor(private registerService: RegisterService, private router: Router) {
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
        this.registerService.login(this.model).subscribe(response => {
          console.log(response);
          this.router.navigate(['/presenze']);
        }, (error) => {
          console.error(error);
        });
      }
    }
  }

}
