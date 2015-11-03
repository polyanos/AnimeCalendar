package com.example.gregor.animecalender.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.example.gregor.animecalender.Adapter.AnimeListAdapter;
import com.example.gregor.animecalender.Domain.Anime;
import com.example.gregor.animecalender.R;
import com.example.gregor.animecalender.Utility.AnimeTitleHandler;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class AnimeListActivity extends AppCompatActivity {

    private final String TAG = "AnimeListActivity";
    private AnimeListAdapter animeListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_list);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.anime_calender_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.anime_list_activity_title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }


        animeListAdapter = new AnimeListAdapter(this);
        ((ListView) findViewById(R.id.anime_list_list)).setAdapter(animeListAdapter);

        new LoadAnimeListTask(this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_anime_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void searchAnime(MenuItem item) {
    }

    public class LoadAnimeListTask extends AsyncTask<Void, Void, List<Anime>> {
        ProgressDialog mdialog;
        Context context;

        public LoadAnimeListTask(Context context) {
            this.context = context;
        }

        /**
         * Runs on the UI thread before {@link #doInBackground}.
         *
         * @see #onPostExecute
         * @see #doInBackground
         */
        @Override
        protected void onPreExecute() {
            mdialog = new ProgressDialog(context);
            mdialog.setMessage("Loading titles...");
            mdialog.show();
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected List<Anime> doInBackground(Void... params) {
            List<Anime> animeList = new ArrayList<>();
            try {
                AnimeTitleHandler xmlParserHandler = new AnimeTitleHandler();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                parser.parse(getAssets().open("anime-titles.xml"), xmlParserHandler);

                animeList = xmlParserHandler.getRetrievedAnime();
            } catch (ParserConfigurationException | SAXException ex) {
                Log.e(TAG, "There was an error while loading the parser. The exact error message was: " + ex.getMessage());
            } catch (IOException ex) {
                Log.e(TAG, "The xml file was not found or could not be opened.");
            }

            return animeList;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param animeList The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(List<Anime> animeList) {
            animeListAdapter.addAllAnime(animeList);
            mdialog.dismiss();
        }
    }
}
