package com.clsma.vpnportal.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.TimeUnit

@Service
class VpnService(
    @Value("\${app.ovpn.clients-path}") private val clientsPath: String,
    @Value("\${app.ovpn.openvpn-volume}") private val openvpnVolume: String,
    @Value("\${app.ovpn.docker-image}") private val dockerImage: String,
) {
    private val logger = LoggerFactory.getLogger(VpnService::class.java)

    /**
     * Returns the .ovpn bytes for a given username.
     * Strategy: if file exists on filesystem → serve it directly.
     * If not → generate it via docker run kylemanna/openvpn.
     */
    fun getOvpnProfile(username: String): ByteArray? {
        val file = File("$clientsPath/$username.ovpn")

        return if (file.exists()) {
            logger.info("Serving existing .ovpn for user: $username")
            file.readBytes()
        } else {
            logger.info("Profile not found, generating .ovpn for user: $username")
            generateOvpnProfile(username)
        }
    }

    fun hasOvpnProfile(username: String): Boolean {
        val file = File("$clientsPath/$username.ovpn")
        if (file.exists()) return true

        // Check if certificate exists (user was created but .ovpn not exported yet)
        return certificateExists(username)
    }

    fun getVpnServerStatus(): Map<String, Any> {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("docker", "inspect", "--format", "{{.State.Running}}", "openvpn")
            )
            val running = process.waitFor(5, TimeUnit.SECONDS) &&
                    process.inputStream.bufferedReader().readText().trim() == "true"

            val clients = if (running) getConnectedClientCount() else 0

            mapOf(
                "serverRunning" to running,
                "connectedClients" to clients,
                "serverAddress" to "udp://clsma.com.co:1194"
            )
        } catch (e: Exception) {
            logger.warn("Could not get VPN status: ${e.message}")
            mapOf(
                "serverRunning" to false,
                "connectedClients" to 0,
                "serverAddress" to "udp://clsma.com.co:1194"
            )
        }
    }

    private fun generateOvpnProfile(username: String): ByteArray? {
        return try {
            // Step 1: Build client certificate (nopass = no password on the cert itself)
            val buildProcess = ProcessBuilder(
                "docker", "run", "--rm",
                "-v", "$openvpnVolume:/etc/openvpn",
                dockerImage,
                "easyrsa", "build-client-full", username, "nopass"
            ).redirectErrorStream(true).start()

            val buildExitCode = buildProcess.waitFor(60, TimeUnit.SECONDS)
            if (!buildExitCode || buildProcess.exitValue() != 0) {
                logger.error("Failed to build client cert for $username")
                logger.error(buildProcess.inputStream.bufferedReader().readText())
                return null
            }

            // Step 2: Export .ovpn
            val exportProcess = ProcessBuilder(
                "docker", "run", "--rm",
                "-v", "$openvpnVolume:/etc/openvpn",
                dockerImage,
                "ovpn_getclient", username
            ).redirectErrorStream(false).start()

            val ovpnContent = exportProcess.inputStream.readBytes()
            val exportExitCode = exportProcess.waitFor(30, TimeUnit.SECONDS)

            if (!exportExitCode || exportProcess.exitValue() != 0) {
                logger.error("Failed to export .ovpn for $username")
                return null
            }

            // Step 3: Append auth-user-pass directive
            val finalContent = ovpnContent + "\nauth-user-pass\n".toByteArray()

            // Step 4: Save to filesystem for future requests
            File(clientsPath).mkdirs()
            val outputFile = File("$clientsPath/$username.ovpn")
            outputFile.writeBytes(finalContent)
            logger.info("Generated and saved .ovpn for $username at ${outputFile.absolutePath}")

            finalContent
        } catch (e: Exception) {
            logger.error("Error generating .ovpn for $username: ${e.message}", e)
            null
        }
    }

    private fun certificateExists(username: String): Boolean {
        return try {
            val process = ProcessBuilder(
                "docker", "run", "--rm",
                "-v", "$openvpnVolume:/etc/openvpn",
                dockerImage,
                "ls", "/etc/openvpn/pki/issued/$username.crt"
            ).start()
            process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun getConnectedClientCount(): Int {
        return try {
            val process = ProcessBuilder(
                "docker", "exec", "openvpn",
                "sh", "-c", "cat /tmp/openvpn-status.log 2>/dev/null | grep -c '^CLIENT_LIST' || echo 0"
            ).start()
            process.waitFor(5, TimeUnit.SECONDS)
            process.inputStream.bufferedReader().readText().trim().toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
