package cz.sodae.doornock.activities.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cz.sodae.doornock.R;
import cz.sodae.doornock.model.site.Site;
import cz.sodae.doornock.model.site.SiteManager;

public class SiteRecyclerViewAdapter extends RecyclerView.Adapter<SiteRecyclerViewAdapter.ViewHolder> {

    private final SiteListFragment.OnListFragmentInteractionListener mListener;

    private List<Site> sites;
    private SiteManager siteManager;

    public SiteRecyclerViewAdapter(SiteManager siteManager, SiteListFragment.OnListFragmentInteractionListener listener) {
        mListener = listener;
        this.siteManager = siteManager;
        refresh();

    }


    public void refresh() {
        sites = siteManager.findAll();
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_site, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setSite(sites.get(position));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return sites.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Site mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        public void setSite(Site site) {
            mItem = site;
            mIdView.setText(site.getTitle() != null ? site.getTitle() : "(Neznámá)");
            mContentView.setText("Adresa sítě: " + site.getUrl());
        }


        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
