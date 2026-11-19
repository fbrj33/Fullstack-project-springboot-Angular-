import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse } from '../models/aurahub.models';
import { TokenStorageService } from './token-storage.service';

export interface RegisterRequest {
  email: string;
  password: string;
  nom: string;
  prenom: string;
  tel: string;
}

@Injectable({ providedIn: 'root' })
export class AuthenticationService {
  private readonly apiUrl = environment.apiUrl;

  constructor(
    private readonly http: HttpClient,
    private readonly tokenStorage: TokenStorageService
  ) {}

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, { email, password });
  }

  register(request: RegisterRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/auth/register`, request);
  }

  currentUser(): AuthResponse | null {
    return this.tokenStorage.getToken() ? this.tokenStorage.getUser() : null;
  }

  get currentUserValue(): AuthResponse | null {
    return this.currentUser();
  }

  saveSession(response: AuthResponse): void {
    this.tokenStorage.saveToken(response.token);
    this.tokenStorage.saveUser(response);
  }

  logout(): void {
    this.tokenStorage.signOut();
  }
}
