package com.willme.appmanager;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends BaseAdapter implements Filterable, SectionIndexer {
	private final LayoutInflater mInflater;
    private List<AppEntry> mOriginalValues;
	private List<AppEntry> mData;
    private AppFilter mFilter;
    private String mQueryString = null;

    Object mLock = new Object();

	public AppListAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	public void setData(List<AppEntry> data) {
		mData = data;
        mOriginalValues = null;
        if(mQueryString != null)
            getFilter().filter(mQueryString);
		notifyDataSetChanged();
	}

    public void setQueryString(String queryString) {
        if (queryString != null) {
            if (queryString.equals(mQueryString)) {
                return;
            }
        }
        mQueryString = queryString;
        getFilter().filter(mQueryString);
    }

	/**
	 * Populate new items in the list.
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.list_item_appinfo, parent, false);
		} else {
			view = convertView;
		}
		final AppEntry item = (AppEntry) getItem(position);
        ImageView appIcon = (ImageView) view.findViewById(R.id.iv_app_icon);
        appIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startApplication(v, item.getApplicationInfo().packageName);
            }
        });
		appIcon.setImageDrawable(item.getIcon());
		((TextView) view.findViewById(R.id.tv_app_name)).setText(item
				.getLabel());
		((TextView) view.findViewById(R.id.tv_package_name)).setText(item
				.getApplicationInfo().packageName);
		((TextView) view.findViewById(R.id.tv_uid)).setText(String.valueOf(item
				.getApplicationInfo().uid));
		return view;
	}


    void startApplication(View v, String pkgName) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        for (ResolveInfo rInfo : v.getContext().getPackageManager().queryIntentActivities(intent, 0)) {
            if (rInfo.activityInfo.packageName.equals(pkgName)) {
                Intent launchIntent = new Intent("android.intent.action.MAIN");
                launchIntent.addCategory("android.intent.category.LAUNCHER");
                launchIntent.setComponent(new ComponentName(
                        rInfo.activityInfo.packageName,
                        rInfo.activityInfo.name));
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Activity activity = (Activity)v.getContext();
                if (Build.VERSION.SDK_INT >= 16) {
                    ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0,
                            v.getMeasuredWidth(), v.getMeasuredHeight());
                    activity.startActivity(launchIntent, opts.toBundle());
                } else {
                    activity.startActivity(launchIntent);
                }
                return;
            }
        }
        Toast.makeText(v.getContext(), R.string.detail_toast_no_default_activity, Toast.LENGTH_SHORT).show();
    }


    @Override
	public int getCount() {
		return mData == null ? 0 : mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

    @Override
    public Filter getFilter() {

        if(mFilter == null){
            mFilter = new AppFilter();
        }
        return mFilter;
    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    class AppFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<>(mData);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<AppEntry> list = new ArrayList<>();
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<AppEntry> values;
                synchronized (mLock) {
                    values = new ArrayList<>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<AppEntry> newValues = new ArrayList<>();

                boolean isEngChars = prefixString.matches("[a-z]+");
                boolean isDigit = prefixString.matches("[0-9]+");

                outer:
                for (int i = 0; i < count; i++) {
                    final AppEntry app = values.get(i);

                    String pkgName = app.getApplicationInfo().packageName.toLowerCase();
                    String[] pkgParts = pkgName.split("\\.");
                    for(String p:pkgParts){
                        if(p.startsWith(prefixString)){
                            newValues.add(app);
                            continue outer;
                        }
                    }
                    String label = app.getLabel();
                    String labelLowerCase = label.toLowerCase();

                    if(labelLowerCase.startsWith(prefixString)){
                        newValues.add(app);
                        continue;
                    }

                    if(isEngChars){
                        final String[] words = labelLowerCase.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(app);
                                continue outer;
                            }
                        }
                    }else if(isDigit){
                        String uidStr = String.valueOf(app.getApplicationInfo().uid);
                        if(uidStr.startsWith(prefixString)){
                            newValues.add(app);
                        }
                    }else{
                        if(labelLowerCase.contains(prefixString)){
                            newValues.add(app);
                        }
                        continue;
                    }

                    if(app.pinyin == null || app.pinyinIndex == null){
                        app.loadPinyin();
                    }


                    if(app.pinyinIndex.startsWith(prefixString)){
                        newValues.add(app);
                        continue;
                    }

                    if(app.pinyin.startsWith(prefixString)){
                        newValues.add(app);
                        continue;
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mData = (List<AppEntry>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

}
