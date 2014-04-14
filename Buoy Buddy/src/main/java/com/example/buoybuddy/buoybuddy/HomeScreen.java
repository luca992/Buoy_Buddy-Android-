package com.example.buoybuddy.buoybuddy;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

//import android.app.FragmentManager;

public class HomeScreen extends ActionBarActivity {
    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_launcher, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position
        Fragment fragment;
        if (position ==0)
            fragment = new PlaceholderFragment("http://www.ndbc.noaa.gov/data/realtime2/46221.spec");
        if (position==1)
            fragment = new PlaceholderFragment("http://www.ndbc.noaa.gov/data/realtime2/44097.spec");
        else
            fragment = new PlaceholderFragment("http://www.ndbc.noaa.gov/data/realtime2/46221.spec");

        Bundle args = new Bundle();
        //args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {
        public GridView gridView;
        public String url;

        public PlaceholderFragment(String _url) {
        url=_url;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            setContentView(R.layout.fragment_home_screen);
            View rootView = inflater.inflate(R.layout.fragment_home_screen, container, false);
            gridView = (GridView) findViewById(R.id.gridview);
            bindGridView(url);
            return rootView;
        }

        public void bindGridView(String url) {
           // DownloadWebPageTask task = new DownloadWebPageTask();
           // task.execute(new String[]{"http://www.google.com"});
            new DownloadHtmlBuoyData(getActivity(), gridView).execute(url);
        }


            private class DownloadHtmlBuoyData extends AsyncTask<String, String, String> {
                GridView mGridView;
                Activity mContext;
                public DownloadHtmlBuoyData(Activity context, GridView gview)
                {
                    this.mGridView=gview;
                    this.mContext=context;
                }
                @Override
                protected String doInBackground(String... urls) {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    String response = "";
                    for (String url : urls) {
                        DefaultHttpClient client = new DefaultHttpClient();
                        HttpGet httpGet = new HttpGet(url);
                        try {
                            HttpResponse execute = client.execute(httpGet);
                            InputStream content = execute.getEntity().getContent();

                            BufferedReader buffer = new BufferedReader(
                                    new InputStreamReader(content));
                            String s = "";
                            int counter=0;
                            while ((s = buffer.readLine()) != null) {
                                response += s;
                                response += "\n";
                                counter++;
                                if (counter==50)break;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return response;
                }

                @Override
                protected void onPostExecute(String result) {
                    String delims = "[ \\r?\\n]+";
                    String[] values=result.split(delims);

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.mylist, values);

                    mGridView.setAdapter(adapter);
                }
            }



    }
}