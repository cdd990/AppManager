package com.willme.appmanager;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class AppDetailActivity extends BaseActivity implements OnClickListener {

	public static final String EXTRA_PACKAGE_NAME = "packageName";
	
	private PackageManager mPackageManager;
    private PackageInfo mPackageInfo;
	private ApplicationInfo mAppInfo;
	Button mDisableButton;
	UninstallReceiver mReceiver;
	
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
            switch (msg.what){
                case 1:
                    try {
                        mAppInfo = mPackageManager.getPackageInfo(mAppInfo.packageName, 0).applicationInfo;
                        mDisableButton.setText(mAppInfo.enabled?"Disable":"Enable");
                        mDisableButton.setEnabled(true);
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    findViewById(R.id.btn_force_stop).setEnabled(true);
                    break;
            }

		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_detail);
		mPackageManager = getPackageManager();
		String packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
		if(packageName == null){
			packageName = getIntent().getData().getSchemeSpecificPart();
		}
        mDisableButton = (Button) findViewById(R.id.btn_disable);
        try {
            mPackageInfo = mPackageManager.getPackageInfo(packageName, 0);
            mAppInfo = mPackageInfo.applicationInfo;
            String appLable = mPackageManager.getApplicationLabel(mAppInfo).toString();
            ((TextView)findViewById(R.id.tv_app_name)).setText(appLable);
            mDisableButton.setText(mAppInfo.enabled?"Disable":"Enable");
            ((ImageView)findViewById(R.id.iv_app_icon)).setImageDrawable(mPackageManager.getApplicationIcon(packageName));
            ((TextView)findViewById(R.id.tv_uid)).setText(String.valueOf(mAppInfo.uid));
        } catch (NameNotFoundException e) {
            finish();
            return;
        }


        ((TextView) findViewById(R.id.tv_package_name)).setText(packageName);
        mDisableButton.setOnClickListener(this);
        findViewById(R.id.btn_app_info).setOnClickListener(this);
        findViewById(R.id.btn_app_ops).setOnClickListener(this);
        findViewById(R.id.btn_market).setOnClickListener(this);
        findViewById(R.id.btn_app_dir).setOnClickListener(this);
        findViewById(R.id.btn_app_sdcard_dir).setOnClickListener(this);
        findViewById(R.id.btn_open).setOnClickListener(this);
        findViewById(R.id.btn_send_apk).setOnClickListener(this);
        findViewById(R.id.btn_send_info).setOnClickListener(this);
        findViewById(R.id.btn_uninstall).setOnClickListener(this);
        findViewById(R.id.btn_force_stop).setOnClickListener(this);
        if(Build.VERSION.SDK_INT >= 21){
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setActionBar(toolbar);
        }
        getActionBar().setTitle(mAppInfo.loadLabel(mPackageManager));
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mReceiver = new UninstallReceiver();
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_open:
            if(!mAppInfo.enabled){
                Toast.makeText(this, R.string.detail_toast_disabled_while_open, Toast.LENGTH_SHORT).show();
            }else{
                startApplication(mAppInfo.packageName);
            }
			break;
		case R.id.btn_app_info:
			Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+mAppInfo.packageName));
			List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent, 0);
			for(ResolveInfo rInfo : activities){
				if(rInfo.activityInfo.packageName.endsWith(getPackageName())){
					continue;
				}
				intent.setComponent(new ComponentName(rInfo.activityInfo.packageName, rInfo.activityInfo.name));
				startActivity(intent);
			}
			break;
        case R.id.btn_app_ops:
            Intent opsIntent = new Intent("com.willme.appops", Uri.parse("package:"+mAppInfo.packageName));
            try{
                startActivity(opsIntent);
            }catch (Exception e){}
            break;
		case R.id.btn_market:
			Uri uri = Uri.parse("market://details?id=" + mAppInfo.packageName);
			Intent marketIntent = new Intent(Intent.ACTION_VIEW, uri);
            try{
                startActivity(marketIntent);
            }catch (ActivityNotFoundException e){
                uri = Uri.parse("https://play.google.com/store/apps/details?id="+mAppInfo.packageName);
                marketIntent.setData(uri);
                startActivity(marketIntent);
            }
			break;
		case R.id.btn_app_dir:
			openDir(mAppInfo.dataDir);
			break;
		case R.id.btn_app_sdcard_dir:
			openDir("/sdcard/Android/data/" + mAppInfo.packageName);
			break;
		case R.id.btn_send_apk:
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			File apkFile = new File(mAppInfo.sourceDir);
			shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(apkFile));
			shareIntent.setType("*/*");
			startActivity(Intent.createChooser(shareIntent, "Share apk to..."));
			break;
			
		case R.id.btn_send_info:
			Intent shareInfoIntent = new Intent(Intent.ACTION_SEND);
			String infoText = mAppInfo.loadLabel(getPackageManager())+"\n"
						+mAppInfo.packageName+"\n"
                        +"versionName: "+ mPackageInfo.versionName+"\n"
                        +"versionCode: "+mPackageInfo.versionCode+"\n"
						+"uid:"+mAppInfo.uid+"\n"
						+mAppInfo.dataDir+"\n"
						+mAppInfo.sourceDir+"\n"
						+"https://play.google.com/store/apps/details?id="+mAppInfo.packageName;
			shareInfoIntent.putExtra(Intent.EXTRA_TEXT, infoText);
			shareInfoIntent.setType("text/plain");
			startActivity(Intent.createChooser(shareInfoIntent, "Share to..."));
			break;
		case R.id.btn_disable:
			mDisableButton.setEnabled(false);
			new Thread(new Runnable() {
				@Override
				public void run() {
					Shell.SU.run("pm "+(mAppInfo.enabled?"disable ":"enable ")+mAppInfo.packageName);
					mHandler.sendEmptyMessage(1);
				}
			}).start();
			break;
        case R.id.btn_force_stop:
            findViewById(R.id.btn_force_stop).setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Shell.SU.run("am force-stop "+mAppInfo.packageName);
                    mHandler.sendEmptyMessage(2);
                }
            }).start();
            break;
		case R.id.btn_uninstall:
			if((mAppInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0){
				new AlertDialog.Builder(this)
				.setTitle(mAppInfo.loadLabel(mPackageManager))
				.setMessage("Do you want to uninstall this app?")
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								Shell.SU.run("uninstall "+mAppInfo.packageName);
							}
						}).start();
					}
					
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create()
				.show();
			}else{
				Uri packageURI = Uri.parse ("package:" + mAppInfo.packageName); 
				Intent uninstallIntent = new Intent (Intent.ACTION_UNINSTALL_PACKAGE, packageURI);
				startActivity (uninstallIntent); 
			}
			break;
		}
	}
	
	public void openDir(String path){
		Uri dirUri = Uri.parse("file://"+path);
		Intent dirIntent = new Intent(Intent.ACTION_VIEW, dirUri);
		dirIntent.addCategory(Intent.CATEGORY_DEFAULT);
		dirIntent.setFlags(0x14000000);
		dirIntent.setComponent(new ComponentName("com.speedsoftware.rootexplorer", "com.speedsoftware.rootexplorer.RootExplorer"));
		try{
			startActivity(dirIntent);
		}catch(ActivityNotFoundException e){
			e.printStackTrace();
			openDirWithES(path);
		}
	}
	
	private void openDirWithES(String path) {
		Uri dirUri = Uri.parse(path);
		Intent dirIntent = new Intent(Intent.ACTION_VIEW);
		dirIntent.setDataAndType(dirUri, "resource/folder");
		dirIntent.setFlags(0x14000000);
		try{
			startActivity(dirIntent);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void startApplication(String pkgName) {
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		for (ResolveInfo rInfo : 
				mPackageManager.queryIntentActivities(intent, 0)) {
			if (rInfo.activityInfo.packageName.equals(pkgName)) {
				Intent launchIntent = new Intent("android.intent.action.MAIN");
				launchIntent.addCategory("android.intent.category.LAUNCHER");
				launchIntent.setComponent(new ComponentName(
						rInfo.activityInfo.packageName,
						rInfo.activityInfo.name));
				launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (Build.VERSION.SDK_INT >= 16) {
                    View openBtn = findViewById(R.id.btn_open);
                    ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(openBtn, 0, 0,
                            openBtn.getMeasuredWidth(), openBtn.getMeasuredHeight());
                    startActivity(launchIntent, opts.toBundle());
                } else {
                    startActivity(launchIntent);
                }
				return;
			}
		}
        Toast.makeText(this, R.string.detail_toast_no_default_activity, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
        try{
            unregisterReceiver(mReceiver);
        }catch (Exception ignored){}
	}
	
	public class UninstallReceiver extends BroadcastReceiver {

		public UninstallReceiver() {
			IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
            filter.addDataScheme("package");
            registerReceiver(this, filter);
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(mAppInfo.packageName.equals(intent.getData().getSchemeSpecificPart())){
				finish();
			}
		}
		
	}
	
	
}
