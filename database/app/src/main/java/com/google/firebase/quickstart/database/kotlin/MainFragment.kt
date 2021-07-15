package com.google.firebase.quickstart.database.kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.databinding.FragmentMainBinding
import com.google.firebase.quickstart.database.kotlin.listfragments.MyPostsFragment
import com.google.firebase.quickstart.database.kotlin.listfragments.MyTopPostsFragment
import com.google.firebase.quickstart.database.kotlin.listfragments.RecentPostsFragment

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: FragmentStateAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        // Create the adapter that will return a fragment for each section
        pagerAdapter = object : FragmentStateAdapter(parentFragmentManager, viewLifecycleOwner.lifecycle) {
            private val fragments = arrayOf<Fragment>(
                    RecentPostsFragment(),
                    MyPostsFragment(),
                    MyTopPostsFragment())

            override fun createFragment(position: Int) = fragments[position]

            override fun getItemCount() = fragments.size
        }

        // Set up the ViewPager with the sections adapter.
        with(binding) {
            container.adapter = pagerAdapter
            TabLayoutMediator(tabs, container) { tab, position ->
                tab.text = when(position) {
                    0 -> getString(R.string.heading_recent)
                    1 -> getString(R.string.heading_my_posts)
                    else -> getString(R.string.heading_my_top_posts)
                }
            }.attach()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_logout) {
            Firebase.auth.signOut()
            findNavController().navigate(R.id.action_MainFragment_to_SignInFragment)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}