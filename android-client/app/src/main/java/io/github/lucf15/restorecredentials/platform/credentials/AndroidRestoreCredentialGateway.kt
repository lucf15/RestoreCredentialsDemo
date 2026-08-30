package io.github.lucf15.restorecredentials.platform.credentials

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CreateRestoreCredentialRequest
import androidx.credentials.CreateRestoreCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetRestoreCredentialOption
import androidx.credentials.RestoreCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.restorecredential.E2eeUnavailableException
import io.github.lucf15.restorecredentials.domain.repository.RestoreCredentialGateway
import io.github.lucf15.restorecredentials.domain.repository.RestoreSignInOutcome

class AndroidRestoreCredentialGateway(context: Context) : RestoreCredentialGateway {
    private val appContext = context.applicationContext
    private val credentialManager = CredentialManager.create(appContext)

    override suspend fun register(creationOptionsJson: String): String {
        val response =
            try {
                credentialManager.createCredential(appContext, CreateRestoreCredentialRequest(creationOptionsJson, isCloudBackupEnabled = true))
            } catch (e: E2eeUnavailableException) {
                credentialManager.createCredential(appContext, CreateRestoreCredentialRequest(creationOptionsJson, isCloudBackupEnabled = false))
            } catch (e: Exception) {
                Log.e("RestoreCredentialGateway", "createCredential failed for requestJson=$creationOptionsJson", e)
                throw e
            }
        return (response as CreateRestoreCredentialResponse).responseJson
    }

    override suspend fun signIn(requestOptionsJson: String): RestoreSignInOutcome {
        val request = GetCredentialRequest(listOf(GetRestoreCredentialOption(requestOptionsJson)))
        val response =
            try {
                credentialManager.getCredential(appContext, request)
            } catch (e: GetCredentialException) {
                return RestoreSignInOutcome.NotAvailable
            }
        return RestoreSignInOutcome.Available((response.credential as RestoreCredential).authenticationResponseJson)
    }

    override suspend fun clear() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest(ClearCredentialStateRequest.TYPE_CLEAR_RESTORE_CREDENTIAL))
    }
}
