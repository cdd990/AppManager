package com.willme.appmanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.content.Loader;

import java.util.List;

/**
 * Created by Wen on 12/5/14.
 */
public class FavoriteAppFragment extends AppListFragment{

    @Override
    public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
        return new FavoriteAppListLoader(getActivity());
    }

    static class FavoriteAppListLoader extends AppListLoader{


        public FavoriteAppListLoader(Context context) {
            super(context);
        }

        @Override
        protected boolean filterApp(ApplicationInfo app) {
            if(app.packageName.startsWith("com.sapp")){
                return true;
            }
            if (app.packageName.startsWith("com.willme")){
                return true;
            }
            return false;
        }

    }

    @Override
    public int getTitleId() {
        return R.string.title_favorites;
    }
}
