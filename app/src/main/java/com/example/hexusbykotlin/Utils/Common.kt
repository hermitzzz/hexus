package com.example.hexusbykotlin.Utils

import com.example.hexusbykotlin.Model.User
import com.example.hexusbykotlin.Remote.IFCMService
import com.example.hexusbykotlin.Remote.RetrofitClient

object Common {

     var loggedUser: User?=null
    var trackingUser: User?=null
    val ACCEPT_LIST: String ="acceptList"
    val USER_UID_SAVE_KEY: String = "SAVE_KEY"
    val TOKENS: String = "Tokens"
    val USER_INFORMATION: String = "UserInformation"
    val TO_EMAIL: String = "ToName"
    val TO_UID: String = "ToUid"
    val FROM_EMAIL: String = "FromName"
    val From_UID: String = "FromUid"
    val FRIEND_REQUEST: String = "FriendRequest"

    val fcmService:IFCMService
    get()=RetrofitClient.getClient("https://fcm.googleapis.com/")
        .create(IFCMService::class.java)
}