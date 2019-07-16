package web

import com.google.api.client.auth.oauth2.TokenResponseException
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import common.ServerTest
import org.junit.jupiter.api.Test
import java.io.FileReader
import java.io.IOException


open class AuthCodeTest: ServerTest() {

    val AUTH_CODE_FOR_TEST = "xxx"
    val REFRESH_TOKEN = "xx"

    /**
     *  認証コードからトークン・リフレッシュトークンを取得。
     *  注意 : AndroidアプリケーションからAUTH_CODE_FOR_TESTの再取得が必要
     */
    /*
    @Test
    fun testRetrieveTokens() {
        fun GoogleTokenResponse.printTokens() =
                "access token : $accessToken \n" +
                "refresh token : $refreshToken \n" +
                "expiresInSeconds : $expiresInSeconds \n" +
                "idToken : $idToken \n" +
                "parseIdToken : ${parseIdToken()}\n" +
                "scope : $scope \n" +
                "tokenType : $tokenType" +
                "prettyString : ${toPrettyString()}"
        val res = authCodeToToken(AUTH_CODE_FOR_TEST)
        assertThat(res).isNotEmpty
        print("success to retrieve the tokens. ${res.printTokens()}")
    }
    */


    val CLIENT_SECRET_FILE = "/Users/yy/Documents/github/kotlin-ktor-exposed-starter/src/main/resources/client_secret.json" // Be careful not to share this!
    val mClientSecrets = GoogleClientSecrets.load(
            JacksonFactory.getDefaultInstance(),
            FileReader(CLIENT_SECRET_FILE)
    )

    fun authCodeToToken(authCode:String) : GoogleTokenResponse{

        val REDIRECT_URI = "" // Can be empty if you don’t use web redirects

        val request = GoogleAuthorizationCodeTokenRequest(
                NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                "https://oauth2.googleapis.com/token",
                mClientSecrets.details.clientId,
                mClientSecrets.details.clientSecret,
                authCode,
                REDIRECT_URI)

        return request.execute()

        // You can also get an ID token from the exchange result if basic profile scopes are requested
        // e.g. starting GoogleSignInOptions.Builder from GoogleSignInOptions.DEFAULT_SIGN_IN like the
        // sample code as used here: http://goo.gl/0Unpq8
        //
        // GoogleIdToken googleIdToken = tokenResponse.parseIdToken();

    }

    /**
     * リフレッシュトークンからトークンを取得
     */
    @Test
    fun testRefreshTokenToToken(){
        refreshAccessToken(REFRESH_TOKEN)
    }

    @Throws(IOException::class)
    fun refreshAccessToken(refreshToken:String) : GoogleTokenResponse {
        try {
            val response = GoogleRefreshTokenRequest(
                    NetHttpTransport(),
                    JacksonFactory(),
                    refreshToken,
                    mClientSecrets.details.clientId,
                    mClientSecrets.details.clientSecret
            ).execute()
            println("Access token: " + response.accessToken)
            return response
        } catch (e: TokenResponseException) {
            if (e.details != null) {
                System.err.println("Error: " + e.details.error)
                if (e.details.errorDescription != null) {
                    System.err.println(e.details.errorDescription)
                }
                if (e.details.errorUri != null) {
                    System.err.println(e.details.errorUri)
                }
            } else {
                System.err.println(e.message)
            }
        }
        throw Error()
    }

}