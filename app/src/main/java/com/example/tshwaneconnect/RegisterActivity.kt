
package com.example.tshwaneconnect

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

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var cbShowPassword: CheckBox
    private lateinit var btnRegister: Button
    private lateinit var tvSignIn: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        // INITIALISE FIREBASE
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // CONNECT XML COMPONENTS
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword =
            findViewById(R.id.etConfirmPassword)

        cbShowPassword = findViewById(R.id.cbShowPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvSignIn = findViewById(R.id.tvSignIn)

        // SHOW OR HIDE PASSWORDS
        cbShowPassword.setOnCheckedChangeListener { _, isChecked ->

            val transformation = if (isChecked) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }

            etPassword.transformationMethod = transformation
            etConfirmPassword.transformationMethod = transformation

            etPassword.setSelection(etPassword.text.length)
            etConfirmPassword.setSelection(
                etConfirmPassword.text.length
            )
        }

        // REGISTER BUTTON
        btnRegister.setOnClickListener {
            validateRegistration()
        }

        // RETURN TO LOGINS
        tvSignIn.setOnClickListener {
            auth.signOut()
            finish()
        }
    }

    private fun validateRegistration() {

        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword =
            etConfirmPassword.text.toString()

        // VALIDATE FULL NAME
        if (fullName.isEmpty()) {
            etFullName.error = "Please enter your full name"
            etFullName.requestFocus()
            return
        }

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
            etPassword.error = "Please create a password"
            etPassword.requestFocus()
            return
        }

        if (password.length < 8) {
            etPassword.error =
                "Password must contain at least 8 characters"
            etPassword.requestFocus()
            return
        }

        // CONFIRM PASSWORD
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error =
                "Please confirm your password"
            etConfirmPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return
        }

        registerWithFirebase(fullName, email, password)
    }

    private fun registerWithFirebase(
        fullName: String,
        email: String,
        password: String
    ) {

        btnRegister.isEnabled = false
        btnRegister.text = "Creating account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (!task.isSuccessful) {
                    resetRegisterButton()
                    showMessage(
                        "Registration failed: ${
                            task.exception?.localizedMessage
                                ?: "Please try again."
                        }"
                    )
                    return@addOnCompleteListener
                }

                val user = auth.currentUser

                if (user == null) {
                    resetRegisterButton()
                    showMessage("Unable to create your account.")
                    return@addOnCompleteListener
                }

                val staffProfile = hashMapOf(
                    "fullName" to fullName,
                    "email" to email,
                    "role" to "STAFF",
                    "status" to "PENDING"
                )

                firestore.collection("users")
                    .document(user.uid)
                    .set(staffProfile)
                    .addOnSuccessListener {

                        auth.signOut()
                        resetRegisterButton()

                        showMessage(
                            "Registration submitted! Your account is awaiting administrator approval."
                        )

                        finish()
                    }
                    .addOnFailureListener { exception ->

                        auth.signOut()
                        resetRegisterButton()

                        showMessage(
                            "Authentication account created, but the staff profile could not be saved. Contact the administrator before retrying. ${exception.localizedMessage ?: ""}"
                        )
                    }
            }
    }

    private fun resetRegisterButton() {
        btnRegister.isEnabled = true
        btnRegister.text = "Register"
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
