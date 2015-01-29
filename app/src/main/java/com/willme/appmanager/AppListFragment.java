package com.willme.appmanager;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

public class AppListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<List<AppEntry>> {

	// This is the Adapter being used to display the list's data.
	AppListAdapter mAdapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Give some text to display if there is no data. In a real
		// application this would come from a resource.
		setEmptyText("No applications");
        getListView().setFastScrollEnabled(true);
        if(Build.VERSION.SDK_INT >= 21)
            getListView().setNestedScrollingEnabled(true);

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new AppListAdapter(getActivity());
		setListAdapter(mAdapter);

		// Start out with a progress indicator.
		setListShown(false);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
        getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onResume() {
		super.onResume();
        getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(getActivity(), AppDetailActivity.class);
		AppEntry app = (AppEntry) mAdapter.getItem(position);
		intent.putExtra(AppDetailActivity.EXTRA_PACKAGE_NAME,
				app.getApplicationInfo().packageName);
		startActivity(intent);
	}

	@Override
	public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
		return new AppListLoader(getActivity());
	}
	

	@Override
	public void onLoadFinished(Loader<List<AppEntry>> loader,
			List<AppEntry> data) {
		mAdapter.setData(data);
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<AppEntry>> loader) {
		mAdapter.setData(null);
	}

    public int getTitleId(){
        return R.string.title_all_apps;
    }
    
}