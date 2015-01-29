package com.willme.appmanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
	final PackageManager mPm;

	List<AppEntry> mApps;
	PackageIntentReceiver mPackageObserver;

	public AppListLoader(Context context) {
		super(context);

		// Retrieve the package manager for later use; note we don't
		// use 'context' directly but instead the save global application
		// context returned by getContext().
		mPm = getContext().getPackageManager();
	}

	/**
	 * This is where the bulk of our work is done. This function is called in a
	 * background thread and should generate a new set of data to be published
	 * by the loader.
	 */
	@Override
	public List<AppEntry> loadInBackground() {
		// Retrieve all known applications.
		List<ApplicationInfo> apps = getAppList(mPm);
		if (apps == null) {
			apps = new ArrayList<ApplicationInfo>();
		}
        List<AppEntry> entries = new ArrayList<AppEntry>();
        final Context context = getContext();
        for (int i = 0; i < apps.size(); i++) {
            ApplicationInfo aInfo = apps.get(i);
            if(!filterApp(aInfo)){
                continue;
            }
			AppEntry entry = new AppEntry(this, apps.get(i));
            loadApp(entry);
			entries.add(entry);
		}

		// Sort the list.
		Collections.sort(entries, new AppComparator());

		// Done!
		return entries;
	}

    protected List<ApplicationInfo> getAppList(PackageManager pm){
        return pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES
                | PackageManager.GET_DISABLED_COMPONENTS);
    }

    protected boolean filterApp(ApplicationInfo app){
        return true;
    }

    protected void loadApp(AppEntry app){
        app.loadLabel(getContext());
    }

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 */
	@Override
	public void deliverResult(List<AppEntry> apps) {
		if (isReset()) {
			// An async query came in while the loader is stopped. We
			// don't need the result.
			if (apps != null) {
				onReleaseResources(apps);
			}
		}
		List<AppEntry> oldApps = apps;
		mApps = apps;

		if (isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			super.deliverResult(apps);
		}

		// At this point we can release the resources associated with
		// 'oldApps' if needed; now that the new result is delivered we
		// know that it is no longer in use.
		if (oldApps != null) {
			onReleaseResources(oldApps);
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (mApps != null) {
			// If we currently have a result available, deliver it
			// immediately.
			deliverResult(mApps);
		}else{
			forceLoad();
		}

		// Start watching for changes in the app data.
		if (mPackageObserver == null) {
			mPackageObserver = new PackageIntentReceiver(this);
		}

	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Handles a request to cancel a load.
	 */
	@Override
	public void onCanceled(List<AppEntry> apps) {
		super.onCanceled(apps);

		// At this point we can release the resources associated with 'apps'
		// if needed.
		onReleaseResources(apps);
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		// At this point we can release the resources associated with 'apps'
		// if needed.
		if (mApps != null) {
			onReleaseResources(mApps);
			mApps = null;
		}

		// Stop monitoring for changes.
		if (mPackageObserver != null) {
			getContext().unregisterReceiver(mPackageObserver);
			mPackageObserver = null;
		}
	}

	/**
	 * Helper function to take care of releasing resources associated with an
	 * actively loaded data set.
	 */
	protected void onReleaseResources(List<AppEntry> apps) {
		// For a simple List<> there is nothing to do. For something
		// like a Cursor, we would close it here.
	}

}