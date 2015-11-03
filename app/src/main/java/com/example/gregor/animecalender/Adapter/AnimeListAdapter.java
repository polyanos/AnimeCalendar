package com.example.gregor.animecalender.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.gregor.animecalender.Domain.Anime;
import com.example.gregor.animecalender.R;
import com.example.gregor.animecalender.Utility.AnimeTitleComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Gregor on 3-11-2015.
 */
public class AnimeListAdapter extends BaseAdapter {
    private List<Anime> originalList;
    private List<Anime> foundItemList;
    private List<Anime> orderedItemList;
    private Context context;

    public AnimeListAdapter(Context context){
        this(context, new ArrayList<Anime>());
    }

    public AnimeListAdapter(Context context, List<Anime> animeList){
        this.context = context;
        originalList = animeList;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return originalList.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Anime getItem(int position) {
        return originalList.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return (long) getItem(position).getId();
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
        View view;
        if(convertView !=null){
            view = convertView;
        }else {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.anime_list_listitem, parent, false);
        }

        ((TextView)view.findViewById(R.id.anime_list_list_text)).setText(getItem(position).getRomanjiTitle());

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void addAnime(Anime anime){
        originalList.add(anime);
        sortList();
    }

    public void addAllAnime(Collection<Anime> animeCollection){
        originalList.addAll(animeCollection);
        sortList();
    }

    private void sortList(){
        Collections.sort(originalList, new AnimeTitleComparator());
        notifyDataSetChanged();
    }
}
