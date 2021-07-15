package com.google.firebase.quickstart.database.java;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentMainBinding;
import com.google.firebase.quickstart.database.java.listfragments.MyPostsFragment;
import com.google.firebase.quickstart.database.java.listfragments.MyTopPostsFragment;
import com.google.firebase.quickstart.database.java.listfragments.RecentPostsFragment;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        // Create the adapter that will return a fragment for each section
        FragmentStateAdapter mPagerAdapter = new FragmentStateAdapter(getParentFragmentManager(),
                getViewLifecycleOwner().getLifecycle()) {
            private final Fragment[] mFragments = new Fragment[]{
                    new RecentPostsFragment(),
                    new MyPostsFragment(),
                    new MyTopPostsFragment(),
            };

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return mFragments[position];
            }

            @Override
            public int getItemCount() {
                return mFragments.length;
            }
        };
        // Set up the ViewPager with the sections adapter.
        binding.container.setAdapter(mPagerAdapter);
        String[] mFragmentNames = new String[]{
                getString(R.string.heading_recent),
                getString(R.string.heading_my_posts),
                getString(R.string.heading_my_top_posts)
        };
        new TabLayoutMediator(binding.tabs, binding.container,
                (tab, position) -> tab.setText(mFragmentNames[position])
        ).attach();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_MainFragment_to_SignInFragment);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
