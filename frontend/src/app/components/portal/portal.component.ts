import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { VpnService, VpnStatus } from '../../services/vpn.service';

@Component({
  selector: 'app-portal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './portal.component.html',
  styleUrl: './portal.component.scss'
})
export class PortalComponent implements OnInit {
  auth = inject(AuthService);
  private vpnSvc = inject(VpnService);

  vpnStatus = signal<VpnStatus | null>(null);
  downloading = signal(false);
  downloadError = signal('');
  activeTab = signal<'download' | 'status' | 'instructions'>('download');

  readonly installSteps = {
    windows: [
      { step: '1', title: 'Descarga el perfil', desc: 'Haz clic en "Descargar perfil .ovpn" arriba.' },
      { step: '2', title: 'Instala OpenVPN Connect', desc: 'Descarga desde openvpn.net/client. Compatible con Windows 10/11.' },
      { step: '3', title: 'Importa el perfil', desc: 'Abre OpenVPN Connect → "+" → Import from file → selecciona tu .ovpn.' },
      { step: '4', title: 'Conéctate', desc: 'Ingresa tu usuario (email) y contraseña institucional al conectar.' },
    ],
    macos: [
      { step: '1', title: 'Descarga el perfil', desc: 'Haz clic en "Descargar perfil .ovpn" arriba.' },
      { step: '2', title: 'Instala OpenVPN Connect', desc: 'Descárgalo desde la Mac App Store o en openvpn.net/client.' },
      { step: '3', title: 'Importa el perfil', desc: 'Abre OpenVPN Connect → "+" → Import from file → selecciona tu .ovpn.' },
      { step: '4', title: 'Conéctate', desc: 'Ingresa tu usuario (email) y contraseña institucional al conectar.' },
    ],
    android: [
      { step: '1', title: 'Descarga el perfil', desc: 'Descarga el archivo .ovpn en tu dispositivo Android.' },
      { step: '2', title: 'Instala OpenVPN Connect', desc: 'Instálalo desde Google Play Store.' },
      { step: '3', title: 'Importa el perfil', desc: 'Abre la app → "+" → Import → From file → selecciona tu .ovpn.' },
      { step: '4', title: 'Conéctate', desc: 'Usa tu email y contraseña institucional.' },
    ],
    ios: [
      { step: '1', title: 'Descarga el perfil', desc: 'Descarga el archivo .ovpn en tu iPhone o iPad.' },
      { step: '2', title: 'Instala OpenVPN Connect', desc: 'Instálalo desde la App Store.' },
      { step: '3', title: 'Importa el perfil', desc: 'Abre el archivo .ovpn → "Open with OpenVPN".' },
      { step: '4', title: 'Conéctate', desc: 'Usa tu email y contraseña institucional.' },
    ]
  };

  selectedOS = signal<keyof typeof this.installSteps>('windows');

  ngOnInit(): void {
    this.loadStatus();
  }

  loadStatus(): void {
    this.vpnSvc.getStatus().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.vpnStatus.set(res.data);
        }
      }
    });
  }

  downloadProfile(): void {
    this.downloading.set(true);
    this.downloadError.set('');
    try {
      this.vpnSvc.downloadProfile();
      setTimeout(() => this.downloading.set(false), 2000);
    } catch {
      this.downloadError.set('Error al descargar. Intenta de nuevo.');
      this.downloading.set(false);
    }
  }

  logout(): void {
    this.auth.logout();
  }
}
