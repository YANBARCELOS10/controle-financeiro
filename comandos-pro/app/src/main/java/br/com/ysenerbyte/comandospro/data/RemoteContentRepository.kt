package br.com.ysenerbyte.comandospro.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class RemoteContentPack(
    val revision: Int,
    val headline: String,
    val message: String,
    val tips: List<String>,
    val releaseUrl: String,
    val updatedAt: String
)

class RemoteContentRepository(context: Context) {
    private val preferences = context.getSharedPreferences("remote_content_v3", Context.MODE_PRIVATE)

    fun loadCached(): RemoteContentPack = runCatching {
        decode(preferences.getString(KEY_JSON, null).orEmpty())
    }.getOrElse { defaultPack() }

    fun refresh(): Result<RemoteContentPack> = runCatching {
        val connection = (URL(CONTENT_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            useCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "ComandosPro3D/3.0")
        }
        try {
            if (connection.responseCode !in 200..299) {
                error("Servidor respondeu ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            require(body.length <= MAX_CONTENT_BYTES) { "Pacote remoto acima do limite" }
            val pack = decode(body)
            preferences.edit { putString(KEY_JSON, body) }
            pack
        } finally {
            connection.disconnect()
        }
    }

    private fun decode(raw: String): RemoteContentPack {
        val json = JSONObject(raw)
        val tipsJson = json.optJSONArray("tips")
        val tips = buildList {
            if (tipsJson != null) {
                for (index in 0 until minOf(tipsJson.length(), 12)) {
                    tipsJson.optString(index).trim().take(240).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }
        return RemoteContentPack(
            revision = json.optInt("revision", 1).coerceIn(1, 1_000_000),
            headline = json.optString("headline", "Conteúdo profissional").trim().take(80),
            message = json.optString("message", "Material disponível offline.").trim().take(500),
            tips = tips.ifEmpty { defaultPack().tips },
            releaseUrl = json.optString("releaseUrl", DEFAULT_RELEASE_URL).takeIf {
                it.startsWith("https://github.com/YANBARCELOS10/controle-financeiro/")
            } ?: DEFAULT_RELEASE_URL,
            updatedAt = json.optString("updatedAt", "offline").trim().take(40)
        )
    }

    private fun defaultPack() = RemoteContentPack(
        revision = 1,
        headline = "Conteúdo offline disponível",
        message = "Conecte-se e toque em Atualizar conteúdo para buscar novidades publicadas no Git.",
        tips = listOf(
            "Use o simulador para comparar estados, não como instrução de montagem real.",
            "No diagnóstico virtual, siga alimentação, entrada, lógica, saída e atuador.",
            "Estrela e triângulo nunca podem permanecer ativos simultaneamente."
        ),
        releaseUrl = DEFAULT_RELEASE_URL,
        updatedAt = "incluído no app"
    )

    companion object {
        private const val KEY_JSON = "content_pack_json"
        private const val MAX_CONTENT_BYTES = 200_000
        private const val CONTENT_URL =
            "https://raw.githubusercontent.com/YANBARCELOS10/controle-financeiro/agent/comandos-pro-firebase/comandos-pro/content/content-pack.json"
        private const val DEFAULT_RELEASE_URL =
            "https://github.com/YANBARCELOS10/controle-financeiro/releases"
    }
}
