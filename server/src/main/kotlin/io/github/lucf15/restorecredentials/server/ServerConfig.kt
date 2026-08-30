package io.github.lucf15.restorecredentials.server

import java.security.SecureRandom
import java.util.Base64

class ServerConfig(
    val port: Int,
    val rpId: String,
    val rpName: String,
    val origins: Set<String>,
    val jwtSecret: String,
    val jwtIssuer: String,
) {
    companion object {
        fun fromEnvironment(): ServerConfig {
            val rpId = System.getenv("RP_ID") ?: "localhost"
            val origin = System.getenv("RP_ORIGIN") ?: "https://$rpId"
            val androidOrigins = System.getenv("ANDROID_ORIGINS")?.split(",")?.map(String::trim)?.toSet() ?: emptySet()
            return ServerConfig(
                port = System.getenv("PORT")?.toIntOrNull() ?: 8080,
                rpId = rpId,
                rpName = System.getenv("RP_NAME") ?: "Restore Credentials Demo",
                origins = setOf(origin) + androidOrigins,
                jwtSecret = System.getenv("JWT_SECRET") ?: randomSecret(),
                jwtIssuer = "restore-credentials-demo",
            )
        }

        private fun randomSecret(): String {
            val bytes = ByteArray(32).also(SecureRandom()::nextBytes)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        }
    }
}
