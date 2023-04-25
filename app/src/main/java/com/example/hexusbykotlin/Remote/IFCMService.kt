package com.example.hexusbykotlin.Remote

import io.reactivex.Observable
import com.example.hexusbykotlin.Model.MyResponse
import com.example.hexusbykotlin.Model.Request
import io.reactivex.Observer
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers("Content-Type:application/json",
    "Authorization:key=AAAAJaSHWKY:APA91bHLuAno5mc71u6jMk55hiWPqSbDQGWuKauCaUNBm8LsElucCV9lDF13zi3La7KnYlN9pBhmdEg7uUMiZyGq8RP2amzNMXBYeIWXwTIccNjA3aKDy9HLaRKrC-m-gpDdD6n0U0KT")
    @POST("fcm/send")
    fun sendFriendRequestToUser(@Body body: Request):Observable<MyResponse>

}