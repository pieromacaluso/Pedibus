import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../auth.service';
import {SignUpModel} from '../models';

@Component({
  selector: 'app-recover',
  templateUrl: './recover.component.html',
  styleUrls: ['./recover.component.scss']
})
export class RecoverComponent implements OnInit {

  model = {password: '', passMatch: ''};
  token;

  postStatus = false;
  confirmStatus = false;
  hidePass = true;
  hidePassAgain = true;

  constructor(private auth: AuthService, private route: ActivatedRoute, private router: Router) {
  }

  ngOnInit() {
    this.token = this.route.snapshot.paramMap.get('token');
  }


  isLoading() {
    return !this.postStatus;
  }

  isOk() {
    return this.confirmStatus && this.postStatus;
  }

  /**
   * Sottomissione del modello per recupero della password
   * @param event evento form
   */
  submit(event) {
    if (event.isTrusted) {
        this.auth.postNewPassword(this.token, this.model).subscribe((res) => {
          this.postStatus = true;
          this.confirmStatus = true;
        }, (error) => {
          this.postStatus = true;
          this.confirmStatus = false;
        });
    }
  }

  goToLogin() {
    this.router.navigate(['sign-in']);
  }
}
