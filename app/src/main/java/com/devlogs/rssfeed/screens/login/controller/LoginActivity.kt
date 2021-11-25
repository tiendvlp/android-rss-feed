package com.devlogs.rssfeed.screens.login.controller

import android.R.attr
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.devlogs.rssfeed.R
import com.facebook.login.widget.LoginButton
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.facebook.*
import com.google.android.gms.common.SignInButton
import org.json.JSONException
import java.util.*
import androidx.core.app.ActivityCompat.startActivityForResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.R.attr.data
import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException


class LoginActivity : AppCompatActivity() {
    companion object {
        fun start (context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var callbackManager : CallbackManager
    private lateinit var loginButton : LoginButton
    private lateinit var signInButton : SignInButton
    private lateinit var  mGoogleSignInClient : GoogleSignInClient
    private val RC_SIGN_IN = 333

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login)
        FacebookSdk.sdkInitialize(applicationContext);
        AppEventsLogger.activateApp(application);
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        callbackManager = CallbackManager.Factory.create();
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener {v ->
            val signInIntent: Intent = mGoogleSignInClient.getSignInIntent()
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        loginButton = findViewById(R.id.login_button) as LoginButton
        loginButton.setReadPermissions(Arrays.asList("email", "user_birthday"))

        // Callback registration
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                var request = GraphRequest.newMeRequest(loginResult!!.accessToken) { o, r ->
                    try {
                        val email = o!!.getString("email")
                        Log.d("LoginFacebook", email)
                    } catch (e : JSONException) {
                        e.message?.let { Log.d("LoginFacebook", it) }
                    }
                }
                val param = Bundle();
                Log.d("AvatarUrl", "https://graph.facebook.com/" + loginResult!!.accessToken.userId + "/picture?return_ssl_resource=1")
                param.putString("fields", "email, name, id")
                request.parameters = param
                request.executeAsync()
            }

            override fun onCancel() {
                Log.d("LoginFacebook", "Cancel")
            }

            override fun onError(exception: FacebookException) {
                Log.d("LoginFacebook", "Error: ${exception.message}")
            }
        })

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

            } catch (e: ApiException) {
                Log.w("LoginGoogle", "signInResult:failed code=" + e.statusCode)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}