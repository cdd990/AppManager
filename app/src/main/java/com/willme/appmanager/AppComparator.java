package com.willme.appmanager;

import com.android.providers.contacts.ChineseSortUtil;

import java.util.Comparator;

public class AppComparator implements Comparator<AppEntry>{
	
    @Override
    public int compare(AppEntry object1, AppEntry object2) {
        String pinyin1 = ChineseSortUtil.getSortKey(object1.getLabel());
        String pinyin2 = ChineseSortUtil.getSortKey(object2.getLabel());
        return pinyin1.compareToIgnoreCase(pinyin2);
	}
	
}