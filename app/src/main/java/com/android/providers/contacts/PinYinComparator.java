package com.android.providers.contacts;

import java.util.Comparator;

public class PinYinComparator implements Comparator<String> {

	@Override
	public int compare(String lhs, String rhs) {
		return ChineseSortUtil.getSortKey(lhs).compareToIgnoreCase(ChineseSortUtil.getSortKey(rhs));
	}

}
