package com.clsma.vpnportal.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class UserInfo(
    val username: String,
    val displayName: String,
    val email: String,
    val hasOvpnProfile: Boolean
)

data class VpnStatus(
    val serverRunning: Boolean,
    val connectedClients: Int,
    val serverAddress: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)
