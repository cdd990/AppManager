package com.willme.appmanager;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;


public class ComponentsActivity extends Activity{

    Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = getFragmentManager().findFragmentById(android.R.id.content);
        if (mFragment == null) {
            mFragment = ComponentListFragment
                    .newInstance(getIntent()
                                    .getStringExtra(AppDetailActivity.EXTRA_PACKAGE_NAME),
                            1);
            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, mFragment)
                    .commit();
        }
    }
}
