package com.example.travelbuddy.repository

import com.example.travelbuddy.data.model.ResponseModel
import com.example.travelbuddy.data.model.UserModel
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    var user: FirebaseUser?
    fun login(email: String, password: String): Flow<ResponseModel.ResponseWithData<AuthResult>>
    fun signup(name: String, email: String, password: String): Flow<ResponseModel.ResponseWithData<AuthResult>>

    fun sendPasswordResetEmail(email: String): Flow<ResponseModel.ResponseWithData<Boolean>>
    suspend fun getUserInfo(uid: String): ResponseModel.ResponseWithData<UserModel.User>
    fun signOut()
    fun getUserId(): String?
   suspend fun getTripsList(): MutableList<String>?
    suspend fun getUserName(): String?
}