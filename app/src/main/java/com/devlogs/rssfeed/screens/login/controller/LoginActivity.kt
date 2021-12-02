package com.devlogs.rssfeed.screens.login.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.devlogs.rssfeed.R
import com.facebook.login.widget.LoginButton
import android.content.Intent
import android.util.Log
import com.facebook.*
import com.google.android.gms.common.SignInButton
import org.json.JSONException
import java.util.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.content.Context
import android.widget.Toast
import com.devlogs.rssfeed.application.ApplicationStateManager
import com.devlogs.rssfeed.authentication.SSOLoginUseCaseSync
import com.devlogs.rssfeed.common.helper.InternetChecker
import com.devlogs.rssfeed.common.helper.InternetChecker.isOnline
import com.devlogs.rssfeed.rss_channels.GetUserRssChannelsUseCaseSync
import com.devlogs.rssfeed.screens.main.MainActivity
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), LoginController.Listener{
    companion object {
        fun start (context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject
    protected lateinit var loginController: LoginController
    @Inject
    protected lateinit var applicationStateManager : ApplicationStateManager
    @Inject
    protected lateinit var getUserRssChannelUseCaseSync: GetUserRssChannelsUseCaseSync
    private lateinit var callbackManager : CallbackManager
    private lateinit var loginButton : LoginButton
    private lateinit var signInButton : SignInButton
    private lateinit var  mGoogleSignInClient : GoogleSignInClient
    private val RC_SIGN_IN = 333

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login)
        setupFacebookLogin()
        setupGoogleLogin ()

    }

    private fun setupFacebookLogin () {
        FacebookSdk.sdkInitialize(applicationContext);
        AppEventsLogger.activateApp(application);
        callbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button)
        loginButton.setReadPermissions(Arrays.asList("email", "user_birthday"))
        loginButton.registerCallback(callbackManager, facebookCallbackHandler())
    }

    private fun setupGoogleLogin () {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        signInButton = findViewById(R.id.sign_in_button);
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton.setOnClickListener {v ->
            if (!isOnline()) {
                Toast.makeText(this,"No internet connection", Toast.LENGTH_SHORT).show()
            } else {
                val signInIntent: Intent = mGoogleSignInClient.getSignInIntent()
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }


    private fun facebookCallbackHandler () = object: FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                if (!isOnline()) {
                    Toast.makeText(this@LoginActivity,"No internet connection", Toast.LENGTH_SHORT).show()
                } else {
                    var request = GraphRequest.newMeRequest(loginResult!!.accessToken) { o, r ->
                        try {
                            val email = o!!.getString("email")
                            val avatarUrl =
                                "https://graph.facebook.com/${loginResult!!.accessToken.userId}/picture?return_ssl_resource=1"
                            val name = o!!.getString("name")
                            login(email, name, avatarUrl)
                            Log.d("LoginFacebook", email)
                            Log.d("LoginFacebook", name)
                            Log.d("LoginFacebook", avatarUrl)
                        } catch (e: JSONException) {
                            e.message?.let { Log.d("LoginFacebook", it) }
                        }
                    }
                    val param = Bundle();
                    Log.d(
                        "AvatarUrl",
                        "https://graph.facebook.com/" + loginResult!!.accessToken.userId + "/picture?return_ssl_resource=1"
                    )
                    param.putString("fields", "email, name, id")
                    request.parameters = param
                    request.executeAsync()
                }
                LoginManager.getInstance().logOut()
            }

            override fun onCancel() {
                Log.d("LoginFacebook", "Cancel")
            }

            override fun onError(exception: FacebookException) {
                Log.d("LoginFacebook", "Error: ${exception.message}")
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode === RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                Log.d("LoginGoogle", account.email)
                Log.d("LoginGoogle", account.displayName)
                Log.d("LoginGoogle", account.photoUrl.toString())
                login(account.email, account.displayName, account.photoUrl.toString())
                mGoogleSignInClient.signInIntent
            } catch (e: ApiException) {
                Log.w("LoginGoogle", "signInResult:failed code=" + e.statusCode)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun login (email: String, name: String, avatarUrl: String) {
        loginController.register(this)
        loginController.login(email,name, avatarUrl)
    }

    override fun loginSuccess() {
        Toast.makeText(this, "Login success", Toast.LENGTH_LONG).show()
        CoroutineScope(Dispatchers.Main.immediate).launch {
            val result = getUserRssChannelUseCaseSync.executes()
            if (result is GetUserRssChannelsUseCaseSync.Result.Success) {
                if (result.channels.isNotEmpty()) {
                    applicationStateManager.selectedChannelId = result.channels.elementAt(0).id
                }
            }
            MainActivity.start(this@LoginActivity)
            finish()
        }
    }

    override fun loginFailed(errorMessage: String) {
        Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show()
    }
}