# GlitchyClient

## Overview

`GlitchyClient` is a Kotlin HTTP client that downloads data from a "glitchy" server using HTTP Range requests. It handles incomplete responses, retries failed requests, and verifies data integrity with SHA-256 hash comparison. The client uses parallel downloads to improve performance.

## Prerequisites

- Java 11 or higher
- Kotlin 1.9 or higher
- Gradle
- Kotlin Coroutines (`org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3`)
- A running server on `http://127.0.0.1:8080` (e.g., `buggy_server.py`)

## Setup

### 1. Clone the Repository
```bash
git clone https://github.com/kireeva-ma/glitchyClient.git
cd GlitchyClient
```

### 2. Start the Server
1. Run the server (e.g., `buggy_server.py`):
   ```bash
   python3 buggy_server.py
   ```
2. Note the SHA-256 hash from the server output (e.g., `e729338af8fa24513523721e86f84fc1dd18131a0dd91a4b2c995bca5eccfeee`).
3. Update the `expectedHash` in `GlitchyClient.kt`:
   ```kotlin
   val expectedHash = "e729338af8fa24513523721e86f84fc1dd18131a0dd91a4b2c995bca5eccfeee"
   ```

### 3. Add Dependencies
Add Kotlin Coroutines to `build.gradle`:
```gradle
dependencies {
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3"
}
```

### 4. Build and Run
- Build the project:
  ```bash
  ./gradlew build
  ```
- Run the client in IntelliJ IDEA:
    1. Open `GlitchyClient.kt`.
    2. Right-click and select `Run 'GlitchyClientKt'`.

## Configuration

Adjust these settings in `GlitchyClient.kt` if needed:
- `initialChunkSize`: Chunk size (default: 65536 bytes).
- `maxAttempts`: Retry attempts (default: 5).
- `connectTimeout` and `readTimeout`: Timeout in ms (default: 2000 ms).
- `parallelDownloads`: Number of parallel downloads (default: 4).

## Example Output
```
Starting download from http://127.0.0.1:8080...
Downloading range: bytes=0-65535
Downloaded 65535 bytes for range bytes=0-65535 (attempt 1)
Request completed in 5000 ms
...
Total downloaded size: 774967 bytes
SHA-256 hash of downloaded data: e729338af8fa24513523721e86f84fc1dd18131a0dd91a4b2c995bca5eccfeee
Hash matches! Data downloaded successfully.
Total download time: 82000 ms
```

## Troubleshooting

- **Hash mismatch:** Check if `expectedHash` matches the server's hash. Try reducing `initialChunkSize` to 32768 or increasing `maxAttempts` to 10.
- **Client hangs:** Ensure the server is running. Reduce `connectTimeout` and `readTimeout` to 1000 ms.
- **Slow download:** Increase `parallelDownloads` to 8.
