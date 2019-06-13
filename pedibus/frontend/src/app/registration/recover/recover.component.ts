import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {AuthService} from '../auth.service';
import {SignUpModel} from '../models';

@Component({
  selector: 'app-recover',
  templateUrl: './recover.component.html',
  styleUrls: ['./recover.component.css']
})
export class RecoverComponent implements OnInit {

  model = {email: '', password: '', passMatch: ''};
  token;

  constructor(private auth: AuthService, private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.token = this.route.snapshot.paramMap.get('token');
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
        this.auth.postNewPassword(this.token, this.model).subscribe((res) => {
          console.log('new pass:', res);
        }, (error) => console.log(error));
      }
    }
  }
}
