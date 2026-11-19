import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { TokenStorageService } from '../services/token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(
    private readonly router: Router,
    private readonly tokenStorage: TokenStorageService
  ) {}

  canActivate(): boolean | UrlTree {
    if (this.tokenStorage.getToken() && this.tokenStorage.getUser()) {
      return true;
    }
    return this.router.createUrlTree(['/auth/login']);
  }
}
