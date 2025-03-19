package com.example.lazy_susan

import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init{
        checkAuthStatus()
    }

    fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.value = AuthState.Unauthenticated
        }
        else{
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email : String, password : String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or Password cannot be empty")
            return
        }
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{task ->
            if (task.isSuccessful){
                _authState.value = AuthState.Authenticated
            }
            else{
                _authState.value = AuthState.Error(task.exception?.message?:"Something Went Wrong")
            }
        }


    }
    fun signup(email : String, password : String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or Password cannot be empty")
            return
        }
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task ->
            if (task.isSuccessful){
                _authState.value = AuthState.Authenticated
            }
            else{
                _authState.value = AuthState.Error(task.exception?.message?:"Something Went Wrong")
            }
        }




}
    fun changePassword(email : String){
        if (email.isEmpty()){
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.toString()).matches()){
            return
        }
        auth.sendPasswordResetEmail(email.toString()).addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                _authState.value = AuthState.Authenticated
                signout()
            }
            else{
                _authState.value = AuthState.Error(task.exception?.message?:"Something Went Wrong")
            }

        }
    }

    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

}
sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}