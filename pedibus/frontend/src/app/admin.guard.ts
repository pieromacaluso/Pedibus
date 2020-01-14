import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, CanActivate, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from './registration/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(private auth: AuthService, private router: Router) {
  }

  canActivate(route: ActivatedRouteSnapshot,
              state: RouterStateSnapshot): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {

              if (!this.auth.isLoggedIn()) {
                this.router.navigate(['sign-in']);
                return false;
              }

              if (!this.auth.isAdmin()) {
                this.router.navigate([this.auth.getHome()]);
                return false;
              }

              return true;
  }

}
