package com.example.gregor.animecalender.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gregor.animecalender.Utility.AnilistApi;
import com.example.gregor.animecalender.Utility.ImageLoader;
import com.example.gregor.animecalender.Domain.Anime;
import com.example.gregor.animecalender.Domain.ImageToLoad;
import com.example.gregor.animecalender.R;
import com.example.gregor.animecalender.View.ExpandableHeightListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AnimeDetailActivity extends AppCompatActivity {

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

        new GetAnimeData(this, String.valueOf(this.getIntent().getIntExtra("ANIME_ID", 0))).execute();
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

    class GetAnimeData extends AsyncTask <Void, Void, Object[]>{
        String animeId;
        String animeJapTitle;
        ProgressDialog mDialog;
        Context context;

        GetAnimeData(Context context, String animeId) {
            this.animeId = animeId;
            this.context = context;
        }

        @Override
        public void onPreExecute(){
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
            AnilistApi anilistApi = new AnilistApi();
            anilistApi.getAccessCode();

            Anime anime = anilistApi.getFullAnimeData(animeId);
            Bitmap image = imageLoader.getImage(new ImageToLoad(anime.getImageFileName(), anime.getImageUrl(), true));
            objects[0] = anime;
            objects[1] = image;
            return objects;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         *
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param objects The result of the operation computed by {@link #doInBackground}.
         *
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(Object... objects){
            Anime anime = (Anime)objects[0];
            Bitmap image = (Bitmap)objects[1];
            DateFormat dateFormat = SimpleDateFormat.getDateInstance();
            ArrayAdapter<String> gerneListAdapter = new ArrayAdapter<String>(context, R.layout.anime_gerne);
            gerneListAdapter.addAll(anime.getGerneArray());
            ((ImageView) findViewById(R.id.anime_detail_image)).setImageDrawable(new BitmapDrawable(context.getResources(),image));
            ((TextView)findViewById(R.id.anime_detail_anime_title)).setText(anime.getRomanjiTitle());
            ((TextView)findViewById(R.id.anime_detail_total_episodes)).setText(String.valueOf(anime.getEpisodeTotal()));
            ((TextView)findViewById(R.id.anime_detail_anime_start_date)).setText(dateFormat.format(new Date(anime.getStartDate())));
            ((TextView)findViewById(R.id.anime_detail_anime_description)).setText(anime.getDescription());
            ((TextView)findViewById(R.id.anime_detail_anime_japanese_title)).setText(anime.getJapaneseTitle());
            ExpandableHeightListView gerneView = (ExpandableHeightListView)findViewById(R.id.anime_detail_gerne_list);
            gerneView.setAdapter(gerneListAdapter);
            gerneView.setExpanded(true);
            mDialog.dismiss();
        }
    }

}
