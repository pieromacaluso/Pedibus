import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthService} from './registration/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private auth: AuthService, private router: Router) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    // Check that the user is logged in...
    console.log(state.url);
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['sign-in']);
      return false;
    }
    if (this.auth.isLoggedIn() && state.url === '/sign-up') {
      this.router.navigate(['presenze']);
      return false;
    }
    return true;

  }

}
