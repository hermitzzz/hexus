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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hexusbykotlin.Interface.IRecyclerItemClickListener
import com.example.hexusbykotlin.Model.MyResponse
import com.example.hexusbykotlin.Model.Request
import com.example.hexusbykotlin.Model.User
import com.example.hexusbykotlin.Utils.Common
import com.example.hexusbykotlin.Interface.IFirebaseLoadDone
import com.example.hexusbykotlin.ViewHolder.UserViewHolder
import com.example.hexusbykotlin.databinding.ActivityAllPeopleBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.Observable

class AllPeopleActivity : AppCompatActivity(), IFirebaseLoadDone {
    private lateinit var binding: ActivityAllPeopleBinding
    var adapter:FirebaseRecyclerAdapter<User, UserViewHolder>? = null
    var searchAdapter:FirebaseRecyclerAdapter<User, UserViewHolder>? = null

    lateinit var iFirebaseLoadDone: IFirebaseLoadDone
    var suggestList:List<String> = ArrayList()
    val compositeDisposable = CompositeDisposable()


   private lateinit var material_search_bar : MaterialSearchBar
   private lateinit var recycler_all_people : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_people)




        material_search_bar = findViewById(R.id.material_search_bar)
        recycler_all_people = findViewById(R.id.recycler_all_people)

        material_search_bar.setCardViewElevation(10)
        material_search_bar.addTextChangeListener(object : TextWatcher{
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

        loadUserList()
        loadSearchData()



        




    }

    private fun startSearch(search_string:String){
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .orderByChild("email")
            .startAt(search_string)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()


        searchAdapter = object:FirebaseRecyclerAdapter<User, UserViewHolder>(options)
        {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): UserViewHolder {
                val itemView = LayoutInflater.from(p0.context)
                    .inflate(R.layout.layout_user, p0,false)
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                if(model.email.equals(Common.loggedUser!!.email)){
                    holder.txt_user_email.text = java.lang.StringBuilder(model.email!!).append(" (me)")
                    holder.txt_user_email.setTypeface(holder.txt_user_email.typeface, Typeface.ITALIC)
                }
                else {
                    holder.txt_user_email.text = java.lang.StringBuilder(model.email!!)
                }

                holder.setClick(object:IRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        showDialogRequest(model)
                    }

                })
            }

        }
        searchAdapter!!.startListening()
        recycler_all_people.adapter = searchAdapter
    }

    private fun showDialogRequest(model: User) {
        val alertDialog = AlertDialog.Builder(this, R.style.MyRequestDialog)
        alertDialog.setTitle("Request Friend")
        alertDialog.setMessage("Do you want to send request friend to "+model.email)
        alertDialog.setIcon(R.drawable.baseline_person_add_24)

        alertDialog.setNegativeButton("Cancel",{dialogInterface, _-> dialogInterface.dismiss() })

        alertDialog.setPositiveButton("Send"){_, _->
            val acceptList = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser!!.uid!!)
                .child(Common.ACCEPT_LIST)

            acceptList.orderByKey().equalTo(model.uid)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.value == null)
                            sendFriendRequest(model)
                        else
                            Toast.makeText(this@AllPeopleActivity, "You and "+model.email+" already are friend", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }

                })
        }

        alertDialog.show()
    }

    private fun sendFriendRequest(model: User)  {
        val tokens =FirebaseDatabase.getInstance().getReference(Common.TOKENS)
        tokens.orderByKey().equalTo(model.uid)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.value == null)
                        Toast.makeText(this@AllPeopleActivity, "Token error", Toast.LENGTH_SHORT).show()
                    else {
                        val request = Request()
                        val dataSend = HashMap<String,String>()
                        dataSend[Common.From_UID] = Common.loggedUser!!.uid!!
                        dataSend[Common.FROM_EMAIL] = Common.loggedUser!!.email!!
                        dataSend[Common.TO_UID] = model.uid!!
                        dataSend[Common.TO_EMAIL] = model.email!!

                        request.to = p0.child(model.uid!!).getValue(String::class.java)!!
                        request.data = dataSend

                         compositeDisposable.add(Common.fcmService.sendFriendRequestToUser( request)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ t: MyResponse? ->
                                if (t!!.success == 1)
                                    Toast.makeText(this@AllPeopleActivity, "Sequest sent", Toast.LENGTH_SHORT).show()
                            }, { t: Throwable? ->
                                Toast.makeText(this@AllPeopleActivity, t!!.message, Toast.LENGTH_SHORT).show()
                            })
                        )


                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }

            })
    }


    private fun loadSearchData() {
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)

        userList.addListenerForSingleValueEvent(object:ValueEventListener{
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

    private fun loadUserList() {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()


        adapter = object:FirebaseRecyclerAdapter<User, UserViewHolder>(options)
        {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): UserViewHolder {
                val itemView = LayoutInflater.from(p0.context)
                    .inflate(R.layout.layout_user, p0,false)
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                if(model.email.equals(Common.loggedUser!!.email)){
                    holder.txt_user_email.text = java.lang.StringBuilder(model.email!!).append(" (me)")
                    holder.txt_user_email.setTypeface(holder.txt_user_email.typeface, Typeface.ITALIC)
                }
                else {
                    holder.txt_user_email.text = java.lang.StringBuilder(model.email!!)
                }

                holder.setClick(object:IRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        showDialogRequest(model)
                    }

                })
            }

        }
        adapter!!.startListening()
        recycler_all_people.adapter = adapter
    }

    override fun onStop() {

        if(adapter != null)
            adapter!!.stopListening()
        if(searchAdapter != null)
            searchAdapter!!.stopListening()


        compositeDisposable.clear()

        super.onStop()

    }

    override fun onFirebaseLoadUserDone(lstEmail: List<String>) {
        material_search_bar.lastSuggestions = lstEmail
    }

    override fun onFirebaseLoadFailed(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}




