package com.example.gregor.animecalender.Utility;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.widget.ListView;

import com.example.gregor.animecalender.Activities.AnimeListActivity;
import com.example.gregor.animecalender.Activities.MainActivity;
import com.example.gregor.animecalender.Adapter.NavigationDrawerListAdapter;
import com.example.gregor.animecalender.Domain.NavigationItem;
import com.example.gregor.animecalender.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gregor on 13-11-2015.
 */
public class DrawerUtil {
    /**
     * This method will load and fill the navigation drawer with the contents specified here. After the drawer has been filled the method will bind a listener to it.
     * @param activeActivity The activity currently activated.
     * @param listView The empty navigation drawer.
     * @return The navigation drawer filled with the necessary data.
     */
    public static ListView setNavigationDrawerAdapter(Class activeActivity, ListView listView) {
        List<NavigationItem> navigationItemList = new ArrayList<>();
        Context context = listView.getContext();
        Resources resources = context.getResources();
        navigationItemList.add(new NavigationItem(resources.getString(R.string.anime_list_activity_title), ContextCompat.getDrawable(context, R.drawable.ic_action_list), AnimeListActivity.class));
        navigationItemList.add(new NavigationItem(resources.getString(R.string.anime_main_activity_title), ContextCompat.getDrawable(context, R.drawable.ic_action_calander), MainActivity.class));

        NavigationDrawerListAdapter adapter = new NavigationDrawerListAdapter(context, navigationItemList);
        adapter.setActiveNavigationItem(activeActivity);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter.getClickListener(context));

        return listView;
    }
}
