package com.example.hexusbykotlin

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.hexusbykotlin.Model.User
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import io.paperdb.Paper
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object{
        private val MY_REQUEST_CODE=7117
    }
    lateinit var user_information:DatabaseReference
    lateinit var providers:List<AuthUI.IdpConfig>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Paper.init(this)

        user_information=FirebaseDatabase.getInstance().getReference(com.example.hexusbykotlin.Utils.Common.USER_INFORMATION)

        providers = Arrays.asList<AuthUI.IdpConfig>(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        Dexter.withActivity(this)
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION )
            .withListener(object: PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    showSignInOptions()

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@MainActivity,"You must accept this permission", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {

                }

            }).check()

    }

    private fun showSignInOptions() {
        startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(providers)
            .build(), MY_REQUEST_CODE)

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE)
        {
            val firebaseUser = FirebaseAuth.getInstance().currentUser


                    user_information.orderByKey()
                    .equalTo(firebaseUser!!.uid)
                    .addListenerForSingleValueEvent(object:ValueEventListener
                    {
                        override fun onDataChange(dataSnapshot: DataSnapshot)
                        {

                            if (dataSnapshot.value==null)
                            {
                                if(!dataSnapshot.child(firebaseUser!!.uid).exists())
                                {
                                    com.example.hexusbykotlin.Utils.Common.loggedUser = User(firebaseUser.uid,
                                        firebaseUser.email!!)

                                    user_information.child(com.example.hexusbykotlin.Utils.Common.loggedUser!!.uid!!)
                                        .setValue(com.example.hexusbykotlin.Utils.Common.loggedUser)

                                }
                            } else
                            {
                                com.example.hexusbykotlin.Utils.Common.loggedUser=dataSnapshot.child(firebaseUser.uid)
                                    .getValue(User::class.java)!!

                            }

                            Paper.book().write(com.example.hexusbykotlin.Utils.Common.USER_UID_SAVE_KEY, com.example.hexusbykotlin.Utils.Common.loggedUser!!.uid!!)
                            updateToken(firebaseUser)
                            setupUI()
                        }

                        override fun onCancelled(error: DatabaseError)
                        {

                        }

                    } )
            }

        setupUI()
    }

    private fun setupUI() {
        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
        finish()
    }

    private fun updateToken(firebaseUser: FirebaseUser?) {
        val tokens = FirebaseDatabase.getInstance()
            .getReference(com.example.hexusbykotlin.Utils.Common.TOKENS);

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { instanceIdResult ->
                tokens.child(firebaseUser!!.uid)
                    .setValue(instanceIdResult)
                 }.addOnFailureListener{e -> Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()}
    }

}
