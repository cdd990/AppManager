package com.willme.appmanager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import eu.chainfire.libsuperuser.Shell;


public class ProxyActivity extends Activity {

    public static final String EXTRA_INTENT = "intent";
    public static final String EXTRA_COMPONENT = "component";
    public static final String EXTRA_USE_ROOT = "useRoot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent().getParcelableExtra(EXTRA_INTENT);
        try{
            if(intent != null){
                startActivity(intent);
            }else{
                ComponentName component = getIntent().getParcelableExtra(EXTRA_COMPONENT);
                if(component != null){
                    if(getIntent().getBooleanExtra(EXTRA_USE_ROOT, false)){
                        Shell.SU.run("am start -n "+component.getPackageName()+"/"+component.getClassName());
                    }else{
                        intent = new Intent();
                        intent.setComponent(component);
                        startActivity(intent);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finish();
    }

}
