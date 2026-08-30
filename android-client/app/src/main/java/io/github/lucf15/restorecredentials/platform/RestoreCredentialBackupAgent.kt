package io.github.lucf15.restorecredentials.platform

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.startup.AppInitializer
import io.github.lucf15.restorecredentials.di.KoinInitializer
import io.github.lucf15.restorecredentials.domain.usecase.TryRestoreSignInUseCase
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val TAG = "RestoreCredentialBackupAgent"

class RestoreCredentialBackupAgent : BackupAgent(), KoinComponent {
    private val tryRestoreSignIn: TryRestoreSignInUseCase by inject()

    override fun onCreate() {
        super.onCreate()
        AppInitializer.getInstance(this).initializeComponent(KoinInitializer::class.java)
    }

    override fun onBackup(oldState: ParcelFileDescriptor?, data: BackupDataOutput?, newState: ParcelFileDescriptor?) = Unit

    override fun onRestore(data: BackupDataInput?, appVersionCode: Int, newState: ParcelFileDescriptor?) = Unit

    override fun onRestoreFinished() {
        super.onRestoreFinished()
        Log.d(TAG, "onRestoreFinished: fired")
        runBlocking {
            runCatching { tryRestoreSignIn() }
                .onSuccess { signedIn -> Log.d(TAG, "onRestoreFinished: tryRestoreSignIn completed, signedIn=$signedIn") }
                .onFailure { e -> Log.e(TAG, "onRestoreFinished: tryRestoreSignIn threw", e) }
        }
        Log.d(TAG, "onRestoreFinished: returning")
    }
}
