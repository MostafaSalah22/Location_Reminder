package com.udacity.project4.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

class AuthenticationActivity : AppCompatActivity() {

    private val SIGN_IN_RESULT_CODE = 100
    private val loginViewModel by viewModels<LoginViewModel>()

    private lateinit var binding: ActivityAuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginMainButton.setOnClickListener {
            intentToFirebaseUI()
        }

        loginViewModel.authenticationState.observe(this, Observer { state ->

            if(state == LoginViewModel.AuthenticationState.AUTHENTICATED) {
                val intent = Intent(this,RemindersActivity::class.java)
                startActivity(intent)
            }
            else
                //Snackbar.make(this,"Enter your email and password",Snackbar.LENGTH_LONG).show()
                Toast.makeText(this,"Enter your email and password",Toast.LENGTH_LONG).show()

        })


    }

    private fun intentToFirebaseUI() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).setLogo(R.drawable.map).build(), SIGN_IN_RESULT_CODE
        )
    }
}