package com.example.hexusbykotlin

//import android.content.ClipData
//import android.graphics.Typeface
//import android.widget.ImageView
//import com.google.android.material.snackbar.Snackbar
//import androidx.navigation.findNavController
//import androidx.navigation.ui.AppBarConfiguration
//import androidx.navigation.ui.navigateUp
//import androidx.navigation.ui.setupActionBarWithNavController
//import androidx.navigation.ui.setupWithNavController
//import androidx.drawerlayout.widget.DrawerLayout
//import androidx.core.view.GravityCompat
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hexusbykotlin.Interface.IFirebaseLoadDone
import com.example.hexusbykotlin.Interface.IRecyclerItemClickListener
import com.example.hexusbykotlin.Model.User
import com.example.hexusbykotlin.Utils.Common
import com.example.hexusbykotlin.ViewHolder.UserViewHolder
import com.example.hexusbykotlin.databinding.ActivityHomeBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar

class HomeActivity : AppCompatActivity(), IFirebaseLoadDone {
    //private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var nav_view : NavigationView
    //private lateinit var user_email : TextView
    private lateinit var material_search_bar : MaterialSearchBar
    private lateinit var recycler_friend_list : RecyclerView
    var adapter: FirebaseRecyclerAdapter<User, UserViewHolder>? = null
    var searchAdapter: FirebaseRecyclerAdapter<User, UserViewHolder>? = null
    lateinit var iFirebaseLoadDone: IFirebaseLoadDone
    var suggestList:List<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        material_search_bar = findViewById(R.id.material_search_bar)
        recycler_friend_list = findViewById(R.id.recycler_friend_list)
        //user_email = findViewById(R.id.user_email)
        nav_view = findViewById( R.id.nav_view)
        setSupportActionBar(binding.appBarHome.toolbar)

        binding.appBarHome.fab.setOnClickListener { view ->
           startActivity(Intent(this@HomeActivity, AllPeopleActivity::class.java))
        }


        //val navView: NavigationView = binding.navView
        val header = nav_view.getHeaderView(0)
        val user_email = header.findViewById<View>(R.id.user_email) as TextView
        user_email.text = Common.loggedUser!!.email!!

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
                        recycler_friend_list.adapter=adapter
                    }
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString())
            }

            override fun onButtonClicked(buttonCode: Int) {

            }

        })
        recycler_friend_list.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler_friend_list.layoutManager = layoutManager
        recycler_friend_list.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))

        loadFriendList()
        loadSearchData()
        iFirebaseLoadDone = this

        /*
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_show_people, R.id.nav_friend_request, R.id.nav_sign_out
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        */
    }

    private fun loadSearchData() {
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

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

    private fun startSearch(search_string:String){
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)
            .orderByChild("email")
            .startAt(search_string)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        searchAdapter = object:FirebaseRecyclerAdapter<User,UserViewHolder>(options){
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): UserViewHolder {
                val itemView = LayoutInflater.from(p0.context)
                    .inflate(R.layout.layout_user, p0,false)
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.setClick(object:IRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        startActivity(Intent(this@HomeActivity, TrackingActivity::class.java))
                    }

                })
            }

        }
        searchAdapter!!.startListening()
        recycler_friend_list.adapter = searchAdapter
    }
    private fun loadFriendList() {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        adapter = object:FirebaseRecyclerAdapter<User,UserViewHolder>(options){
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): UserViewHolder {
                val itemView = LayoutInflater.from(p0.context)
                    .inflate(R.layout.layout_user, p0,false)
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.setClick(object:IRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        startActivity(Intent(this@HomeActivity, TrackingActivity::class.java))
                    }

                })
            }

        }
        adapter!!.startListening()
        recycler_friend_list.adapter = adapter
    }
    override fun onStop() {

        if(adapter != null)
            adapter!!.stopListening()
        if(searchAdapter != null)
            searchAdapter!!.stopListening()


        super.onStop()

    }

    override fun onResume() {
        super.onResume()
        if(adapter != null)
            adapter!!.startListening()
        if(searchAdapter != null)
            searchAdapter!!.startListening()


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_main_drawer, menu)
        return true
    }

   /* override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_show_people -> {
                startActivity(Intent(this@HomeActivity, AllPeopleActivity::class.java))
                true
            }
            R.id.nav_friend_request ->{
                startActivity(Intent(this@HomeActivity, FriendRequestActivity::class.java))
                return true
            }
            R.id.nav_sign_out ->{
                Toast.makeText(applicationContext, "click on exit", Toast.LENGTH_LONG).show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onFirebaseLoadUserDone(lstEmail: List<String>) {
        material_search_bar.lastSuggestions = lstEmail
    }

    override fun onFirebaseLoadFailed(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}

