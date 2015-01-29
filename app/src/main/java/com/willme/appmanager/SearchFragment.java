package com.willme.appmanager;

import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

import java.util.List;

/**
 * Created by Wen on 1/4/15.
 */
public class SearchFragment extends AppListFragment{

    String mQueryString = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No matching apps");
    }

    public void setQueryString(String queryString){
        mQueryString = queryString;
        mAdapter.setQueryString(mQueryString);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
        return new QueryAppListLoader(getActivity());
    }

    static class QueryAppListLoader extends AppListLoader{
        public QueryAppListLoader(Context context) {
            super(context);
        }

        @Override
        protected void loadApp(AppEntry app) {
            super.loadApp(app);
            app.loadPinyin();
        }

    }

    @Override
    public int getTitleId() {
        return 0;
    }
}
