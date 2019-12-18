package com.google.firebase.quickstart.database.kotlin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.fragment.MyPostsFragment
import com.google.firebase.quickstart.database.kotlin.fragment.MyTopPostsFragment
import com.google.firebase.quickstart.database.kotlin.fragment.RecentPostsFragment
import kotlinx.android.synthetic.main.activity_main.container
import kotlinx.android.synthetic.main.activity_main.fabNewPost
import kotlinx.android.synthetic.main.activity_main.tabs

class MainActivity : BaseActivity() {

    private lateinit var pagerAdapter: FragmentPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create the adapter that will return a fragment for each section
        pagerAdapter = object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            private val fragments = arrayOf<Fragment>(
                    RecentPostsFragment(),
                    MyPostsFragment(),
                    MyTopPostsFragment())

            private val fragmentNames = arrayOf(
                    getString(R.string.heading_recent),
                    getString(R.string.heading_my_posts),
                    getString(R.string.heading_my_top_posts))

            override fun getItem(position: Int): Fragment {
                return fragments[position]
            }

            override fun getCount() = fragments.size

            override fun getPageTitle(position: Int): CharSequence? {
                return fragmentNames[position]
            }
        }

        // Set up the ViewPager with the sections adapter.
        container.adapter = pagerAdapter
        tabs.setupWithViewPager(container)

        // Button launches NewPostActivity
        fabNewPost.setOnClickListener {
            startActivity(Intent(this, NewPostActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private const val TAG = "MainActivity"
    }
}
