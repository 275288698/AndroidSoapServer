package edu.agh.wsserver.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import edu.agh.wsserver.activity.R;
import edu.agh.wsserver.activity.fragment.LoggerActivityFragment;
import edu.agh.wsserver.activity.fragment.ParamsActivityFragment;
import edu.agh.wsserver.activity.fragment.SecurityActivityFragment;
import edu.agh.wsserver.activity.fragment.ServerActivityFragment;
import edu.agh.wsserver.activity.fragment.SettingsActivityFragment;
import edu.agh.wsserver.data.NavDrawerItem;
import edu.agh.wsserver.data.NavDrawerListAdapter;
import edu.agh.wsserver.utils.ServerUtils;

public class MainActivity extends FragmentActivity {
	public static final String LOG_TAG = "MainActivity";
	
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
 
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
 
    private List<Fragment> fragmentsList;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
 
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // czyscimy, po co nam stary syf
        ServerUtils.clearLogCat();

        Log.d(LOG_TAG, this.getClass().getSimpleName() + " init.");

        setContentView(R.layout.activity_main);
 
        mTitle = mDrawerTitle = getTitle();
 
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);
 
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
 
        fragmentsList = new ArrayList<Fragment>();
        fragmentsList.add(new ServerActivityFragment());
        fragmentsList.add(new ParamsActivityFragment());
        fragmentsList.add(new SettingsActivityFragment());
        fragmentsList.add(new SecurityActivityFragment());
        fragmentsList.add(new LoggerActivityFragment());
        
        navDrawerItems = new ArrayList<NavDrawerItem>();
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));

        navMenuIcons.recycle();
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);
 
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
 
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.app_name, R.string.app_name){
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }
 
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
 
        if (savedInstanceState == null) {
        	/* pokazujemy od razu tab server */
            displayView(0);
        }
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
    }
    
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            displayView(position);
        }
    }
 
    /**
     * Pokazuje odpowiednia zakladke
	 * FIXME - chyba juz dziala dobrze 
     */
    private void displayView(int fragmentIndex) {
        Fragment fragment = fragmentsList.get(fragmentIndex);
 
       /* if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();
 
            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(fragmentIndex, true);
            mDrawerList.setSelection(fragmentIndex);
            setTitle(navMenuTitles[fragmentIndex]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }*/
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if(fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.addToBackStack(fragmentIndex + "stack_item");
            transaction.replace(R.id.frame_container, fragment);
        }
        transaction.commit();
        
        mDrawerList.setItemChecked(fragmentIndex, true);
        mDrawerList.setSelection(fragmentIndex);
        setTitle(navMenuTitles[fragmentIndex]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        
        switch (item.getItemId()) {
        case R.id.action_settings:
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
 
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
 
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
 
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}