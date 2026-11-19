import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { TokenStorageService } from '../services/token-storage.service';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  constructor(
    private readonly router: Router,
    private readonly tokenStorage: TokenStorageService
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const user = this.tokenStorage.getUser();
    const roles = route.data['roles'];
    if (!user || !Array.isArray(roles) || !roles.every((role: unknown) => typeof role === 'string')) {
      return this.router.createUrlTree(['/auth/login']);
    }

    if (roles.some((role: string) => user.roles.includes(role))) {
      return true;
    }

    const redirectTo = route.data['redirectTo'];
    return this.router.createUrlTree([typeof redirectTo === 'string' ? redirectTo : '/auth/login']);
  }
}
