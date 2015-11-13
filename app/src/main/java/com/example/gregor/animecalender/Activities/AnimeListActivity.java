package com.example.gregor.animecalender.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.gregor.animecalender.Adapter.AnimeListAdapter;
import com.example.gregor.animecalender.Adapter.NavigationDrawerListAdapter;
import com.example.gregor.animecalender.Domain.Anime;
import com.example.gregor.animecalender.R;
import com.example.gregor.animecalender.Utility.AniDBApi;
import com.example.gregor.animecalender.Utility.DrawerUtil;

import java.util.List;

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
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        DrawerUtil.setNavigationDrawerAdapter(this.getClass(), (ListView) findViewById(R.id.left_drawer));

        animeListAdapter = new AnimeListAdapter(this);
        ((ListView) findViewById(R.id.anime_list_list)).setAdapter(animeListAdapter);
        ((ListView) findViewById(R.id.anime_list_list)).setOnItemClickListener(new AnimeListClickListener());
        ((EditText) findViewById(R.id.anime_list_search_textbox)).addTextChangedListener(new SerachListener());

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
        switch (id) {
            case R.id.action_anime_list_search:
                View searchTextBoxView = findViewById(R.id.anime_list_search_textbox);
                InputMethodManager manager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (searchTextBoxView.getVisibility() == View.VISIBLE) {
                    manager.hideSoftInputFromWindow(searchTextBoxView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    searchTextBoxView.setVisibility(View.GONE);
                    searchTextBoxView.clearFocus();
                    findViewById(R.id.anime_list_list).requestFocus();
                } else {
                    searchTextBoxView.setVisibility(View.VISIBLE);
                    searchTextBoxView.requestFocus();
                    manager.showSoftInput(searchTextBoxView, InputMethodManager.SHOW_IMPLICIT);
                }
        }
        return false;
    }

    public void loadDetailPage(int id) {
        Intent intent = new Intent(this.getApplicationContext(), AnimeDetailActivity.class);
        intent.putExtra("ANIME_ID", id);
        intent.putExtra("ANIME_API", AniDBApi.NAME);

        startActivity(intent);
    }

    private void searchAnime(String searchString) {
        animeListAdapter.searchAnime(searchString);
        TextView listTitle = (TextView) findViewById(R.id.anime_list_list_title);
        if (searchString.isEmpty()) {
            listTitle.setText(R.string.anime_list_title);
        } else if (animeListAdapter.getCount() > 0) {
            listTitle.setText(R.string.anime_list_title_found);
        } else {
            listTitle.setText(R.string.anime_list_title_not_found);
        }
    }

    public class AnimeListClickListener implements ListView.OnItemClickListener {
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
            int animeId = (int) id;
            loadDetailPage(animeId);
        }
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
            AniDBApi aniDBApi = new AniDBApi(context.getApplicationContext());
            return aniDBApi.loadAnimeTitles();
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

    private class SerachListener implements TextWatcher {
        /**
         * This method is called to notify you that, within <code>s</code>,
         * the <code>count</code> characters beginning at <code>start</code>
         * are about to be replaced by new text with length <code>after</code>.
         * It is an error to attempt to make changes to <code>s</code> from
         * this callback.
         *
         * @param s
         * @param start
         * @param count
         * @param after
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        /**
         * This method is called to notify you that, within <code>s</code>,
         * the <code>count</code> characters beginning at <code>start</code>
         * have just replaced old text that had length <code>before</code>.
         * It is an error to attempt to make changes to <code>s</code> from
         * this callback.
         *
         * @param s
         * @param start
         * @param before
         * @param count
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            searchAnime(String.valueOf(s));

        }

        /**
         * This method is called to notify you that, somewhere within
         * <code>s</code>, the text has been changed.
         * It is legitimate to make further changes to <code>s</code> from
         * this callback, but be careful not to get yourself into an infinite
         * loop, because any changes you make will cause this method to be
         * called again recursively.
         * (You are not told where the change took place because other
         * afterTextChanged() methods may already have made other changes
         * and invalidated the offsets.  But if you need to know here,
         * you can use in {@link #onTextChanged}
         * to mark your place and then look up from here where the span
         * ended up.
         *
         * @param s
         */
        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
