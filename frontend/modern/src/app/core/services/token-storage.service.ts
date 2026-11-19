import { Injectable } from '@angular/core';
import { AuthResponse } from '../models/aurahub.models';

const TOKEN_KEY = 'token';
const USER_KEY = 'currentUser';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  signOut(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  saveToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  saveUser(user: AuthResponse): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  getUser(): AuthResponse | null {
    const storedUser = localStorage.getItem(USER_KEY);
    if (!storedUser) {
      return null;
    }

    try {
      const user: unknown = JSON.parse(storedUser);
      if (typeof user === 'object' && user !== null) {
        const record = user as Record<string, unknown>;
        if (
          typeof record['id'] === 'number'
          && typeof record['email'] === 'string'
          && typeof record['nom'] === 'string'
          && typeof record['prenom'] === 'string'
          && typeof record['token'] === 'string'
          && Array.isArray(record['roles'])
          && record['roles'].every(role => typeof role === 'string')
        ) {
          return user as AuthResponse;
        }
      }
      this.signOut();
      return null;
    } catch {
      this.signOut();
      return null;
    }
  }
}
