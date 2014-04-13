package com.example.buoybuddy.buoybuddy;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HomeScreen extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            setContentView(R.layout.fragment_home_screen);
            View rootView = inflater.inflate(R.layout.fragment_home_screen, container, false);
            gridView = (GridView) findViewById(R.id.gridview);
            bindGridView();
            return rootView;
        }

        public void bindGridView() {
           // DownloadWebPageTask task = new DownloadWebPageTask();
           // task.execute(new String[]{"http://www.google.com"});
            new DownloadHtmlBuoyData(getActivity(), gridView).execute(new String[]{"http://www.ndbc.noaa.gov/data/realtime2/46221.spec"});
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