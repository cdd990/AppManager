package com.willme.appmanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.content.Loader;

import java.util.List;

/**
 * Created by Wen on 12/5/14.
 */
public class DisabledAppFragment extends AppListFragment{

    @Override
    public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
        return new DisabledAppListLoader(getActivity());
    }

    static class DisabledAppListLoader extends AppListLoader{
        public DisabledAppListLoader(Context context) {
            super(context);
        }

        @Override
        protected boolean filterApp(ApplicationInfo app) {
            return !app.enabled;
        }
    }

    @Override
    public int getTitleId() {
        return R.string.title_disabled;
    }
}
