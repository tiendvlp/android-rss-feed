package com.devlogs.rssfeed.screens.login.controller

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
import org.json.JSONException
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var callbackManager : CallbackManager
    private lateinit var loginButton : LoginButton
    private lateinit var imgAvatar : ImageView
    private lateinit var txtName : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login)
        FacebookSdk.sdkInitialize(applicationContext);
        AppEventsLogger.activateApp(application);

        callbackManager = CallbackManager.Factory.create();

        imgAvatar = findViewById(R.id.imgAvatar)
        txtName = findViewById(R.id.txtName)
        loginButton = findViewById<LoginButton>(R.id.login_button) as LoginButton
        loginButton.setReadPermissions(Arrays.asList("email", "user_birthday"))

        // Callback registration
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                var request = GraphRequest.newMeRequest(loginResult!!.accessToken) { o, r ->
                    try {
                        val email = o!!.getString("email")
                        txtName.text = o!!.getString("name")
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
                Glide
                    .with(imgAvatar.context)
                    .load("https://graph.facebook.com/" + loginResult!!.accessToken.userId + "/picture?return_ssl_resource=1")
                    .centerCrop()
                    .into(imgAvatar);
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
        super.onActivityResult(requestCode, resultCode, data)
    }
}