package com.example.api.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.config.JwtConfig
import com.example.services.auth.TokenProvider
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respond
import org.koin.ktor.ext.inject
import java.util.UUID

fun Application.configureSecurity() {
    val jwtConfig by inject<JwtConfig>()
    val tokenProvider by inject<TokenProvider>()

    // Please read the jwt property from the config file if you are using EngineMain
    val jwtAudience = jwtConfig.jwtAudience
    val jwtDomain = "https://jwt-provider-domain/"
    val jwtRealm = jwtConfig.jwtRealm
    val jwtSecret = jwtConfig.jwtSecrete
    authentication {
        jwt {
            realm = jwtRealm
            verifier(tokenProvider.getVerifier())
            validate { credential ->
                val payload = credential.payload
                val userIdString = credential.payload.getClaim("userId").asString()
                val email = credential.payload.getClaim("email").asString()
                val role = credential.payload.getClaim("role").asString()

                if (userIdString != null && email != null && role != null) {
                    try {
                        //TO-DO: Check if user exists
                        JWTPrincipal(payload)
                    } catch (e: IllegalArgumentException) {
                        return@validate null
                    }
                } else {
                    return@validate null
                }
            }

            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or expired")
            }
        }
    }
}