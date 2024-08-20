package com.google.firebase.quickstart.analytics.kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.quickstart.analytics.R
import com.google.firebase.quickstart.analytics.databinding.ActivityMainBinding
import com.google.firebase.quickstart.analytics.kotlin.data.Constants
import com.google.firebase.quickstart.analytics.kotlin.data.ImageInfo
import java.util.Locale

/**
 * Activity which displays numerous background images that may be viewed. These background images
 * are shown via {@link ImageFragment}.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    /**
     * The [androidx.viewpager2.widget.PagerAdapter] that will provide fragments for each image.
     * This uses a [FragmentStateAdapter], which keeps every loaded fragment in memory.
     */
    private lateinit var imagePagerAdapter: ImagePagerAdapter

    private lateinit var context: Context

    // Injects FirebaseAnalytics and app measurement configuration from the factory for centralized management.
    // [START declare_analytics]
    // [START shared_app_measurement]
    private val viewModel: FirebaseAnalyticsViewModel by viewModels { FirebaseAnalyticsViewModel.Factory }
    // [END shared_app_measurement]
    // [END declare_analytics]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = applicationContext

        // On first app open, ask the user his/her favorite food. Then set this as a user property
        // on all subsequent opens.
        viewModel.getUserFavoriteFood(context)
        if (viewModel.userFavoriteFood.value == null) {
            askFavoriteFood()
        } else {
            // [START user_property]
            viewModel.setUserFavoriteFood(context, viewModel.userFavoriteFood.value)
            // [END user_property]
        }

        // Create the adapter that will return a fragment for each image.
        imagePagerAdapter = ImagePagerAdapter(supportFragmentManager, Constants.IMAGE_INFOS, lifecycle)

        // Set up the ViewPager with the pattern adapter.
        binding.viewPager.adapter = imagePagerAdapter

        val pageChangedCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                recordImageView()
                recordScreenView()
            }
        }

        binding.viewPager.registerOnPageChangeCallback(pageChangedCallback)

        val tabLayout: TabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, binding.viewPager) { tab, position ->
            tab.setText(Constants.IMAGE_INFOS[position].title)
        }.attach()

        // Send initial screen screen view hit.
        recordImageView()
    }

    public override fun onResume() {
        super.onResume()
        recordScreenView()
    }

    /**
     * Display a dialog prompting the user to pick a favorite food from a list, then record
     * the answer.
     */
    private fun askFavoriteFood() {
        val choices = resources.getStringArray(R.array.food_items)
        val ad = AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.food_dialog_title)
                .setItems(choices) { _, which ->
                    val food = choices[which]

                    // [START user_property]
                    viewModel.setUserFavoriteFood(context, food)
                    // [END user_property]
                }.create()
        ad.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.menu_share) {
            val name = getCurrentImageTitle()
            val text = "I'd love you to hear about $name"

            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, text)
            sendIntent.type = "text/plain"
            startActivity(sendIntent)

            // [START custom_event]
            viewModel.recordShareEvent(name, text)
            // [END custom_event]
        }
        return false
    }

    /**
     * Return the title of the currently displayed image.
     *
     * @return title of image
     */
    private fun getCurrentImageTitle(): String {
        val position = binding.viewPager.currentItem
        val info = Constants.IMAGE_INFOS[position]
        return getString(info.title)
    }

    /**
     * Return the id of the currently displayed image.
     *
     * @return id of image
     */
    private fun getCurrentImageId(): String {
        val position = binding.viewPager.currentItem
        val info = Constants.IMAGE_INFOS[position]
        return getString(info.id)
    }

    /**
     * Record a screen view for the visible [ImageFragment] displayed
     * inside [FragmentStateAdapter].
     */
    private fun recordImageView() {
        val id = getCurrentImageId()
        val name = getCurrentImageTitle()

        // [START image_view_event]
        viewModel.recordImageView(id, name)
        // [END image_view_event]
    }

    /**
     * This sample has a single Activity, so we need to manually record "screen views" as
     * we change fragments.
     */
    private fun recordScreenView() {
        // This string must be <= 36 characters long.
        val screenName = "${getCurrentImageId()}-${getCurrentImageTitle()}"

        // [START set_current_screen]
        viewModel.recordScreenView(screenName, "MainActivity")
        // [END set_current_screen]
    }

    /**
     * A [FragmentStateAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class ImagePagerAdapter(
        fm: FragmentManager,
        private val infos: Array<ImageInfo>,
        lifecyle: Lifecycle
    ) : FragmentStateAdapter(fm, lifecyle) {

        fun getPageTitle(position: Int): CharSequence? {
            if (position < 0 || position >= infos.size) {
                return null
            }
            val l = Locale.getDefault()
            val info = infos[position]
            return getString(info.title).uppercase(l)
        }

        override fun getItemCount(): Int = infos.size

        override fun createFragment(position: Int): Fragment {
            val info = infos[position]
            return ImageFragment.newInstance(info.image)
        }
    }
}
