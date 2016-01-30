package cz.sodae.doornock.activities.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cz.sodae.doornock.R;
import cz.sodae.doornock.model.site.Door;

public class SiteDoorListAdapter extends ArrayAdapter<Door>
{
    public SiteDoorListAdapter(Context context, List<Door> doors) {
        super(context, R.layout.row_open_door_popup, doors);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Door door = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_open_door_popup, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.door_title);
            viewHolder.description = (TextView) convertView.findViewById(R.id.door_description);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        viewHolder.name.setText(door.getTitle());
        viewHolder.description.setText(door.getId() + " - Přístup: " + (door.isAccess() ? "Ano" : "Ne"));
        // Return the completed view to render on screen
        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView description;
    }

}
