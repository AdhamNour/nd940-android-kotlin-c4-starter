package com.udacity.project4.authentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.firebase.ui.auth.AuthUI
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.IdpResponse
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    val SIGN_IN_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
//         tTODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          tTODO: If the user was authenticated, send him to RemindersActivity
        val sharedPref =getSharedPreferences(getString(R.string.logedin), Context.MODE_PRIVATE)
        val isLogedIn = sharedPref.getBoolean(getString(R.string.isLogedIn),false)
        if(isLogedIn){
            val intent = Intent(this, RemindersActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
//          tTODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
        findViewById<Button>(R.id.authenticate_button).setOnClickListener {
            startAuthenticationWorkFlow()
        }
    }

    private fun startAuthenticationWorkFlow() {



        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            )        .setIsSmartLockEnabled(false)
                .build(), SIGN_IN_CODE
        )
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val intent = Intent(this, RemindersActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                val sharedPref =getSharedPreferences(getString(R.string.logedin), Context.MODE_PRIVATE)
                val x  = sharedPref.edit()
                x.putBoolean(getString(R.string.isLogedIn),true)
                x.apply()
            } else {
                Log.i("Auth Failed", "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }
}
