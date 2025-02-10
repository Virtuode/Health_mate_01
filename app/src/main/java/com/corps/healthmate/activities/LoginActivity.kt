package com.corps.healthmate.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.corps.healthmate.R
import com.corps.healthmate.fragment.ForgotPasswordBottomSheet
import com.corps.healthmate.navigation.NavigationManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text
import timber.log.Timber
import java.util.regex.Pattern



class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var loginContainer: View

    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var buttonLogin: Button? = null
    private var forgotPassTv: TextView? = null
    private var redirectToSignUp: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupStatusBar()

        auth = FirebaseAuth.getInstance()

        initViews()

        buttonLogin?.setOnClickListener {
            animateButton()
            if (validateEmail() && validatePassword()){
                onLoginButtonClick()
            }
        }

        forgotPassTv?.setOnClickListener {
            showForgotPasswordBottomSheet()
        }

        redirectToSignUp?.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)

        }
    }

    private fun animateButton() {
        buttonLogin?.let { button ->
            button.isEnabled = false

            val animatorSet = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(button, View.SCALE_X, 1f, 0.9f, 1f),
                    ObjectAnimator.ofFloat(button, View.SCALE_Y, 1f, 0.9f, 1f)
                )
                duration = 300
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        button.isEnabled = true
                    }
                })
            }
            animatorSet.start()
        }
    }

    private fun validateEmail(): Boolean {
        val email = emailEditText!!.text.toString().trim { it <= ' ' }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText!!.error = "Invalid email address"
            return false
        }
        return true
    }

    private fun validatePassword(): Boolean {
        val password = passwordEditText!!.text.toString().trim { it <= ' ' }

        if (password.isEmpty() ) {
            passwordEditText!!.error = "Please enter correct password"
            return false
        }

        return true
    }





    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun onLoginButtonClick() {
        val email = emailEditText?.text.toString().trim()
        val password = passwordEditText?.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please enter email and password.")
            return
        }

        if (!isValidEmail(email)) {
            showToast("Please enter a valid email address.")
            return
        }

        showLoading()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Reload user to get latest email verification status
                        user.reload().addOnCompleteListener {
                            if (user.isEmailVerified) {
                                // Update email verification status in database
                                FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(user.uid)
                                    .child("emailVerified")
                                    .setValue(true)
                                    .addOnSuccessListener {
                                        handleLoginSuccess()
                                    }
                                    .addOnFailureListener {
                                        // Even if database update fails, proceed with login
                                        handleLoginSuccess()
                                    }
                            } else {
                                hideLoading()
                                showError(getString(R.string.email_not_verified))
                                // Show verification dialog
                                showVerificationDialog(user)
                            }
                        }
                    } else {
                        hideLoading()
                        handleLoginError(Exception("User not found"))
                    }
                } else {
                    hideLoading()
                    handleLoginError(task.exception!!)
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        val isValid = Pattern.compile(emailRegex).matcher(email).matches()
        Timber.tag("LoginActivity").d("Email after trim:  " + email + ", isValid: " + isValid)
        return isValid
    }

    private fun handleLoginSuccess() {
        hideLoading()
        NavigationManager.handleLoginSuccess(this)
    }

    private fun handleLoginError(exception: Exception) {
        hideLoading()
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidUserException -> getString(R.string.error_user_not_found)
            is FirebaseAuthInvalidCredentialsException -> getString(R.string.error_invalid_credentials)
            else -> getString(R.string.error_login_failed)
        }
        showError(errorMessage)
    }

    private fun setupStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        val decorView = window.decorView
        val insetsController = WindowInsetsControllerCompat(window, decorView)
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true
    }

    private fun initViews() {

        emailEditText = findViewById(R.id.login_email_EditText)
        Timber.tag("LoginActivity").d("emailEditText after init: %s", emailEditText)
        passwordEditText = findViewById(R.id.login_password_EditText)
        buttonLogin = findViewById(R.id.button_Login)
        forgotPassTv = findViewById(R.id.forgotPasswordTextView)
        loadingProgressBar = findViewById(R.id.loading_progress_bar)
        loginContainer = findViewById(R.id.login_container)
        redirectToSignUp = findViewById(R.id.signUpTextView_redirect)
    }

    private fun showForgotPasswordBottomSheet() {
        val bottomSheet = ForgotPasswordBottomSheet()
        bottomSheet.show(supportFragmentManager, "ForgotPasswordBottomSheet")
    }

    private fun showLoading() {
        loadingProgressBar.visibility = View.VISIBLE
        loginContainer.alpha = 0.5f
        loginContainer.isEnabled = false
    }

    private fun hideLoading() {
        loadingProgressBar.visibility = View.GONE
        loginContainer.alpha = 1.0f
        loginContainer.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showVerificationDialog(user: FirebaseUser) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Email Not Verified")
            .setMessage("Please verify your email to continue. Would you like us to send a new verification email?")
            .setPositiveButton("Send Email") { _, _ ->
                user.sendEmailVerification()
                    .addOnSuccessListener {
                        showToast("Verification email sent. Please check your inbox.")
                    }
                    .addOnFailureListener {
                        showToast("Failed to send verification email. Please try again later.")
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
}
