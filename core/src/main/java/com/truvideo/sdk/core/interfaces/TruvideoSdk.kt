package com.truvideo.sdk.core.interfaces

/**
 * Interface defining the contract for Truvideo SDK authentication.
 */
interface TruvideoSdk {
    /**
     * Checks if the user is authenticated with the Truvideo SDK.
     *
     * @return `true` if the user is authenticated; otherwise, `false`.
     */
    val isAuthenticated: Boolean

    /**
     * Checks if the authentication with the Truvideo SDK has expired.
     *
     * @return `true` if the authentication has expired; otherwise, `false`.
     */
    val isAuthenticationExpired: Boolean

    val apiKey: String

    /**
     * Generates a payload for authentication.
     *
     * @return A payload string for authentication.
     */
    fun generatePayload(): String

    /**
     * Asynchronously authenticates the user with the Truvideo SDK.
     *
     * @param apiKey The API key for authentication.
     * @param payload The payload for authentication.
     * @param signature The signature for authentication.
     */
    suspend fun authenticate(
        apiKey: String,
        payload: String,
        signature: String,
    )

    fun authenticate(
        apiKey: String,
        payload: String,
        signature: String,
        callback: TruvideoSdkAuthenticationCallback
    )

    /**
     * Initializes the Truvideo SDK.
     */
    suspend fun init()

    fun init(callback: TruvideoSdkInitCallback)

    /**
     * Clears the authentication data and session for the Truvideo SDK.
     */
    fun clear()

    val environment: String
}
