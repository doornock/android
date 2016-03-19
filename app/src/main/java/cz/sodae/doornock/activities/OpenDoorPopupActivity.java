package cz.sodae.doornock.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cz.sodae.doornock.R;
import cz.sodae.doornock.activities.fragments.SiteDoorListAdapter;
import cz.sodae.doornock.model.site.Door;
import cz.sodae.doornock.model.site.Site;
import cz.sodae.doornock.model.site.SiteApi;
import cz.sodae.doornock.model.site.SiteManager;
import cz.sodae.doornock.utils.InvalidGUIDException;

public class OpenDoorPopupActivity extends ListActivity {

    public static String RESULT_CODE = "result_code";
    public static final String LOG_TAG = "Doornock/OpenDoorPopup";

    SiteManager siteManager;
    SiteDoorListAdapter listDoorAdapter;
    Site site;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String guid = getIntent().getStringExtra("guid");
        if (guid == null) {
            finish();
        }

        siteManager = new SiteManager(this);

        try {
            site = siteManager.getByGuid(guid);
            if (site == null) {
                Log.i(LOG_TAG, "Site " + guid + " not found");
                finish();
                return;
            }
        } catch (InvalidGUIDException e) {
            Log.w(LOG_TAG, "Invalid GUID");
            finish();
            return;
        }


        this.listDoorAdapter = new SiteDoorListAdapter(this, new ArrayList<Door>());

        new SiteDoorListTask(this.listDoorAdapter, this.siteManager, this.site.getGuid()).execute();
        setListAdapter(this.listDoorAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Door door = listDoorAdapter.getItem(position);

                new OpenDoorTask(siteManager, site, door, new OnDoorOpenTaskResult() {
                    @Override
                    public void onResult(boolean success) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(RESULT_CODE, success);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                }).execute();
            }
        });
    }



    private class SiteDoorListTask extends AsyncTask<Object, Float, Boolean> {

        private String guid;
        private SiteManager siteManager;
        private SiteDoorListAdapter adapter;

        Site site;
        List<Door> doors;


        private SiteApi.FindDoorsException findDoorsException;

        public SiteDoorListTask(SiteDoorListAdapter adapter, SiteManager siteManager, String guid) {
            this.adapter = adapter;
            this.siteManager = siteManager;
            this.guid = guid;
        }

        @Override
        protected Boolean doInBackground(Object... obj) {
            try {
                site = siteManager.getByGuid(guid);
                doors = siteManager.findDoor(site);
                return true;
            } catch (InvalidGUIDException e) {
                e.printStackTrace();
                return false;
            } catch (SiteApi.FindDoorsException e) {
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (!ok || doors == null) {
                Toast.makeText(OpenDoorPopupActivity.this, "Error: " + (findDoorsException != null ? findDoorsException.getMessage() : " unknown"), Toast.LENGTH_LONG).show();
                adapter.clear();
                return;
            }
            adapter.clear();
            adapter.addAll(doors);

        }
    }



    private class OpenDoorTask extends AsyncTask<Object, Float, Boolean> {

        private SiteManager siteManager;
        private Site site;
        private Door door;
        private OnDoorOpenTaskResult result;

        public OpenDoorTask(SiteManager siteManager, Site site, Door door, OnDoorOpenTaskResult result) {
            this.siteManager = siteManager;
            this.site = site;
            this.door = door;
            this.result = result;
        }

        @Override
        protected Boolean doInBackground(Object... obj) {
            try {
                siteManager.openDoor(site, door);
                return true;
            } catch (SiteApi.OpenDoorException e) {
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean ok) {
            result.onResult(ok);
        }
    }


    interface OnDoorOpenTaskResult {
        void onResult(boolean success);
    }


}
