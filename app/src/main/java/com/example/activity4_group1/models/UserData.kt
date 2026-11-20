package com.example.activity4_group1.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class UserData(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
) : Parcelable