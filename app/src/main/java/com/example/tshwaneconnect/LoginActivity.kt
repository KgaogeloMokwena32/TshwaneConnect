
package com.example.tshwaneconnect

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbShowPassword: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvSignUp: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        // INITIALISE FIREBASE
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // CONNECT XML COMPONENTS
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        cbShowPassword = findViewById(R.id.cbShowPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvSignUp = findViewById(R.id.tvSignUp)

        // SHOW OR HIDE PASSWORD
        cbShowPassword.setOnCheckedChangeListener { _, isChecked ->

            etPassword.transformationMethod =
                if (isChecked) {
                    HideReturnsTransformationMethod.getInstance()
                } else {
                    PasswordTransformationMethod.getInstance()
                }

            etPassword.setSelection(etPassword.text.length)
        }

        // LOGIN BUTTON
        btnLogin.setOnClickListener {
            validateLogin()
        }

        // REGISTER BUTTON
        tvSignUp.setOnClickListener {
            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }

        // FORGOT PASSWORD
        tvForgotPassword.setOnClickListener {
            sendPasswordReset()
        }
    }

    private fun validateLogin() {

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        // VALIDATE EMAIL
        if (email.isEmpty()) {
            etEmail.error = "Please enter your email address"
            etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email address"
            etEmail.requestFocus()
            return
        }

        // VALIDATE PASSWORD
        if (password.isEmpty()) {
            etPassword.error = "Please enter your password"
            etPassword.requestFocus()
            return
        }

        // AUTHENTICATE WITH FIREBASE
        loginWithFirebase(email, password)
    }

    private fun loginWithFirebase(
        email: String,
        password: String
    ) {

        btnLogin.isEnabled = false
        btnLogin.text = "Signing in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (!task.isSuccessful) {

                    resetLoginButton()

                    showMessage(
                        "Sign-in failed. Check your email and password."
                    )

                    return@addOnCompleteListener
                }

                val user = auth.currentUser

                if (user == null) {

                    auth.signOut()
                    resetLoginButton()

                    showMessage(
                        "Unable to verify your account."
                    )

                    return@addOnCompleteListener
                }

                // CHECK ROLE AND APPROVAL STATUS
                checkAccountAccess(user.uid)
            }
    }

    private fun checkAccountAccess(uid: String) {

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                resetLoginButton()

                if (!document.exists()) {

                    auth.signOut()

                    showMessage(
                        "Your staff profile was not found. Contact the administrator."
                    )

                    return@addOnSuccessListener
                }

                val role = document.getString("role")
                val status = document.getString("status")

                when {

                    // ADMINISTRATOR
                    role == "ADMIN" && status == "ACTIVE" -> {

                        showMessage(
                            "Welcome, Administrator!"
                        )

                        openAdminDashboard()
                    }

                    // APPROVED STAFF
                    role == "STAFF" && status == "ACTIVE" -> {

                        showMessage(
                            "Welcome to TLF Connect!"
                        )

                        openDashboard()
                    }

                    // PENDING STAFF
                    status == "PENDING" -> {

                        auth.signOut()

                        showMessage(
                            "Your account is awaiting administrator approval."
                        )
                    }

                    // DECLINED STAFF
                    status == "DECLINED" -> {

                        auth.signOut()

                        showMessage(
                            "Your registration was declined. Contact the administrator."
                        )
                    }

                    // UNAUTHORISED ACCOUNT
                    else -> {

                        auth.signOut()

                        showMessage(
                            "Your account is not authorised to access TLF Connect."
                        )
                    }
                }
            }
            .addOnFailureListener {

                auth.signOut()
                resetLoginButton()

                showMessage(
                    "Unable to verify account permissions. Check your internet connection."
                )
            }
    }

    // OPEN ADMINISTRATOR DASHBOARD
    private fun openAdminDashboard() {

        val intent = Intent(
            this,
            AdminDashboardActivity::class.java
        ).apply {

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    // OPEN STAFF DASHBOARD
    private fun openDashboard() {

        val intent = Intent(
            this,
            DashboardActivity::class.java
        ).apply {

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    // RESET PASSWORD THROUGH FIREBASE
    private fun sendPasswordReset() {

        val email = etEmail.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            etEmail.error =
                "Please enter your email address first"

            etEmail.requestFocus()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {

                showMessage(
                    "If an account exists for this email, check its inbox for reset instructions."
                )
            }
            .addOnFailureListener {

                showMessage(
                    "Unable to process password reset. Try again later."
                )
            }
    }

    // RESTORE LOGIN BUTTON
    private fun resetLoginButton() {

        btnLogin.isEnabled = true
        btnLogin.text = "Sign In"
    }

    // DISPLAY MESSAGE
    private fun showMessage(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
