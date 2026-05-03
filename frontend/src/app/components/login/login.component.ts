import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  username = '';
  password = '';
  error = signal('');
  loading = signal(false);

  onSubmit(): void {
    if (!this.username || !this.password) return;

    this.loading.set(true);
    this.error.set('');

    this.auth.login(this.username, this.password).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.success) {
          this.router.navigate(['/portal']);
        } else {
          this.error.set(res.message ?? 'Error al iniciar sesión');
        }
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 401) {
          this.error.set('Usuario o contraseña incorrectos');
        } else {
          this.error.set('Error de conexión. Intenta de nuevo.');
        }
      }
    });
  }
}
