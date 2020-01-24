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


  postStatus = false;
  confirmStatus = false;

  constructor(private auth: AuthService, private snackBar: MatSnackBar) {
  }

  ngOnInit() {
  }

  isLoading() {
    return !this.postStatus;
  }

  isOk() {
    return this.confirmStatus && this.postStatus;
  }

  submit(event) {
    if (event.isTrusted) {
      const formValid = event.target[0].validity.valid;
      if (formValid) {
        this.auth.postRecover(this.model.email).subscribe((res) => {
          this.postStatus = true;
          this.confirmStatus = true;
            // this.snackBar.open('An email has been sent to your account', 'Undo', {duration: 7000});
          }, (error1 => {
          this.postStatus = true;
          this.confirmStatus = false;
          })
        );
      }
    }
  }

  restart() {
    this.model.email = '';
    this.postStatus = false;
    this.confirmStatus = false;
  }
}
