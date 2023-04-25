package com.example.hexusbykotlin.ui.slideshow

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hexusbykotlin.AllPeopleActivity
import com.example.hexusbykotlin.FriendRequestActivity
import com.example.hexusbykotlin.databinding.FragmentSlideshowBinding

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //val textView: TextView = binding.textSlideshow
        binding.recyclerFriendList.setOnClickListener{
                view ->
            startActivity(Intent(requireContext(), AllPeopleActivity::class.java))}

        return root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}