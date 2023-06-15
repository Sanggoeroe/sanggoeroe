package com.capstone.sanggoroe.data

open class Result<out R> private constructor(){
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val error: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}