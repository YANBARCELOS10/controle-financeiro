package br.com.ysenerbyte.comandospro.data

import android.content.Context
import br.com.ysenerbyte.comandospro.BuildConfig
import br.com.ysenerbyte.comandospro.core.UserProgress
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object CloudSyncService {
    val isConfigured: Boolean get() = BuildConfig.FIREBASE_CONFIGURED

    fun sync(
        context: Context,
        progress: UserProgress,
        callback: (Result<String>) -> Unit
    ) {
        if (!isConfigured) {
            callback(Result.failure(IllegalStateException("Firebase ainda não configurado")))
            return
        }
        val app = FirebaseApp.initializeApp(context)
        if (app == null) {
            callback(Result.failure(IllegalStateException("Configuração Firebase inválida")))
            return
        }
        val auth = FirebaseAuth.getInstance(app)
        val upload: () -> Unit = upload@{
            val user = auth.currentUser
            if (user == null) {
                callback(Result.failure(IllegalStateException("Sessão anônima indisponível")))
                return@upload
            }
            val safeNickname = progress.nickname.trim().take(24).ifBlank { "Operador" }
            val data = mapOf(
                "nickname" to safeNickname,
                "xp" to progress.xp.coerceIn(0, 10_000_000),
                "quizBest" to progress.quizBest.coerceIn(0, 100),
                "productionCount" to progress.productionCount.coerceIn(0, 10_000_000),
                "completed" to progress.completed.sorted().take(200),
                "studiedModules" to progress.studiedModules.sorted().take(50),
                "appVersion" to BuildConfig.VERSION_NAME,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            FirebaseFirestore.getInstance(app)
                .collection("users")
                .document(user.uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener { callback(Result.success("Progresso sincronizado com segurança.")) }
                .addOnFailureListener { error -> callback(Result.failure(error)) }
        }

        if (auth.currentUser != null) {
            upload()
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener { upload() }
                .addOnFailureListener { error -> callback(Result.failure(error)) }
        }
    }
}
