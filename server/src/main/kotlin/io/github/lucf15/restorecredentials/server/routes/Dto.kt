package io.github.lucf15.restorecredentials.server.routes

import kotlinx.serialization.Serializable

@Serializable data class LoginRequest(val username: String, val password: String)

@Serializable data class TokenPairResponse(val accessToken: String, val refreshToken: String, val username: String)

@Serializable data class RefreshRequest(val refreshToken: String)

@Serializable data class LogoutRequest(val refreshToken: String)

@Serializable data class RefreshResponse(val accessToken: String, val refreshToken: String)

@Serializable data class MeResponse(val userId: String, val username: String)

@Serializable data class RestoreRegistrationOptionsResponse(val requestJson: String)

@Serializable data class RestoreRegistrationVerifyRequest(val registrationResponseJson: String)

@Serializable data class RestoreAuthenticationOptionsResponse(val requestId: String, val requestJson: String)

@Serializable data class RestoreAuthenticationVerifyRequest(val requestId: String, val authenticationResponseJson: String)
