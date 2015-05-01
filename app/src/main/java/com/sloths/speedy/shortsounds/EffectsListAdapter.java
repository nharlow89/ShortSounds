package com.sloths.speedy.shortsounds;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
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
    private LayoutInflater mInflater;
    private Context context;
    private static final String TAG = "EffectsListAdapter";


    // After implementing tracks from database, this constructor
    // could take an input of the actual effects
    public EffectsListAdapter(Context context, List<ShortSoundTrackEffect> effects) {
        mInflater = LayoutInflater.from(context);
        this.effects = effects;
        this.context = context;
        Log.d(TAG, "effects length is " + effects.size());
    }


    // This puts the effect name & toggle for populating the effect list
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.effects_list_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.effectName);
            holder.toggle = (Switch) view.findViewById(R.id.effectSwitch);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        ShortSoundTrackEffect effect = (ShortSoundTrackEffect) getItem(position);
        holder.title.setText(effect.getTitleString());
        view.setOnClickListener(new EffectItemClickListener(parent));
        Log.d(TAG, "returning a view ofr the list at index " + position);
        // Do something with setting views toggle?
        return view;
    }
    /* The click listener for ListView in the navigation drawer */
    private class EffectItemClickListener implements View.OnClickListener {
        private  ViewGroup viewGroup;

        public EffectItemClickListener(ViewGroup viewGroup) {
            this.viewGroup = viewGroup;
        }
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Effect clicked");
            AlertDialog dialog = getEffectAlert(viewGroup);
            dialog.show();
        }
    }

    // Returns an alert specific for one effect
    private AlertDialog getEffectAlert(ViewGroup viewGroup) {

        // TODO: Need to use a different view group (right now uses listview)
        final AlertDialog.Builder effectDialog = new AlertDialog.Builder(context);

        // Get effect view & manipulate it
        View effectView = mInflater.
                inflate(R.layout.track_view, viewGroup, false);
        TextView effectTitle = (TextView) effectView.findViewById(R.id.effectNameTitle);
        // Can set effect title here
        String title = "Effect Title!";
        effectTitle.setText(title);
        // Set view to dialog
        effectDialog.setView(effectView);

        final AlertDialog dialog = effectDialog.create();
        // Can set on click listeners here
        return dialog;
    }

    @Override
    public int getCount() {
        Log.d(TAG, "Returning size of effects of " + effects.size());
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
