import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.HexFormat
import kotlin.system.measureTimeMillis

fun main() {
    val serverUrl = "http://127.0.0.1:8080"
    val initialChunkSize = 65536 // Начальный размер чанка 64 КБ
    val maxAttempts = 5
    val connectTimeout = 5000 // 5 секунд
    val readTimeout = 5000 // 5 секунд
    val expectedHash = "e729338af8fa24513523721e86f84fc1dd18131a0dd91a4b2c995bca5eccfeee"

    println("Starting download from $serverUrl...")

    val totalTime = measureTimeMillis {
        val allData = downloadData(
            serverUrl = serverUrl,
            initialChunkSize = initialChunkSize,
            maxAttempts = maxAttempts,
            connectTimeout = connectTimeout,
            readTimeout = readTimeout
        )

        // Вычисляем SHA-256 хэш
        val hash = calculateSha256(allData)
        println("SHA-256 hash of downloaded data: $hash")

        // Сравниваем с ожидаемым хэшем
        if (hash.equals(expectedHash, ignoreCase = true)) {
            println("Hash matches! Data downloaded successfully.")
        } else {
            println("Hash does not match. Data may be corrupted.")
        }
    }

    println("Total download time: $totalTime ms")
}

// Основная функция для скачивания данных
fun downloadData(
    serverUrl: String,
    initialChunkSize: Int,
    maxAttempts: Int,
    connectTimeout: Int,
    readTimeout: Int
): ByteArray {
    val outputStream = ByteArrayOutputStream()
    var start = 0L
    var keepDownloading = true
    var chunkSize = initialChunkSize

    while (keepDownloading) {
        val end = start + chunkSize - 1
        val range = "bytes=$start-$end"

        val chunkResult = downloadChunk(
            serverUrl = serverUrl,
            range = range,
            maxAttempts = maxAttempts,
            connectTimeout = connectTimeout,
            readTimeout = readTimeout
        )

        if (chunkResult == null || chunkResult.isEmpty()) {
            keepDownloading = false
            continue
        }

        outputStream.write(chunkResult)

        // Если сервер вернул меньше данных, чем ожидалось, уменьшаем chunkSize
        val expectedSize = end - start + 1
        if (chunkResult.size.toLong() < expectedSize && chunkResult.size < chunkSize) {
            chunkSize = chunkSize / 2
            if (chunkSize < 1024) chunkSize = 1024 // Минимальный размер чанка
            println("Reduced chunkSize to $chunkSize due to incomplete download")
        }

        // Обновляем start на основе реального количества скачанных байт
        start += chunkResult.size
    }

    val downloadedSize = outputStream.size()
    println("Total downloaded size: $downloadedSize bytes")
    return outputStream.toByteArray()
}

// Скачивание одного чанка с повторными попытками
fun downloadChunk(
    serverUrl: String,
    range: String,
    maxAttempts: Int,
    connectTimeout: Int,
    readTimeout: Int
): ByteArray? {
    println("Downloading range: $range")

    var attempts = 0
    var chunk: ByteArray? = null

    while (attempts < maxAttempts) {
        attempts++
        val requestTime = measureTimeMillis {
            try {
                val url = URL(serverUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Range", range)
                connection.connectTimeout = connectTimeout
                connection.readTimeout = readTimeout
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    chunk = connection.inputStream.use { input -> input.readBytes() }
                    println("Downloaded ${chunk?.size ?: 0} bytes for range $range (attempt $attempts)")
                } else {
                    println("Unexpected response code $responseCode for range $range, stopping download")
                    return null
                }
                connection.disconnect()
            } catch (e: Exception) {
                println("Error downloading range $range (attempt $attempts): ${e.message}")
                return null
            }
        }

        if (chunk != null) {
            println("Request completed in $requestTime ms")
            if (chunk!!.isEmpty()) return null
            return chunk
        }
    }

    println("Failed to download range $range after $maxAttempts attempts")
    return null
}

// Вычисляем SHA-256 хэш
fun calculateSha256(data: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(data)
    return HexFormat.of().formatHex(hashBytes)
}