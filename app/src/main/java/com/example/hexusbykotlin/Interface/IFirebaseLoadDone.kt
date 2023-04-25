package com.example.hexusbykotlin.Interface

interface IFirebaseLoadDone {
    fun onFirebaseLoadUserDone(lstEmail:List<String>)
    fun onFirebaseLoadFailed(message:String)
}