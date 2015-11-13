package com.example.gregor.animecalender.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gregor.animecalender.Domain.AnimeCharacter;
import com.example.gregor.animecalender.Domain.ImageToLoad;
import com.example.gregor.animecalender.R;
import com.example.gregor.animecalender.Utility.Interface.Api;
import com.example.gregor.animecalender.Domain.Dimension;
import com.example.gregor.animecalender.Utility.FileCache;
import com.example.gregor.animecalender.Utility.ImageLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Gregor on 12-11-2015.
 */
public class AnimeCharacterListAdapter extends BaseAdapter {
    List<AnimeCharacter> characterList;
    Context context;
    Api api;

    public AnimeCharacterListAdapter(Context context, Api api) {
        this.api = api;
        this.context = context;
        characterList = new ArrayList<>();
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return characterList.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public AnimeCharacter getItem(int position) {
        return characterList.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
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
        AnimeCharacter character = characterList.get(position);
        ImageLoader imageLoader = new ImageLoader(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View characterView = inflater.inflate(R.layout.anime_character_item, parent, false);
        ImageView imageView = (ImageView) characterView.findViewById(R.id.anime_detail_character_item_image);

        int imageWidth = (int)context.getResources().getDimension(R.dimen.character_image_size_width);
        int imageHeight = (int)context.getResources().getDimension(R.dimen.character_image_size_height);
        Dimension dimension = new Dimension(imageWidth, imageHeight);
        ImageToLoad imageToLoad = new ImageToLoad(String.valueOf(character.getId()), new FileCache(null).getStandardCharacterImageDirectory(), character.getUrl(), true, true, dimension);

        ((TextView) characterView.findViewById(R.id.anime_detail_character_item_name)).setText(character.getName());
        imageLoader.ShowImage(imageView, imageToLoad, api);
        return characterView;
    }

    public void addCharacter(AnimeCharacter character) {
        characterList.add(character);
        notifyDataSetChanged();
    }

    public void addCharacters(Collection<AnimeCharacter> characterCollection) {
        characterList.addAll(characterCollection);
        notifyDataSetChanged();
    }
}
