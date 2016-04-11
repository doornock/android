package cz.sodae.doornock.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

    SiteApi siteApi;
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
        siteApi = new SiteApi();

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

        new SiteDoorListTask(this.listDoorAdapter, this.siteManager, this.siteApi, this.site.getGuid()).execute();

        setListAdapter(this.listDoorAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Door door = listDoorAdapter.getItem(position);

                new OpenDoorTask(siteApi, site, door, new OnDoorOpenTaskResult() {
                    @Override
                    public void onResult(boolean success) {
                        if (success) {
                            Toast.makeText(
                                    OpenDoorPopupActivity.this,
                                    getString(R.string.activity_open_door_popup_door_opened),
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(RESULT_CODE, success);
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        } else {
                            Toast.makeText(
                                    OpenDoorPopupActivity.this,
                                    getString(R.string.activity_open_door_popup_door_error),
                                    Toast.LENGTH_SHORT
                            ).show();

                        }
                    }
                }).execute();
            }
        });
    }



    private class SiteDoorListTask extends AsyncTask<Object, Float, Boolean> {

        private String guid;
        private SiteManager siteManager;
        private SiteApi siteApi;
        private SiteDoorListAdapter adapter;

        Site site;
        List<Door> doors;

        private Exception resultException;

        public SiteDoorListTask(SiteDoorListAdapter adapter, SiteManager siteManager, SiteApi siteApi, String guid) {
            this.adapter = adapter;
            this.siteManager = siteManager;
            this.siteApi = siteApi;
            this.guid = guid;
        }

        @Override
        protected Boolean doInBackground(Object... obj) {
            try {
                site = siteManager.getByGuid(guid);
                doors = siteApi.findDoors(site);
                return true;
            } catch (Exception e) {
                resultException = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (!ok || doors == null) {

                if (resultException != null) {
                    String message;
                    if (resultException instanceof SiteApi.DeviceIsBlockedException) {
                        message = getString(R.string.error_api_device_is_blocked);
                    } else {
                        message = new ApiErrorHelper(OpenDoorPopupActivity.this).toMessage(resultException);
                    }

                    Toast.makeText(OpenDoorPopupActivity.this, message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(OpenDoorPopupActivity.this, "Error", Toast.LENGTH_LONG).show();
                }
                adapter.clear();
                return;
            }
            adapter.clear();
            adapter.addAll(doors);

        }
    }



    private class OpenDoorTask extends AsyncTask<Object, Float, Boolean> {

        private SiteApi siteApi;
        private Site site;
        private Door door;
        private OnDoorOpenTaskResult result;

        private Exception resultException;

        public OpenDoorTask(SiteApi siteManager, Site site, Door door, OnDoorOpenTaskResult result) {
            this.siteApi = siteManager;
            this.site = site;
            this.door = door;
            this.result = result;
        }

        @Override
        protected Boolean doInBackground(Object... obj) {
            try {
                siteApi.openDoor(site, door);
                return true;
            } catch (Exception e) {
                resultException = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (!ok) {
                if (resultException != null) {
                    String message;
                    if (resultException instanceof SiteApi.DeviceIsBlockedException) {
                        message = getString(R.string.error_api_device_is_blocked);
                    } else {
                        message = new ApiErrorHelper(OpenDoorPopupActivity.this).toMessage(resultException);
                    }

                    Toast.makeText(OpenDoorPopupActivity.this, message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(OpenDoorPopupActivity.this, "Error", Toast.LENGTH_LONG).show();
                }
            }
            result.onResult(ok);
        }
    }


    interface OnDoorOpenTaskResult {
        void onResult(boolean success);
    }


}
