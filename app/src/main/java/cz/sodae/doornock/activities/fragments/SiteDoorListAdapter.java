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

public class SiteDoorListAdapter extends ArrayAdapter<Door> {
    public SiteDoorListAdapter(Context context, List<Door> doors) {
        super(context, R.layout.row_open_door_popup, doors);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Door door = getItem(position);

        ViewHolder viewHolder;
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

        viewHolder.name.setText(door.getTitle());
        viewHolder.description.setText(String.format(getContext().getString(
                door.hasAccess()
                        ? R.string.activity_open_door_popup_list_has_access
                        : R.string.activity_open_door_popup_list_has_not_access
        ), door.getId()));

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView description;
    }

}
