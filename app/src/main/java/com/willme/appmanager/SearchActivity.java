package com.willme.appmanager;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by Wen on 1/5/15.
 */
public class SearchActivity extends BaseActivity{

    SearchFragment mFragment;
    EditText mEditText;
    ViewGroup mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if(Build.VERSION.SDK_INT >= 21){
            mSearchView = (ViewGroup) findViewById(R.id.search_bar);
        }else{
            mSearchView = (ViewGroup) getLayoutInflater().inflate(R.layout.search_bar, null);
            getActionBar().setCustomView(mSearchView, new ActionBar.LayoutParams(-1, -1));
            getActionBar().setTitle(null);
            getActionBar().setDisplayShowHomeEnabled(false);
            getActionBar().setDisplayShowCustomEnabled(true);
        }

        mEditText = (EditText) mSearchView.findViewById(R.id.et_search);
        if(savedInstanceState != null){
            mFragment = (SearchFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_search);
        }
        if(mFragment ==null){
            mFragment = new SearchFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.frag_search, mFragment)
                    .hide(mFragment)
                    .commit();
        }
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0){
                    getFragmentManager()
                            .beginTransaction()
                            .hide(mFragment)
                            .commit();
                }else{
                    getFragmentManager()
                            .beginTransaction()
                            .show(mFragment)
                            .commit();
                }
                mFragment.setQueryString(s.toString());
            }
        });
        mSearchView.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSearchView.findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setText("");
            }
        });
    }
}
