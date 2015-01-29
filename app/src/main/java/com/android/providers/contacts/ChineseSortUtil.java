package com.android.providers.contacts;

import java.util.ArrayList;

import com.android.providers.contacts.HanziToPinyin.Token;

public class ChineseSortUtil {

	public static String getSortKey(String displayName) {
        ArrayList<Token> tokens = HanziToPinyin.getInstance().get(displayName);
        if (tokens != null && tokens.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Token token : tokens) {
                // Put Chinese character's pinyin, then proceed with the
                // character itself.
                if (Token.PINYIN == token.type) {
                    if (sb.length() > 0) {
                        sb.append(' ');
                    }
                    sb.append(token.target);
                    sb.append(' ');
                    sb.append(token.source);
                } else {
                    if (sb.length() > 0) {
                        sb.append(' ');
                    }
                    sb.append(token.source);
                }
            }
            return sb.toString();
        }
        return displayName;
    }
	
}
