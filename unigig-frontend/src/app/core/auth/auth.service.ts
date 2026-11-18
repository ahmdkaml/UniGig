import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UserRole,
} from '../models/auth.models';

const TOKEN_KEY = 'unigig_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  // must match the backend controller's @RequestMapping path
  private readonly baseUrl = '/api/auth';

  private readonly tokenSignal = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  readonly isAuthenticated = computed(() => this.tokenSignal() !== null);
  readonly currentUser = computed(() => {
    const token = this.tokenSignal();
    if (!token) return null;
    try {
      const payload = JSON.parse(
        atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
      ) as { sub?: string; userId?: number; role?: UserRole };
      return payload.sub && payload.role
        ? { userId: payload.userId ?? 0, email: payload.sub, role: payload.role }
        : null;
    } catch {
      return null;
    }
  });
  
  register(payload: RegisterRequest): Observable<AuthResponse> {
    // must match the backend controller's @PostMapping path
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, payload).pipe(
      tap((res) => {
        localStorage.setItem(TOKEN_KEY, res.token);
        this.tokenSignal.set(res.token);
      })
    );
  }

  login(payload: LoginRequest): Observable<AuthResponse> {
    // must match the backend controller's @PostMapping path
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, payload).pipe(
      tap((res) => {
        localStorage.setItem(TOKEN_KEY, res.token);
        this.tokenSignal.set(res.token);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.tokenSignal.set(null);
  }

  getToken(): string | null {
    return this.tokenSignal();
  }
}
