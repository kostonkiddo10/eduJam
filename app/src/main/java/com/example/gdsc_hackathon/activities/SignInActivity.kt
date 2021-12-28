package com.example.gdsc_hackathon.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gdsc_hackathon.R

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.text.TextUtils
import android.util.Patterns
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import java.util.regex.Matcher
import java.util.regex.Pattern


class SignInActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var googleLoginButton: RelativeLayout
    private lateinit var emailLoginButton: Button
    private lateinit var registerButton: RelativeLayout
    private lateinit var passwordEditText: EditText
    private lateinit var emailEditText: EditText

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 120
    }

    private var TAG = "LOOK"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in_activity)

        button = findViewById(R.id.register)
        googleLoginButton = findViewById(R.id.google_sign_in_layout)
        emailLoginButton = findViewById(R.id.signin_with_email_button)
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)

        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        button.setOnClickListener {

            startActivity(Intent(applicationContext, SignUpActivity::class.java))

//            signInWithEmailAndPassword()
        }

        googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }

        emailLoginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(applicationContext, "Please enter all values!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            Firebase.firestore.collection("users").document(email).get()
                .addOnCompleteListener { task ->
                    val doc = task.result
                    if (doc != null && !doc.exists()) {
                        Toast.makeText(applicationContext, "User Does Not Exist. Please Signup", Toast.LENGTH_SHORT)
                            .show()
                        return@addOnCompleteListener
                    }
                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT)
                                    .show()
                                startActivity(Intent(applicationContext, MainActivity::class.java))
                                finish()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, "Please try again", Toast.LENGTH_SHORT)
                                .show()
                        }
                }

        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, SignInActivity.RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SignInActivity.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception

            if (task.isSuccessful) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e)
                }
            } else {
                Log.w(TAG, exception.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth.currentUser

                    if (user?.email!!.contains("tcet", true) || user.email!!.contains(
                            "thakur",
                            true
                        )
                    ) {

                        val usr = mAuth.currentUser

                        if (usr != null) {
                            Firebase.firestore.collection("users").document(user.email!!).get()
                                .addOnCompleteListener { t ->
                                    val doc = t.result
                                    if (doc != null && !doc.exists()) {
                                        Toast.makeText(
                                            applicationContext,
                                            "User Does Not Exist. Please Signup",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        mAuth.signOut()
//                                        googleSignInClient.signOut()
                                        return@addOnCompleteListener
                                    }
                                    else if(doc != null && doc.exists()){
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Please Use College Email",
                            Toast.LENGTH_LONG
                        ).show()

                        googleSignInClient.signOut()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
//                    updateUI(null)
                }
            }
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target!!).matches()
    }

    fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }

}