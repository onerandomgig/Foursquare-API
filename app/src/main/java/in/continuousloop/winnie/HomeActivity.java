package in.continuousloop.winnie;

import android.Manifest;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import in.continuousloop.winnie.constants.AppConstants;
import in.continuousloop.winnie.fragments.ProfileFragment;
import in.continuousloop.winnie.fragments.StoriesFragment;
import in.continuousloop.winnie.fragments.WinnieMapViewFragment;
import in.continuousloop.winnie.utils.PermissionsManager;

/**
 * The home activity to show venues at a location.
 */
public class HomeActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a{@link FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter sectionsPagerAdapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabLayout) TabLayout tabLayout;
    @BindView(R.id.container) ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Custom view for tab
        TabLayout.Tab mapsTab = _createTab(tabLayout, R.drawable.icon_wn_explore, R.string.section_map);
        _toggleTabColor(mapsTab, true);
        tabLayout.addTab(mapsTab, 0);
        tabLayout.addTab(_createTab(tabLayout, R.drawable.icon_wn_stories, R.string.section_stories), 1);
        tabLayout.addTab(_createTab(tabLayout, R.drawable.icon_wn_profile, R.string.section_profile), 2);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                viewPager.setCurrentItem(tab.getPosition());

                _toggleTabColor(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);

                _toggleTabColor(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                super.onTabReselected(tab);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != AppConstants.LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionsManager.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.

            if (viewPager.getCurrentItem() == 0) {
                WinnieMapViewFragment mapFragment = (WinnieMapViewFragment) sectionsPagerAdapter.getItem(viewPager.getCurrentItem());
                mapFragment.locationPermissionsGranted();
            }
        }
    }

    /**
     * Create a tab and set the specified icon and text
     *
     * @param tabLayout     - The tablayout to which the tab has to be added
     * @param aIconDrawable - The icon drawable id for the tab icon
     * @param aTabTextId    - The text resource id for the tab text
     *
     * @return {@link android.support.design.widget.TabLayout.Tab}
     */
    private TabLayout.Tab _createTab(TabLayout tabLayout, int aIconDrawable, int aTabTextId) {
        TabLayout.Tab lTab = tabLayout.newTab();

        View lTabView = getLayoutInflater().inflate(R.layout.item_tabs, null);
        ImageView tabIcon = ButterKnife.findById(lTabView, R.id.tabIcon);
        TextView tabText = ButterKnife.findById(lTabView, R.id.tabText);

        tabIcon.setImageResource(aIconDrawable);
        tabText.setText(getResources().getString(aTabTextId));
        lTab.setCustomView(lTabView);

        return lTab;
    }

    /**
     * Toggle tab icon color when it is selected / unselected
     * @param tab - Tab to toggle icon color for
     * @param enabled - true when selected, false otherwise
     */
    private void _toggleTabColor(TabLayout.Tab tab, boolean enabled) {

        int tabIconColor;
        if (enabled) {
            tabIconColor = ContextCompat.getColor(HomeActivity.this, R.color.colorAccent);
        } else {
            tabIconColor = ContextCompat.getColor(HomeActivity.this, R.color.darkGray);
        }

        ImageView icon = ButterKnife.findById(tab.getCustomView(), R.id.tabIcon);
        icon.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private List<String> tabs;
        private List<Fragment> tabFragments;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            tabs = new ArrayList<>();
            tabs.add(getApplication().getResources().getString(R.string.section_map));
            tabs.add(getApplication().getResources().getString(R.string.section_stories));
            tabs.add(getApplication().getResources().getString(R.string.section_profile));

            tabFragments = new ArrayList<>();
            tabFragments.add(new WinnieMapViewFragment());
            tabFragments.add(new StoriesFragment());
            tabFragments.add(new ProfileFragment());
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given tab.
            return tabFragments.get(position);
        }

        @Override
        public int getCount() {
            // Show as many sections as configured (explore, stories and profile tabs)
            return tabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs.get(position);
        }
    }
}
