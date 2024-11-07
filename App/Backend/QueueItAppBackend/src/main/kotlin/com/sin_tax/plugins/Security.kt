package com.sin_tax.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

const val jwtAudience = "QueueItApp"
const val jwtIssuer = "https://www.sin_tax.com"
const val jwtRealm = "ktor sample app"
const val jwtSecret = "secret"

fun Application.configureSecurity() {

    authentication {
        jwt("user-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("userId") != null) JWTPrincipal(credential.payload) else null
            }
        }
    }
}

fun createToken(userId: String): String {
    return JWT.create()
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .withClaim("userId", userId)
        .sign(Algorithm.HMAC256(jwtSecret))
}