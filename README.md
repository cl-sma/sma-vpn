# VPN Portal — clsma.com.co

Portal web para que los usuarios gestionen su acceso VPN. Autenticación via LDAP (LLDAP), descarga de perfil `.ovpn`, estado del servidor y guías de instalación.

## Stack

| Capa | Tecnología |
|---|---|
| Frontend | Angular 20+, nginx |
| Backend | Spring Boot 3 / Java 21, Kotlin |
| Auth | LDAP → LLDAP (misma red Docker) |
| Deploy | Docker Compose en Dokploy |
| Dominio | `vpn.clsma.com.co` via Traefik |

## Estructura

```
vpn-portal/
├── backend/            Spring Boot (Kotlin)
│   ├── src/
│   ├── Dockerfile
│   └── build.gradle.kts
├── frontend/           Angular
│   ├── src/
│   ├── nginx.conf
│   └── Dockerfile
└── docker-compose.yml
```

## Despliegue en Dokploy

### 1. Variables de entorno requeridas

En Dokploy → Settings → Environment del proyecto, añadir:

```env
LLDAP_ADMIN_PASSWORD=<contraseña del admin de LLDAP>
```

### 2. Redes Docker requeridas

El compose asume que ya existen estas redes externas (de tu stack de identidad):

- `identity_network` — para comunicación con lldap
- `dokploy-network` — para Traefik

### 3. DNS

Apuntar `vpn.clsma.com.co` → `200.7.106.32` en tu proveedor DNS.

### 4. Volumen

El backend necesita acceso al volumen de OpenVPN:
```
/opt/services/auth/vpn   → contiene la PKI y /clients/*.ovpn
```

Este path ya existe en tu servidor desde la configuración de OpenVPN.

### 5. Docker socket

El backend monta `/var/run/docker.sock` en read-only para poder ejecutar:
```bash
docker run kylemanna/openvpn easyrsa build-client-full USERNAME nopass
docker run kylemanna/openvpn ovpn_getclient USERNAME
```
Esto permite **generar automáticamente** el perfil `.ovpn` si no existe todavía.

## Flujo de autenticación

```
Usuario → vpn.clsma.com.co
    → POST /api/auth/login { username, password }
    → Spring Security autentica contra LLDAP via LDAP
    → Session cookie HttpOnly
    → GET /api/vpn/profile/download  (solo el .ovpn del usuario autenticado)
```

## Endpoints del backend

| Método | Path | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Login con credenciales LDAP |
| POST | `/api/auth/logout` | Cerrar sesión |
| GET | `/api/auth/me` | Info del usuario actual |
| GET | `/api/vpn/profile/download` | Descargar `.ovpn` propio |
| GET | `/api/vpn/status` | Estado del servidor OpenVPN |
| GET | `/api/vpn/profile/exists` | Verificar si tiene perfil |

## Desarrollo local

```bash
# Backend
cd backend
./gradlew bootRun

# Frontend
cd frontend
npm install
npm start   # incluye proxy a localhost:8080
```
