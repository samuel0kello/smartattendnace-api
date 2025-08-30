package com.example.web

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header

object RequestUtils {
    /**
     * Determine the public base URL from an incoming request.
     * Prefer X-Forwarded-Proto and X-Forwarded-Host (ngrok sets these) then fall back to Host + local scheme.
     * Returns a string like "https://4881ee814b07.ngrok-free.app" (no trailing slash).
     */
    fun requestBaseUrl(call: ApplicationCall): String {
        val forwardedProto = call.request.header("X-Forwarded-Proto")?.trim()
        val forwardedHost = call.request.header("X-Forwarded-Host")?.trim()
        val forwardedPort = call.request.header("X-Forwarded-Port")?.trim()
        val hostHeader = call.request.header("Host")?.trim()

        val scheme = forwardedProto ?: call.request.local.scheme
        val rawHost = when {
            forwardedHost != null -> forwardedHost
            hostHeader != null -> hostHeader
            else -> {
                val localHost = call.request.local.localHost
                val localPort = call.request.local.localPort
                if (isDefaultPortForScheme(localPort, scheme)) localHost else "$localHost:$localPort"
            }
        }

        // If forwardedPort is present and host does not already contain a port, append it when non-default.
        val host = if (forwardedPort != null && !rawHost.contains(":")) {
            val portInt = forwardedPort.toIntOrNull()
            if (portInt != null && !isDefaultPortForScheme(portInt, scheme)) "$rawHost:$portInt" else rawHost
        } else {
            rawHost
        }

        return "$scheme://${host.trimEnd('/')}"
    }

    private fun isDefaultPortForScheme(port: Int, scheme: String): Boolean =
        (scheme.equals("http", ignoreCase = true) && port == 80) ||
            (scheme.equals("https", ignoreCase = true) && port == 443)
}