package com.loic.spe95.signin.data

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.loic.spe95.data.Result
import com.loic.spe95.data.await

class LoginRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * login user into firebase
     */
    suspend fun loginUser(email: String, password: String): Result<AuthResult> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .await()
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}