import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  signupForm!: UntypedFormGroup;
  submitted = false;
  loading = false;
  success = false;
  error = '';

  constructor(
    private readonly formBuilder: UntypedFormBuilder,
    private readonly router: Router,
    private readonly authenticationService: AuthenticationService
  ) {}

  ngOnInit(): void {
    this.signupForm = this.formBuilder.group({
      prenom: ['', Validators.required],
      nom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      tel: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  get fields() {
    return this.signupForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';
    if (this.signupForm.invalid || this.loading) {
      return;
    }

    this.loading = true;
    this.authenticationService.register(this.signupForm.getRawValue()).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
      },
      error: (error: unknown) => {
        this.error = error instanceof HttpErrorResponse && typeof error.error?.message === 'string'
          ? error.error.message
          : 'Unable to submit your request. Please try again.';
        this.loading = false;
      }
    });
  }

  backToLogin(): void {
    void this.router.navigate(['/auth/login']);
  }
}
