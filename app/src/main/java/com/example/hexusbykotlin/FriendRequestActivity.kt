package com.example.hexusbykotlin

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hexusbykotlin.Model.User
import com.example.hexusbykotlin.Utils.Common
import com.example.hexusbykotlin.Interface.IFirebaseLoadDone
import com.example.hexusbykotlin.Interface.IRecyclerItemClickListener
import com.example.hexusbykotlin.ViewHolder.FriendRequestViewHolder
import com.example.hexusbykotlin.ViewHolder.UserViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar

class FriendRequestActivity : AppCompatActivity(), IFirebaseLoadDone {

    var adapter: FirebaseRecyclerAdapter<User, FriendRequestViewHolder>? = null
    var searchAdapter: FirebaseRecyclerAdapter<User, FriendRequestViewHolder>? = null
    lateinit var iFirebaseLoadDone: IFirebaseLoadDone
    var suggestList:List<String> = ArrayList()
    private lateinit var material_search_bar : MaterialSearchBar
    private lateinit var recycler_all_people : RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request)
        material_search_bar = findViewById(R.id.material_search_bar)
        recycler_all_people = findViewById(R.id.recycler_friend_request)

        material_search_bar.setCardViewElevation(10)
        material_search_bar.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val suggest= ArrayList<String>()
                for (search in suggestList)
                    if(search.toLowerCase().contentEquals(material_search_bar.text.toLowerCase()))
                        suggest.add(search)
                material_search_bar.lastSuggestions = (suggest)
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        material_search_bar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener{
            override fun onSearchStateChanged(enabled: Boolean) {
                if(!enabled){
                    if(adapter != null){
                        recycler_all_people.adapter=adapter
                    }
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString())
            }

            override fun onButtonClicked(buttonCode: Int) {

            }

        })

        recycler_all_people.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler_all_people.layoutManager = layoutManager
        recycler_all_people.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))

        iFirebaseLoadDone = this


        loadFriendRequestList()
        loadSearchData()

    }

    private fun loadSearchData() {
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

        userList.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (userSnapShot in p0.children){
                    val user = userSnapShot.getValue(User::class.java)
                    lstUserEmail.add(user!!.email!!)
                }
                iFirebaseLoadDone.onFirebaseLoadUserDone(lstUserEmail)
            }

            override fun onCancelled(p0: DatabaseError) {
                iFirebaseLoadDone.onFirebaseLoadFailed(p0.message)
            }

        })
    }

    private fun loadFriendRequestList() {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        adapter = object:FirebaseRecyclerAdapter<User, FriendRequestViewHolder>(options){
            override fun onCreateViewHolder(
                p0: ViewGroup,
                p1: Int
            ): FriendRequestViewHolder {
                val itemView = LayoutInflater.from(p0.context)
                    .inflate(R.layout.layout_friend_request, p0,false)
                return FriendRequestViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.btn_decline.setOnClickListener{
                    deleteFriendRequest(model, true)

                }
                holder.btn_accept.setOnClickListener{
                    deleteFriendRequest(model, false)
                    addToAcceptList(model)
                    addUserToFriendContact(model)
                }
            }


        }
        adapter!!.startListening()
        recycler_all_people.adapter = adapter
    }
    private fun startSearch(search_string:String){
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)
            .orderByChild("email")
            .startAt(search_string)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        searchAdapter = object:FirebaseRecyclerAdapter<User, FriendRequestViewHolder>(options)
        {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int):  FriendRequestViewHolder {
                val itemView = LayoutInflater.from(p0.context)
                    .inflate(R.layout.layout_friend_request, p0,false)
                return  FriendRequestViewHolder (itemView)
            }

            override fun onBindViewHolder(holder:  FriendRequestViewHolder, position: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.btn_decline.setOnClickListener{
                    deleteFriendRequest(model, true)

                }
                holder.btn_accept.setOnClickListener{
                    deleteFriendRequest(model, false)
                    addToAcceptList(model)
                    addUserToFriendContact(model)
                }
            }

        }
        searchAdapter!!.startListening()
        recycler_all_people.adapter = searchAdapter
    }
    private fun addUserToFriendContact(model: User) {
        val acceptList = FirebaseDatabase.getInstance()
            .getReference(Common.USER_INFORMATION)
            .child(model.uid!!)
            .child(Common.ACCEPT_LIST)

        acceptList.child(Common.loggedUser!!.uid!!).setValue(Common.loggedUser)
    }

    private fun addToAcceptList(model: User) {
        val acceptList = FirebaseDatabase.getInstance()
            .getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

        acceptList.child(model.uid!!).setValue(model)
        
    }

    private fun deleteFriendRequest(model: User, isShowMessage: Boolean) {
        val friendRequest = FirebaseDatabase.getInstance()
            .getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

        friendRequest.child(model.uid!!).removeValue()
            .addOnSuccessListener {
                if(isShowMessage)
                    Toast.makeText(this@FriendRequestActivity,"Remove!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStop() {

        if(adapter != null)
            adapter!!.stopListening()
        if(searchAdapter != null)
            searchAdapter!!.stopListening()




        super.onStop()

    }

    override fun onFirebaseLoadUserDone(lstEmail: List<String>) {
        material_search_bar.lastSuggestions = lstEmail
    }

    override fun onFirebaseLoadFailed(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}