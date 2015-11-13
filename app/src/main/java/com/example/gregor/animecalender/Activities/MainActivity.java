package com.example.gregor.animecalender.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gregor.animecalender.Adapter.AnimeSeasonListAdapter;
import com.example.gregor.animecalender.Domain.Anime;
import com.example.gregor.animecalender.Exceptions.AuthorizeException;
import com.example.gregor.animecalender.R;
import com.example.gregor.animecalender.Utility.AnilistApi;
import com.example.gregor.animecalender.Utility.DrawerUtil;
import com.example.gregor.animecalender.Utility.FileCache;
import com.example.gregor.animecalender.Utility.SeasonUtil;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static final String SETTING_FILE = "SETTINGS";
    private static final String TAG = "MAIN_ACTIVITY";
    private AnimeSeasonListAdapter listAdapter;
    private AnilistApi anilistApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isNetworkConnected()) {
            Toast toast = Toast.makeText(this, "No internet connection, exiting app", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }

        SharedPreferences preferences = getSharedPreferences(SETTING_FILE, MODE_PRIVATE);
        if (preferences.getBoolean("FIRST_RUN", true)) {
            FileCache fileCache = new FileCache(this.getApplicationContext());
            fileCache.createStandardDirectories();

            preferences.edit().putBoolean("FIRST_RUN", false).apply();
        }

        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.anime_calender_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.anime_main_activity_title);

        DrawerUtil.setNavigationDrawerAdapter(this.getClass(), (ListView) findViewById(R.id.left_drawer));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        listAdapter = new AnimeSeasonListAdapter(this);
        ListView listView = (ListView) findViewById(R.id.anime_season_list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AnimeListClickListener());

        fillSpinners();

        loadSeasonAnime(null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_anime_list_search:
                View view = findViewById(R.id.anime_season_search_layout);
                if(view.getVisibility() == View.VISIBLE){
                    view.setVisibility(View.GONE);
                }else{
                    view.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.action_main_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method for when the search button has been clicked.
     * @param view
     */
    public void onSearchSeaonAnimeClick(View view) {
        Spinner seasonSpinner = (Spinner) findViewById(R.id.anime_season_search_season_spinner);
        Spinner yearSpinner = (Spinner) findViewById(R.id.anime_season_search_year_spinner);
        String year = (String) yearSpinner.getSelectedItem();
        String season = (String) seasonSpinner.getSelectedItem();

        loadSeasonAnime(year, season);
    }

    private void loadSeasonAnime(String year, String season) {
        if (year == null || season == null) {
            SeasonUtil seasonUtil = new SeasonUtil();
            Calendar calendar = Calendar.getInstance();
            year = String.valueOf(calendar.get(Calendar.YEAR));
            season = String.valueOf(seasonUtil.getSeasonString(calendar.get(Calendar.MONTH)));
        }
        anilistApi = new AnilistApi(this.getApplicationContext());

        Runnable task = new GetSeasonAnimeTask(this, year, season);

        Executors.newSingleThreadExecutor().execute(task);
    }

    /**
     * Checks if the device has a active network connection.
     *
     * @return truw if it has a connection of false if it hasn't.
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    /**
     * Fills the spinners with the data where the user can choose from.
     */
    private void fillSpinners() {
        SeasonUtil util = new SeasonUtil();
        Spinner seasonSpinner = (Spinner) findViewById(R.id.anime_season_search_season_spinner);
        Spinner yearSpinner = (Spinner) findViewById(R.id.anime_season_search_year_spinner);

        yearSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, util.getYears()));
        seasonSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.anime_season_search_seasons)));
    }

    private class GetSeasonAnimeTask implements Runnable {
        String year, season;
        Context context;
        List<Anime> animeList;

        public GetSeasonAnimeTask(Context context, String year, String season) {
            this.year = year;
            this.season = season;
            this.context = context;
        }

        /**
         * Starts executing the active part of the class' code. This method is
         * called when a thread is started that has been created with a class which
         * implements {@code Runnable}.
         */
        @Override
        public void run() {
            try {
                anilistApi.authorizeApi();
            } catch (AuthorizeException e) {
                e.printStackTrace();
            }
            animeList = anilistApi.getSeasonAnime(season, year, "Tv");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(animeList.isEmpty()){
                        Toast toast = Toast.makeText(context, R.string.anime_season_search_no_found, Toast.LENGTH_LONG);
                        toast.show();
                    } else {
                        if(!listAdapter.isEmpty()){
                            ((TextView)findViewById(R.id.anime_season_list_title)).setText(R.string.anime_season_search_found);
                        }
                        listAdapter.clear();
                        listAdapter.addAnimesToList(animeList);
                        listAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private class AnimeListClickListener implements ListView.OnItemClickListener {
        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         * <p/>
         * Implementers can call getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * @param parent   The AdapterView where the click happened.
         * @param view     The view within the AdapterView that was clicked (this
         *                 will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id       The row id of the item that was clicked.
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Anime anime = listAdapter.getItem(position);
            Log.i(TAG, "User clicked anime with id: " + anime.getId());
            Intent intent = new Intent(getApplicationContext(), AnimeDetailActivity.class);
            intent.putExtra("ANIME_ID", anime.getId());
            intent.putExtra("ANIME_API", anilistApi.NAME);
            startActivity(intent);
        }
    }
}
