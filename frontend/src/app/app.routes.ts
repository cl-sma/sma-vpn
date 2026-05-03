import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'portal',
    loadComponent: () => import('./components/portal/portal.component').then(m => m.PortalComponent),
    canActivate: [authGuard]
  },
  { path: '', redirectTo: '/portal', pathMatch: 'full' },
  { path: '**', redirectTo: '/portal' }
];
