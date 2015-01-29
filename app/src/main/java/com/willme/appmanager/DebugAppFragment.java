package com.willme.appmanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.content.Loader;

import java.util.List;

/**
 * Created by Wen on 12/29/14.
 */
public class DebugAppFragment extends AppListFragment{

    @Override
    public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
        return new DebugAppListLoader(getActivity());
    }

    static class DebugAppListLoader extends AppListLoader{
        public DebugAppListLoader(Context context) {
            super(context);
        }

        @Override
        protected boolean filterApp(ApplicationInfo app) {
            return AppUtils.isAppDebuggable(getContext(), app.packageName);
        }
    }

    @Override
    public int getTitleId() {
        return R.string.title_debug;
    }

}
