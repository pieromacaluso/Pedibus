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
import {AuthService} from './registration/auth.service';
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
          let errorMessage = '';
          if (error.error instanceof ErrorEvent) {
            // client-side error
            errorMessage = `Error: ${error.error.message}`;
          } else {
            // server-side error
            errorMessage = `Error Code: ${error.error.errorMessage}`;
          }
          console.log(error);
          this.snackBar.open(
            errorMessage, '', {
              duration: 10000,
            });
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
