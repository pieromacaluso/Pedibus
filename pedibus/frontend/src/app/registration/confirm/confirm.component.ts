import { Component, OnInit } from '@angular/core';
import {AuthService} from '../auth.service';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-confirm',
  templateUrl: './confirm.component.html',
  styleUrls: ['./confirm.component.scss']
})
export class ConfirmComponent implements OnInit {

  token;
  getStatus = false;
  confirmStatus = false;

  constructor(private auth: AuthService, private route: ActivatedRoute, private router: Router) {
  }

  ngOnInit() {
    this.token = this.route.snapshot.paramMap.get('token');
    this.auth.getConfirm(this.token).subscribe((res) => {
      this.getStatus = true;
      this.confirmStatus = true;
    }, (error) => {
      this.getStatus = true;
      this.confirmStatus = false;
    });
  }

  isLoading() {
    return !this.getStatus;
  }

  isOk() {
    return this.confirmStatus && this.getStatus;
  }

  goToLogin() {
    this.router.navigate(['sign-in']);
  }
}
