import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthenticationService } from '../../core/services/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm!: UntypedFormGroup;
  submitted = false;
  loading = false;
  error = '';
  returnUrl = '/';
  readonly year = new Date().getFullYear();

  constructor(
    private readonly formBuilder: UntypedFormBuilder,
    private readonly authenticationService: AuthenticationService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    if (this.authenticationService.currentUserValue) {
      void this.router.navigate(['/']);
      return;
    }

    this.loginForm = this.formBuilder.group({
      email: ['admin@aura.com', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    const requestedUrl = this.route.snapshot.queryParams['returnUrl'];
    if (typeof requestedUrl === 'string' && requestedUrl.startsWith('/') && !requestedUrl.startsWith('//')) {
      this.returnUrl = requestedUrl;
    }
  }

  get fields() {
    return this.loginForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';
    if (this.loginForm.invalid || this.loading) {
      return;
    }

    this.loading = true;
    this.authenticationService.login(this.fields['email'].value, this.fields['password'].value).subscribe({
      next: response => {
        this.authenticationService.saveSession(response);
        void this.router.navigateByUrl(this.returnUrl);
      },
      error: (error: unknown) => {
        this.error = this.getErrorMessage(error);
        this.loading = false;
      }
    });
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (typeof error.error?.message === 'string') {
        return error.error.message;
      }
      if (error.status === 401) {
        return 'Email or password is incorrect.';
      }
      if (error.status === 403) {
        return 'This account has not been activated yet. Please contact an administrator.';
      }
      if (error.status === 0) {
        return `The Aura Hub API is unavailable. Check that the backend is running at ${environment.apiUrl}.`;
      }
    }
    return 'Unable to sign in. Please try again.';
  }
}
