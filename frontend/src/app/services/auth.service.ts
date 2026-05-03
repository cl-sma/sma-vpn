import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UserInfo {
  username: string;
  displayName: string;
  email: string;
  hasOvpnProfile: boolean;
}

interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  currentUser = signal<UserInfo | null>(null);
  isLoading = signal(false);

  login(username: string, password: string): Observable<ApiResponse<UserInfo>> {
    return this.http.post<ApiResponse<UserInfo>>(
      `${environment.apiUrl}/auth/login`,
      { username, password }
    ).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.currentUser.set(res.data);
        }
      })
    );
  }

  logout(): void {
    this.http.post(`${environment.apiUrl}/auth/logout`, {}).subscribe({
      complete: () => {
        this.currentUser.set(null);
        this.router.navigate(['/login']);
      }
    });
  }

  checkSession(): Observable<ApiResponse<UserInfo>> {
    return this.http.get<ApiResponse<UserInfo>>(`${environment.apiUrl}/auth/me`).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.currentUser.set(res.data);
        }
      })
    );
  }

  isAuthenticated(): boolean {
    return this.currentUser() !== null;
  }
}
