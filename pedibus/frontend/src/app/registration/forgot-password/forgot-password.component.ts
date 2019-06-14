import {Component, OnInit} from '@angular/core';
import {AuthService} from '../auth.service';
import {MatSnackBar} from '@angular/material';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent implements OnInit {

  model = {email: ''};

  constructor(private auth: AuthService, private snackBar: MatSnackBar) {
  }

  ngOnInit() {
  }

  submit(event) {
    if (event.isTrusted) {
      const formValid = event.target[0].validity.valid;
      if (formValid) {
        console.log('form valid');
        this.auth.postRecover(this.model.email).subscribe((res) => {
            this.snackBar.open('An email has been sent to your account', 'Undo', {duration: 7000});
          }, (error1 => {
            console.log(error1);
          })
        );
      }
    }
  }
}
