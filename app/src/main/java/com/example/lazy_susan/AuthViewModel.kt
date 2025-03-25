package com.example.lazy_susan

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    private fun mapFirebaseError(message: String?): String {
        return when {
            message?.contains("The supplied auth credential is incorrect") == true ->
                "Invalid email or password. Please try again."

            message?.contains("There is no user record") == true ->
                "No account found with this email."

            message?.contains("The email address is badly formatted") == true ->
                "Please enter a valid email address."

            else -> message ?: "Something went wrong. Please try again."
        }
    }

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or Password cannot be empty")
            return
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                userId?.let { fetchUserData(it) }
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(mapFirebaseError(task.exception?.message))

            }
        }
    }

    fun signup(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or Password cannot be empty")
            return
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    saveUserToDatabase(userId, email)
                }
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(mapFirebaseError(task.exception?.message))

            }
        }
    }

    private fun saveUserToDatabase(userId: String, email: String) {
        val user = User(userId, email)
        database.child(userId).setValue(user).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                _authState.value = AuthState.Error(task.exception?.message ?: "Failed to save user data")
            }
        }
    }

    private fun fetchUserData(userId: String) {
        database.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(User::class.java)
                // Handle the retrieved user data if needed
            } else {
                _authState.value = AuthState.Error("User data not found")
            }
        }.addOnFailureListener { exception ->
            _authState.value = AuthState.Error(exception.message ?: "Failed to fetch user data")
        }
    }

    fun changePassword(email: String) {
        if (email.isEmpty()) return
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return

        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = AuthState.Authenticated
                signout()
            } else {
                _authState.value = AuthState.Error(mapFirebaseError(task.exception?.message))

            }
        }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

data class User(
    val userId: String = "",
    val email: String = ""
)

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}