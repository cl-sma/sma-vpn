import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface VpnStatus {
  serverRunning: boolean;
  connectedClients: number;
  serverAddress: string;
}

interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class VpnService {
  private http = inject(HttpClient);

  getStatus(): Observable<ApiResponse<VpnStatus>> {
    return this.http.get<ApiResponse<VpnStatus>>(`${environment.apiUrl}/vpn/status`);
  }

  downloadProfile(): void {
    // Use window.location to trigger download (blob download)
    const link = document.createElement('a');
    link.href = `${environment.apiUrl}/vpn/profile/download`;
    link.click();
  }
}
