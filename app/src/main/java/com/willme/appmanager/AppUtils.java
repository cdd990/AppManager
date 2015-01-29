package com.willme.appmanager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.x500.X500Principal;

/**
 * Created by Wen on 12/29/14.
 */
public class AppUtils {

    private static final X500Principal DEBUG_DN = new X500Principal("CN=Android Debug,O=Android,C=US");

    public static boolean isAppDebuggable(Context context, String pkgName){
        boolean debuggable = false;
        try
        {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
            Signature signatures[] = pinfo.signatures;

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            for ( int i = 0; i < signatures.length;i++)
            {
                ByteArrayInputStream stream = new ByteArrayInputStream(signatures[i].toByteArray());
                X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);
                debuggable = cert.getSubjectX500Principal().equals(DEBUG_DN);
                if (debuggable)
                    break;
            }
        }catch (Exception e){}
        return debuggable;
    }

    static final Pattern CHINESE_PATTERN = Pattern.compile("[\u4e00-\u9fa5]+");

    public static String getFirstMatchedChineseString(String string){
        Matcher matcher = CHINESE_PATTERN.matcher(string);
        if(matcher.find()){
            return matcher.group();
        }else{
            return null;
        }
    }

}
