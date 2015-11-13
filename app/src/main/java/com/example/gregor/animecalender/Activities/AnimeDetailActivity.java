package com.example.gregor.animecalender.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.gregor.animecalender.Adapter.AnimeCharacterListAdapter;
import com.example.gregor.animecalender.Domain.Anime;
import com.example.gregor.animecalender.Domain.ImageToLoad;
import com.example.gregor.animecalender.Exceptions.AuthorizeException;
import com.example.gregor.animecalender.R;
import com.example.gregor.animecalender.Utility.AniDBApi;
import com.example.gregor.animecalender.Utility.AnilistApi;
import com.example.gregor.animecalender.Utility.Interface.Api;
import com.example.gregor.animecalender.Utility.FileCache;
import com.example.gregor.animecalender.Utility.ImageLoader;
import com.example.gregor.animecalender.View.ExpandableHeightListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AnimeDetailActivity extends AppCompatActivity {
    private static final String TAG = "AnimeDetailActivity";
    private ArrayAdapter<String> gerneListAdapter;
    private AnimeCharacterListAdapter animeCharacterListAdapter;
    private Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_anime_detail);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.anime_calender_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.anime_detail_activity_title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    public void onResume(){
        super.onResume();
        Intent recievedIntent = this.getIntent();
        int animeId = recievedIntent.getIntExtra("ANIME_ID", 0);
        String animeApi = recievedIntent.getStringExtra("ANIME_API");

        switch (animeApi) {
            case AnilistApi.NAME:
                api = new AnilistApi(this.getApplicationContext());
                break;
            case AniDBApi.NAME:
                api = new AniDBApi(this.getApplicationContext());
                break;
        }

        gerneListAdapter = new ArrayAdapter<>(this, R.layout.anime_gerne);
        animeCharacterListAdapter = new AnimeCharacterListAdapter(this, api);
        ((ListView) findViewById(R.id.anime_detail_gerne_list)).setAdapter(gerneListAdapter);
        ((ListView) findViewById(R.id.anime_detail_character_list)).setAdapter(animeCharacterListAdapter);

        Log.d(TAG, "Creating detail page for id: " + animeId + ". Api being used is the " + animeApi + " api");
        new GetAnimeData(this, String.valueOf(animeId), animeApi).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_anime_detail, menu);
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

    class GetAnimeData extends AsyncTask<Void, Void, Object[]> {
        String animeId;
        String animeApi;
        ProgressDialog mDialog;
        Activity context;

        GetAnimeData(Activity context, String animeId, String animeApi) {
            this.animeId = animeId;
            this.animeApi = animeApi;
            this.context = context;
        }

        @Override
        public void onPreExecute() {
            mDialog = new ProgressDialog(context);
            mDialog.setMessage("Please wait...");
            mDialog.show();
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
        protected Object[] doInBackground(Void... params) {
            ImageLoader imageLoader = new ImageLoader(context);
            Object[] objects = new Object[2];

            try {
                api.authorizeApi();
            } catch (AuthorizeException ex) {
                Log.e(TAG, "Authorization failure");
            }

            Anime anime = api.getFullAnimeData(animeId);
            Bitmap image = imageLoader.getImage(new ImageToLoad(String.valueOf(anime.getId()), new FileCache(null).getStandardAnimeImageDirectory(), anime.getImageUrl(), true), api);
            objects[0] = anime;
            objects[1] = image;
            return objects;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param objects The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(Object... objects) {
            Anime anime = (Anime) objects[0];
            Bitmap image = (Bitmap) objects[1];
            DateFormat dateFormat = SimpleDateFormat.getDateInstance();
            if (anime.getGerneArray() != null) {
                TextView gerneTitleView = (TextView) findViewById(R.id.anime_detail_gerne_list_title);
                switch (context.getIntent().getStringExtra("ANIME_API")) {
                    case AniDBApi.NAME:
                        gerneTitleView.setText(R.string.anime_detail_tag_title);
                        break;
                    case AnilistApi.NAME:
                        gerneTitleView.setText(R.string.anime_detail_genre_title);
                        break;
                }

                gerneListAdapter.addAll(anime.getGerneArray());
            }
            if (anime.getAnimeCharacters() != null) {
                animeCharacterListAdapter.addCharacters(anime.getAnimeCharacters());
            }

            ((ImageView) findViewById(R.id.anime_detail_image)).setImageDrawable(new BitmapDrawable(context.getResources(), image));
            ((TextView) findViewById(R.id.anime_detail_anime_title)).setText(anime.getRomanjiTitle());
            ((TextView) findViewById(R.id.anime_detail_total_episodes)).setText(String.valueOf(anime.getEpisodeTotal()) + " episodes");
            ((TextView) findViewById(R.id.anime_detail_anime_start_date)).setText(dateFormat.format(new Date(anime.getStartDate())));
            if (anime.getDescription().isEmpty()) {
                findViewById(R.id.anime_detail_anime_description_title).setVisibility(View.GONE);
                findViewById(R.id.anime_detail_anime_description).setVisibility(View.GONE);
            } else {
                ((TextView) findViewById(R.id.anime_detail_anime_description)).setText(anime.getDescription());
            }
            findViewById(R.id.anime_detail_anime_japanese_title).setVisibility(View.GONE);
            ((ExpandableHeightListView) findViewById(R.id.anime_detail_gerne_list)).setExpanded(true);
            if(anime.getAnimeCharacters() == null || anime.getAnimeCharacters().isEmpty()){
                findViewById(R.id.anime_detail_character_list_title).setVisibility(View.GONE);
                findViewById(R.id.anime_detail_character_list).setVisibility(View.GONE);
            } else {
                ((ExpandableHeightListView) findViewById(R.id.anime_detail_character_list)).setExpanded(true);
            }

            mDialog.dismiss();
        }
    }

}
