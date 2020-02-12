import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthService} from '../auth.service';


/**
 * Guardia che controlla sia loggato ed evita che questo vada ad accedere a pagine che non potrebbe visualizzare da loggato
 */
@Injectable({
  providedIn: 'root'
})
export class NotAuthGuard implements CanActivate {

  constructor(private auth: AuthService, private router: Router) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    // Check that the user is logged in...
    if (this.auth.isLoggedIn()) {
      this.router.navigate([this.auth.getHome()]);
      return false;
    }
    return true;

  }


}
