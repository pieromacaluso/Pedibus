import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
  HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {retry, catchError} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material';
import {Injectable} from '@angular/core';
import {AuthService} from '../auth/auth.service';
import {Router} from '@angular/router';

@Injectable()
export class HttpExceptionsInterceptor implements HttpInterceptor {
  constructor(private snackBar: MatSnackBar, private auth: AuthService, private router: Router) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request)
      .pipe(
        retry(1),
        catchError((error: HttpErrorResponse) => {
          // Errori Unauthorized fanno logout e navigano verso sign-in
          let errorMessage = '';
          if (error.error instanceof ErrorEvent) {
            // client-side error
            errorMessage = `Error: ${error.error.message}`;
          } else {
            // server-side error
            errorMessage = `${error.error.errorMessage}`;
          }

          switch (error.status) {
            case 401:
              this.snackBar.open(
                error.error.errorMessage ? errorMessage : 'Non autorizzato', '', {
                  duration: 5000,
                });
              this.auth.logout();
              this.router.navigate(['/sign-in']);
              break;
            case 404:
              break;
            case 403:
              // TODO: LOGOUT?
              this.snackBar.open(
                'Non hai i permessi per effettuare l\'operazione', '', {
                  duration: 5000,
                });
              break;
            case 504:
              this.snackBar.open(
                'Errore di Rete, Server non disponibile', '', {
                  duration: 5000,
                });
              this.auth.logout();
              this.router.navigate(['/sign-in']);
              break;
            default:
              this.snackBar.open(
                error.error.errorMessage ? errorMessage : 'Si è verificato un errore', '', {
                  duration: 5000,
                });
          }
          const timer = JSON.parse(localStorage.getItem('expires_at'));
          if (timer && (Date.now() > timer)) {
            this.auth.logout();
            this.router.navigate(['/sign-in']);
          }
          return throwError(errorMessage);
        })
      );
  }

}
