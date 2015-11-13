package com.example.gregor.animecalender.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.gregor.animecalender.Domain.NavigationItem;
import com.example.gregor.animecalender.R;

import java.util.List;

/**
 * Created by Gregor on 13-11-2015.
 */
public class NavigationDrawerListAdapter extends BaseAdapter {
    Context context;
    List<NavigationItem> navigationItemList;
    int currentActicityPosition;

    public NavigationDrawerListAdapter(@NonNull Context context, @NonNull List<NavigationItem> navigationItemList) {
        this.context = context;
        this.navigationItemList = navigationItemList;
        currentActicityPosition = -1;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return navigationItemList.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public NavigationItem getItem(int position) {
        return navigationItemList.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.navigation_drawer_item, parent, false);
        }

        if(currentActicityPosition > -1 && currentActicityPosition == position){
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarker));
        }
        NavigationItem navigationItem = navigationItemList.get(position);
        ((ImageView) view.findViewById(R.id.navigation_drawer_icon)).setImageDrawable(navigationItem.getIcon());
        ((TextView) view.findViewById(R.id.navigation_drawer_title)).setText(navigationItem.getTitle());
        return view;
    }

    public void setActiveNavigationItem(Class activeActivity){
        if(activeActivity == null) return;

        for(int x = 0; x < navigationItemList.size(); x++){
            if(navigationItemList.get(x).getActivity().equals(activeActivity)) currentActicityPosition = x;
        }
    }

    public ListView.OnItemClickListener getClickListener(Context context){
        return new OnNavigationClickListener(this, context);
    }

    private class OnNavigationClickListener implements ListView.OnItemClickListener{
        NavigationDrawerListAdapter listAdapter;
        Context context;

        public OnNavigationClickListener(NavigationDrawerListAdapter listAdapter, Context context) {
            this.listAdapter = listAdapter;
            this.context = context;
        }

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
            NavigationItem navigationItem = listAdapter.getItem(position);
            Intent intent = new Intent(context, navigationItem.getActivity());
            context.startActivity(intent);
        }
    }
}
