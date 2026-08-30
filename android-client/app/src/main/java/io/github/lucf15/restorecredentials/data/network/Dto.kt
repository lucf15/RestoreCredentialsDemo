package io.github.lucf15.restorecredentials.data.network

import kotlinx.serialization.Serializable

@Serializable data class LoginRequestDto(val username: String, val password: String)

@Serializable data class TokenPairResponseDto(val accessToken: String, val refreshToken: String, val username: String)

@Serializable data class RefreshRequestDto(val refreshToken: String)

@Serializable data class RefreshResponseDto(val accessToken: String, val refreshToken: String)

@Serializable data class RestoreRegistrationOptionsResponseDto(val requestJson: String)

@Serializable data class RestoreRegistrationVerifyRequestDto(val registrationResponseJson: String)

@Serializable data class RestoreAuthenticationOptionsResponseDto(val requestId: String, val requestJson: String)

@Serializable data class RestoreAuthenticationVerifyRequestDto(val requestId: String, val authenticationResponseJson: String)
