package io.github.cyprienj.sporz.engine

import kotlinx.serialization.json.Json

/**
 * JSON settings shared by everything the engine serializes: saved games on
 * the MJ phone (ticket B9) and, if the backend reuses the engine, its
 * payloads. Unknown keys are ignored so an older app can read newer saves.
 */
public val EngineJson: Json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    explicitNulls = false
    classDiscriminator = "type"
}
