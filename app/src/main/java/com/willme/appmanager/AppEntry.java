package com.willme.appmanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

import com.android.providers.contacts.HanziToPinyin;

import java.io.File;
import java.util.ArrayList;

public class AppEntry {

	private final AppListLoader mLoader;
	private final ApplicationInfo mInfo;
	private final File mApkFile;
	private String mLabel;
    public String pinyin;
    public String pinyinIndex;
	private Drawable mIcon;
	private boolean mMounted;
	
	public AppEntry(AppListLoader loader, ApplicationInfo info) {
		mLoader = loader;
		mInfo = info;
		mApkFile = new File(info.sourceDir);
	}

	public ApplicationInfo getApplicationInfo() {
		return mInfo;
	}

	public String getLabel() {
		return mLabel;
	}

	public Drawable getIcon() {
		if (mIcon == null) {
			if (mApkFile.exists()) {
				mIcon = mInfo.loadIcon(mLoader.mPm);
				return mIcon;
			} else {
				mMounted = false;
			}
		} else if (!mMounted) {
			// If the app wasn't mounted but is now mounted, reload
			// its icon.
			if (mApkFile.exists()) {
				mMounted = true;
				mIcon = mInfo.loadIcon(mLoader.mPm);
				return mIcon;
			}
		} else {
			return mIcon;
		}

		return mLoader.getContext().getResources()
				.getDrawable(android.R.drawable.sym_def_app_icon);
	}

	/*
	 * public Drawable getIcon(){ if(mIcon != null){ return mIcon; }
	 * if("com.qihoo.appstore".equals(mInfo.packageName)){ Log.e("", ""); }
	 * Context context = mLoader.getContext(); PackageManager pm =
	 * context.getPackageManager(); Intent intent =
	 * pm.getLaunchIntentForPackage(mInfo.packageName); if(intent == null){
	 * mIcon = getApplicationIcon(context, mInfo.packageName); }else{ mIcon =
	 * getActivityIcon(context, intent.getComponent()); } if(mIcon == null){
	 * mIcon = pm.getDefaultActivityIcon(); } return mIcon; }
	 */

	@Override
	public String toString() {
		return mLabel;
	}

	void loadLabel(Context context) {
		if (mLabel == null || !mMounted) {
			if (!mApkFile.exists()) {
				mMounted = false;
				mLabel = mInfo.packageName;
			} else {
				mMounted = true;
				CharSequence label = mInfo.loadLabel(context
						.getPackageManager());
				mLabel = label != null ? label.toString() : mInfo.packageName;
			}
		}
	}

    public void loadPinyin(){
        if(mLabel == null){
            throw new RuntimeException("Load the pinyin after you load the label.");
        }
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(mLabel);
        StringBuffer sb1 = new StringBuffer();
        StringBuffer sb2 = new StringBuffer();
        for (HanziToPinyin.Token token : tokens){
            sb1.append(token.target.charAt(0));
            sb2.append(token.target);
        }
        pinyinIndex = sb1.toString().toLowerCase();
        pinyin = sb2.toString().toLowerCase();
    }

}