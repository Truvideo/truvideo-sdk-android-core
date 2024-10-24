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
    fun isAuthenticated(): Boolean

    /**
     * Checks if the authentication with the Truvideo SDK has expired.
     *
     * @return `true` if the authentication has expired; otherwise, `false`.
     */
    fun isAuthenticationExpired(): Boolean

    /**
     * The API key for the Truvideo SDK.
     *
     * This property holds the current API key used for authentication with the Truvideo SDK.
     * If the user is not authenticated, it returns an empty string. It should be checked
     * before making any SDK calls that require authentication.
     *
     * @return The current API key as a `String` if authenticated; otherwise, an empty string.
     */
    fun getApiKey(): String

    suspend fun handleAuthentication(
        apiKey: String,
        externalId: String = "",
        signatureProvider: TruvideoSdkSignatureProvider,
    )

    suspend fun handleAuthentication(
        apiKey: String,
        externalId: String = "",
        signatureProvider: TruvideoSdkSignatureProvider,
        callback: TruvideoSdkCallback<Unit>
    )

    /**
     * Generates a payload for authentication.
     *
     * @return A payload string for authentication.
     */
    fun generatePayload(): String

    /**
     * Authenticates the user with the Truvideo SDK.
     *
     * @param apiKey The API key for authentication.
     * @param payload The payload for authentication.
     * @param signature The signature for authentication.
     */
    suspend fun authenticate(
        apiKey: String,
        payload: String,
        signature: String,
        accessTokenTTL: Long? = null,
        refreshTokenTTL: Long? = null,
        externalId: String = ""
    )

    /**
     * Authenticates the user with the Truvideo SDK.
     *
     * @param apiKey The API key for authentication.
     * @param payload The payload for authentication.
     * @param signature The signature for authentication.
     */
    fun authenticate(
        apiKey: String,
        payload: String,
        signature: String,
        externalId: String = "",
        callback: TruvideoSdkCallback<Unit>,
    )

    /**
     * Initializes the Truvideo SDK Authentication.
     */
    suspend fun initAuthentication(
        accessTokenTTL: Long? = null,
        refreshTokenTTL: Long? = null
    )

    /**
     * Initializes the Truvideo SDK Authentication.
     */
    fun initAuthentication(callback: TruvideoSdkCallback<Unit>)

    /**
     * Clears the authentication data and session for the Truvideo SDK.
     */
    fun clearAuthentication()

    /**
     * The environment for the Truvideo SDK.
     *
     * This property indicates the current environment in which the Truvideo SDK is running.
     * It can be one of the following values:
     * - `DEV`: Dev environment.
     * - `PROD`: Production environment.
     * - `BETA`: Beta testing environment.
     * - `RC`: Release Candidate environment.
     *
     * The environment setting can be used to control the behavior of the SDK based on the
     * specific environment configuration.
     *
     * @return A `String` representing the current environment (`PROD`, `BETA`, `RC` or `DEV`).
     */
    val environment: String

    /**
     * The TruvideoSDK Core version
     */
    val version: String
}