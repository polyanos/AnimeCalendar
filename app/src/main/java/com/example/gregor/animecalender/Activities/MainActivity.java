package com.example.gregor.animecalender.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.gregor.animecalender.Adapter.AnimeSeasonListAdapter;
import com.example.gregor.animecalender.Utility.AnilistApi;
import com.example.gregor.animecalender.Domain.Anime;
import com.example.gregor.animecalender.R;

import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private AnimeSeasonListAdapter listAdapter;
    private AnilistApi anilistApi;

    private final String TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.anime_calender_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.anime_main_activity_title);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        listAdapter = new AnimeSeasonListAdapter(this);
        ListView listView = (ListView) findViewById(R.id.anime_season_list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AnimeListClickListener());

        loadSeasonAnime();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_main_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadSeasonAnime() {
        anilistApi = new AnilistApi();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                List<Anime> animeList;
                anilistApi.getAccessCode();
                animeList = anilistApi.getSeasonAnime("Fall", "2015", "Tv");
                listAdapter.addAnimesToList(animeList);
                refresAnimeList();
            }
        });


    }

    private void refresAnimeList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    public void loadCalander(MenuItem item) {
    }

    public void loadList(MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), AnimeListActivity.class);
        startActivity(intent);
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
            startActivity(intent);
        }
    }

}
