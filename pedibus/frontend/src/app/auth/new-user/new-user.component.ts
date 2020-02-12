import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../auth.service';
import {SignUpModel} from '../models';

@Component({
  selector: 'app-new-user',
  templateUrl: './new-user.component.html',
  styleUrls: ['./new-user.component.scss']
})
export class NewUserComponent implements OnInit {

  model = {password: '', passMatch: ''};
  token;

  postStatus = false;
  confirmStatus = false;
  hideOldPassword = true;
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
   * Sottomissione del modello per attivare l'utente
   * @param event evento form
   */
  submit(event) {
    if (event.isTrusted) {
        this.auth.newUserPasswordChange(this.token, this.model).subscribe((res) => {
          this.postStatus = true;
          this.confirmStatus = true;
        }, (error) => {
          this.postStatus = true;
          this.confirmStatus = false;
        });
      }
  }

  /**
   * Vai al login
   */
  goToLogin() {
    this.router.navigate(['sign-in']);
  }
}
