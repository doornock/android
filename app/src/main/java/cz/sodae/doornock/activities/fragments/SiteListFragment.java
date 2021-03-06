package cz.sodae.doornock.activities.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import cz.sodae.doornock.R;
import cz.sodae.doornock.activities.AddSiteActivity;
import cz.sodae.doornock.model.site.Site;
import cz.sodae.doornock.model.site.SiteManager;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SiteListFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SiteListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view;
        view = inflater.inflate(R.layout.fragment_site_list, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AddSiteActivity.class));

            }
        });
        fab.setImageDrawable(new IconicsDrawable(getContext(), GoogleMaterial.Icon.gmd_add).color(Color.WHITE).actionBar());

        // Set the adapter
        if (view.findViewById(R.id.list) instanceof RecyclerView) {
            Context context = view.findViewById(R.id.list).getContext();
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new SiteRecyclerViewAdapter(new SiteManager(getContext()), mListener));
        }

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        RecyclerView r = ((RecyclerView) getView().findViewById(R.id.list));
        ((SiteRecyclerViewAdapter) r.getAdapter()).refresh();
        boolean b = r.getAdapter().getItemCount() == 0;
        getView().findViewById(R.id.empty).setVisibility(b ? View.VISIBLE : View.GONE);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Site item);
    }
}
