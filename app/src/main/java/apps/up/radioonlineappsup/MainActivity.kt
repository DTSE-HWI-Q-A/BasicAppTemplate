package apps.up.radioonlineappsup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.result.AuthAccount
import com.huawei.hms.support.account.service.AccountAuthService
import com.huawei.hms.support.api.entity.common.CommonConstant
import im.delight.android.webview.AdvancedWebView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logininicio()
        webset()
    }
    fun logininicio (){
        silentSignInByHwId()
    }

    fun webset(){
        val mWebView = findViewById<AdvancedWebView>(R.id.webid)
        mWebView.loadUrl("https://emisoras.com.mx/")
        mWebView.setMixedContentAllowed(true)
        mWebView.settings.allowFileAccess
        mWebView.setDesktopMode(false)
    }
    private var mAuthService: AccountAuthService? = null

    // Set HUAWEI ID sign-in authorization parameters.
    private var mAuthParam: AccountAuthParams? = null

    // Define the request code for signInIntent.
    private val REQUEST_CODE_SIGN_IN = 1000

    // Define the log tag.
    private val TAG = "Account"

    /**
     * Silent sign-in: If a user has authorized your app and signed in, no authorization or sign-in screen will appear during subsequent sign-ins, and the user will directly sign in.
     * After a successful silent sign-in, the HUAWEI ID information will be returned in the success event listener.
     * If the user has not authorized your app or signed in, the silent sign-in will fail. In this case, your app will show the authorization or sign-in screen to the user.
     */
    private fun silentSignInByHwId() {
        // 1. Use AccountAuthParams to specify the user information to be obtained after user authorization, including the user ID (OpenID and UnionID), email address, and profile (nickname and picture).
        // 2. By default, DEFAULT_AUTH_REQUEST_PARAM specifies two items to be obtained, that is, the user ID and profile.
        // 3. If your app needs to obtain the user's email address, call setEmail().
        // 4. To support ID token-based HUAWEI ID sign-in, use setIdToken(). User information can be parsed from the ID token.
        mAuthParam = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setEmail()
            .setIdToken()
            .createParams()

        // Use AccountAuthParams to build AccountAuthService.
        mAuthService = AccountAuthManager.getService(this, mAuthParam)

        // Sign in with a HUAWEI ID silently.
        val task: Task<AuthAccount> = mAuthService!!.silentSignIn()
        task.addOnSuccessListener { authAccount -> // The silent sign-in is successful. Process the returned AuthAccount object to obtain the HUAWEI ID information.
            dealWithResultOfSignIn(authAccount)
        }
        task.addOnFailureListener { e ->
            // The silent sign-in fails. Your app will call getSignInIntent() to show the authorization or sign-in screen.
            if (e is ApiException) {
                val apiException: ApiException? = e as ApiException?
                val signInIntent: Intent = mAuthService!!.getSignInIntent()
                // If your app appears in full screen mode when a user tries to sign in, that is, with no status bar at the top of the device screen, add the following parameter in the intent:
                // intent.putExtra(CommonConstant.RequestParams.IS_FULL_SCREEN, true)
                // Check the details in this FAQ.
                signInIntent.putExtra(CommonConstant.RequestParams.IS_FULL_SCREEN, true)
                startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
            }
        }
    }

    /**
     * Process the returned AuthAccount object to obtain the HUAWEI ID information.
     *
     * @param authAccount AuthAccount object, which contains the HUAWEI ID information.
     */
    private fun dealWithResultOfSignIn(authAccount: AuthAccount) {
        Log.i(TAG, "idToken:" + authAccount.getIdToken())
        // TODO: After obtaining the ID token, your app will send it to your app server if there is one. If you have no app server, your app will verify and parse the ID token locally.

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Log.i(TAG, "onActivitResult of sigInInIntent, request code: $REQUEST_CODE_SIGN_IN")
            val authAccountTask: Task<AuthAccount> = AccountAuthManager.parseAuthResultFromIntent(data)
            if (authAccountTask.isSuccessful()) {
                // The sign-in is successful, and the authAccount object that contains the HUAWEI ID information is obtained.
                val authAccount: AuthAccount = authAccountTask.getResult()
                dealWithResultOfSignIn(authAccount)
                Log.i(TAG, "onActivitResult of sigInInIntent, request code: $REQUEST_CODE_SIGN_IN")
            } else {
                // The sign-in failed. Find the failure cause from the status code. For more information, please refer to Error Codes.
                Log.e(TAG, "sign in failed : " + (authAccountTask.getException() as ApiException).getStatusCode())
            }
        }
    }
}