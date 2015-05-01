package com.sloths.speedy.shortsounds;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
* This Adapter populates our list of available Effects in the Track Effects view.
 * TODO: Update it to use actual track data
 * Created by shampson on 4/27/15.
 */
public class EffectsListAdapter extends BaseAdapter {

    private List<ShortSoundTrackEffect> effects;
    private Context context;

    // After implementing tracks from database, this constructor
    // could take an input of the actual effects
    public EffectsListAdapter(Context context, List<ShortSoundTrackEffect> effects) {
        this.context = context;
        this.effects = effects;
    }


    // This puts the effect name & toggle for populating the effect list
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;

        if (rowView == null) {

            rowView = ((Activity) context).getLayoutInflater()
                    .inflate(R.layout.effects_list_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) rowView.findViewById(R.id.effectName);
            holder.toggle = (Switch) rowView.findViewById(R.id.effectSwitch);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        ShortSoundTrackEffect effect = effects.get(position);
        holder.title.setText(effect.getTitleString());
        // Do something with setting views toggle?
        return rowView;
    }

    @Override
    public int getCount() {
        return effects.size();
    }

    @Override
    public Object getItem(int position) {
        return effects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        // Holds effect toggle & name that should go in this list view
        public TextView title;
        public Switch toggle;
    }
}
