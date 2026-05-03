package com.clsma.vpnportal.controller

import com.clsma.vpnportal.model.ApiResponse
import com.clsma.vpnportal.model.LoginRequest
import com.clsma.vpnportal.model.UserInfo
import com.clsma.vpnportal.service.LdapService
import com.clsma.vpnportal.service.VpnService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val ldapService: LdapService,
    private val vpnService: VpnService,
) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse
    ): ApiResponse<UserInfo> {
        return try {
            val auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )
            SecurityContextHolder.getContext().authentication = auth

            // Persist in session
            val session = httpRequest.getSession(true)
            session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
            )

            val ldapUser = ldapService.findUserByUid(request.username)
            val hasProfile = vpnService.hasOvpnProfile(request.username)

            ApiResponse(
                success = true,
                data = UserInfo(
                    username = request.username,
                    displayName = ldapUser?.cn ?: request.username,
                    email = ldapUser?.mail ?: "",
                    hasOvpnProfile = hasProfile
                )
            )
        } catch (e: BadCredentialsException) {
            httpResponse.status = 401
            ApiResponse(success = false, message = "Credenciales incorrectas")
        } catch (e: Exception) {
            httpResponse.status = 500
            ApiResponse(success = false, message = "Error de autenticación: ${e.message}")
        }
    }

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): ApiResponse<Nothing> {
        request.getSession(false)?.invalidate()
        SecurityContextHolder.clearContext()
        return ApiResponse(success = true, message = "Sesión cerrada")
    }

    @GetMapping("/me")
    fun me(): ApiResponse<UserInfo> {
        val auth = SecurityContextHolder.getContext().authentication
            ?: return ApiResponse(success = false, message = "No autenticado")

        val username = auth.name
        val ldapUser = ldapService.findUserByUid(username)
        val hasProfile = vpnService.hasOvpnProfile(username)

        return ApiResponse(
            success = true,
            data = UserInfo(
                username = username,
                displayName = ldapUser?.cn ?: username,
                email = ldapUser?.mail ?: "",
                hasOvpnProfile = hasProfile
            )
        )
    }
}
