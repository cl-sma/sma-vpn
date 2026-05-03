package com.clsma.vpnportal.controller

import com.clsma.vpnportal.model.ApiResponse
import com.clsma.vpnportal.model.VpnStatus
import com.clsma.vpnportal.service.VpnService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vpn")
class VpnController(private val vpnService: VpnService) {

    @GetMapping("/profile/download")
    fun downloadProfile(): ResponseEntity<ByteArray> {
        val username = SecurityContextHolder.getContext().authentication?.name
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val ovpnBytes = vpnService.getOvpnProfile(username)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        headers.setContentDispositionFormData("attachment", "$username.ovpn")
        headers.contentLength = ovpnBytes.size.toLong()

        return ResponseEntity(ovpnBytes, headers, HttpStatus.OK)
    }

    @GetMapping("/status")
    fun getStatus(): ApiResponse<VpnStatus> {
        val statusMap = vpnService.getVpnServerStatus()
        return ApiResponse(
            success = true,
            data = VpnStatus(
                serverRunning = statusMap["serverRunning"] as Boolean,
                connectedClients = statusMap["connectedClients"] as Int,
                serverAddress = statusMap["serverAddress"] as String
            )
        )
    }

    @GetMapping("/profile/exists")
    fun profileExists(): ApiResponse<Map<String, Boolean>> {
        val username = SecurityContextHolder.getContext().authentication?.name
            ?: return ApiResponse(success = false, message = "No autenticado")

        val exists = vpnService.hasOvpnProfile(username)
        return ApiResponse(success = true, data = mapOf("exists" to exists))
    }
}
