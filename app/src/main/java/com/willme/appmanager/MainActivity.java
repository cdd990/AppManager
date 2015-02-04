/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.willme.appmanager;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toolbar;

import com.android.dialer.widget.OverlappingPaneLayout;
import com.willme.appmanager.view.SlidingTabLayout;

public class MainActivity extends BaseActivity {

	ViewPager mViewPager;
    SlidingTabLayout mSlidingTabLayout;
    Toolbar mToolbar;

    AppListFragment mDebugFragment, mAllFragment, mDisabledFragment;

    private OverlappingPaneLayout.PanelSlideCallbacks mPanelSlideCallbacks = new OverlappingPaneLayout.PanelSlideCallbacks() {

        @Override
        public void onPanelSlide(View panel, float slideOffset) {
            mToolbar.setTranslationY(-mToolbar.getHeight()*(1-slideOffset));
        }

        @Override
        public void onPanelOpened(View panel) {

        }

        @Override
        public void onPanelClosed(View panel) {

        }

        @SuppressLint("NewApi")
        @Override
        public void onPanelFlingReachesEdge(int velocityY) {
            if(getCurrentList() != null)
                getCurrentList().fling(velocityY);
        }

        @Override
        public boolean isScrollableChildUnscrolled() {
            return true;
        }
    };
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if(Build.VERSION.SDK_INT >= 21){
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            mToolbar.setElevation(0.0f);
            setActionBar(mToolbar);
        }else{
            mSlidingTabLayout = (SlidingTabLayout) getLayoutInflater().inflate(R.layout.sliding_tab, null);
            getActionBar().setCustomView(mSlidingTabLayout);
            getActionBar().setTitle(null);
            getActionBar().setDisplayShowHomeEnabled(false);
            getActionBar().setDisplayShowCustomEnabled(true);
        }
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        if(savedInstanceState != null){
            mDebugFragment = (AppListFragment) getFragmentManager().findFragmentByTag(getFragmentName(0));
            mAllFragment = (AppListFragment) getFragmentManager().findFragmentByTag(getFragmentName(1));
            mDebugFragment = (AppListFragment) getFragmentManager().findFragmentByTag(getFragmentName(2));
        }

        mViewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {

            private final int[] TITLES = new int[] {
                    R.string.title_debug,
                    R.string.title_all_apps,
                    R.string.title_disabled
            };
			@Override
			public int getCount() {
				return 3;
			}
			
			@Override
			public Fragment getItem(int position) {

                switch (position){
                    case 0:
                        mDebugFragment = new DebugAppFragment();
                        return mDebugFragment;
                    case 1:
                        mAllFragment = new AppListFragment();
                        return mAllFragment;
                    case 2:
                        mDisabledFragment = new DisabledAppFragment();
                        return mDisabledFragment;
                    case 3:
                        return new FavoriteAppFragment();
                }
                return null;
			}

            @Override
            public CharSequence getPageTitle(int position) {
                return getString(TITLES[position]);
            }
        });

        if(mSlidingTabLayout == null)
            mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sildingtab);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        mSlidingTabLayout.setSelectedIndicatorColors(Color.WHITE);
        mSlidingTabLayout.setViewPager(mViewPager);
        if(Build.VERSION.SDK_INT >= 21)
            setupPaneLayout((OverlappingPaneLayout) findViewById(R.id.lists_frame));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_search){
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupPaneLayout(OverlappingPaneLayout paneLayout) {
        // TODO: Remove the notion of a capturable view. The entire view be slideable, once
        // the framework better supports nested scrolling.
        paneLayout.setCapturableView(mSlidingTabLayout);
        paneLayout.openPane();
        paneLayout.setPanelSlideCallbacks(mPanelSlideCallbacks);
        paneLayout.setIntermediatePinnedOffset(mToolbar.getHeight());
        LayoutTransition transition = paneLayout.getLayoutTransition();
        // Turns on animations for all types of layout changes so that they occur for
        // height changes.
        transition.enableTransitionType(LayoutTransition.CHANGING);
     }

    private ListView getCurrentList(){
        int item = mViewPager.getCurrentItem();
        switch (item){
            case 0:
                return mDebugFragment.getListView();
            case 1:
                return mAllFragment.getListView();
            case 2:
                return mDisabledFragment.getListView();
        }
        return null;
    }

    private String getFragmentName(int index) {
        return "android:switcher:" + mViewPager.getId() + ":" + index;
    }
}
