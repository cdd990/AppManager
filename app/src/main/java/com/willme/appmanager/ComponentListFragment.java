package com.willme.appmanager;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class ComponentListFragment extends Fragment implements AbsListView.OnItemClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PACKAGE_NAME = "packageName";
    private static final String ARG_TYPE = "type";
    public static final int TYPE_ACTIVITY = 1;
    public static final int TYPE_SERVICE = 2;
    public static final int TYPE_RECEIVER = 3;
    public static final int TYPE_PROVIDER = 4;


    private String mPackageName;
    private int mType;

    ArrayList<ComponentInfo> mComponents = new ArrayList<>();

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ComponentsAdapter mAdapter;

    public static ComponentListFragment newInstance(String packageName, int type) {
        ComponentListFragment fragment = new ComponentListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PACKAGE_NAME, packageName);
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ComponentListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPackageName = getArguments().getString(ARG_PACKAGE_NAME);
            mType = getArguments().getInt(ARG_TYPE);
        }

        PackageManager pm = getActivity().getPackageManager();
        try {
            PackageInfo pInfo = pm.getPackageInfo(mPackageName, PackageManager.GET_ACTIVITIES);
            ComponentInfo[] list;
            switch (mType){
                case TYPE_RECEIVER:
                    list = pInfo.receivers;
                    break;
                case TYPE_SERVICE:
                    list = pInfo.services;
                    break;
                case TYPE_PROVIDER:
                    list = pInfo.providers;
                    break;
                case TYPE_ACTIVITY:
                default:
                    list = pInfo.activities;
                    break;
            }
            if(list != null && list.length > 0)
                mComponents.addAll(Arrays.asList(pInfo.activities));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Comparator<ComponentInfo> compCompr = new Comparator<ComponentInfo>(){

            @Override
            public int compare(ComponentInfo lhs, ComponentInfo rhs) {
                if(lhs.exported && !rhs.exported){
                    return -1;
                }
                if(!lhs.exported && rhs.exported){
                    return 1;
                }
                return 0;
            }
        };
        Collections.sort(mComponents, compCompr);
        mAdapter = new ComponentsAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        setEmptyText("No Activity found");
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ComponentInfo cInfo = mComponents.get(position);
        ComponentName component = new ComponentName(cInfo.packageName, cInfo.name);
        Intent intent = new Intent(getActivity(), ProxyActivity.class);
        intent.putExtra(ProxyActivity.EXTRA_COMPONENT, component);
        intent.putExtra(ProxyActivity.EXTRA_USE_ROOT, !cInfo.exported);
        startActivity(intent);
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    class ComponentsAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mComponents.size();
        }

        @Override
        public ComponentInfo getItem(int position) {
            return mComponents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null, false);
            }
            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            ComponentInfo item = getItem(position);
            textView.setText(item.name);
            textView.setTextColor(item.exported?0xFF000000:0xFFCACACA);
            return convertView;
        }
    }

}
