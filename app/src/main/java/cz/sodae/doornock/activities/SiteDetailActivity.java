package cz.sodae.doornock.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.sodae.doornock.R;
import cz.sodae.doornock.model.keys.Key;
import cz.sodae.doornock.model.site.Site;
import cz.sodae.doornock.model.site.SiteApi;
import cz.sodae.doornock.model.site.SiteManager;
import cz.sodae.doornock.utils.InvalidGUIDException;

public class SiteDetailActivity extends AppCompatActivity {

    final static String INTENT_SITE_GUID = "site_guid";
    public static final String LOG_TAG = "Doornock/SiteDetail";

    SiteManager siteManager;
    Site site;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        String guid = getIntent().getStringExtra(INTENT_SITE_GUID);
        if (guid == null) {
            Log.w(LOG_TAG, "Site id is not set");
            finish();
            return;
        }

        this.siteManager = new SiteManager(this);

        reload(guid);
    }


    private void reload(String guid)
    {
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

        setTitle(site.getTitle());

        setText(R.id.site_guid, site.getGuid());
        setText(R.id.site_url, site.getUrl());
        setText(R.id.site_username, site.getUsername());
        setText(R.id.device_id, site.getDeviceId());
        setText(R.id.device_api_key, site.getApiKey());
        setText(R.id.device_key, site.getKey() != null ? site.getKey().getTitle() : null);
    }


    private void setText(int id, String value) {
        ((TextView)findViewById(id)).setText(value == null ? "-" : value);
    }


    @OnClick(R.id.btn_remove)
    public void onClickDelete(View view)
    {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.activity_detail_site_remove_site)
                .setMessage(site.getTitle())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        siteManager.remove(site);
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    @OnClick(R.id.btn_new_key)
    public void onClickUpdateKey(View view) {
        Snackbar.make(view, R.string.activity_detail_site_generating_key, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        new SiteUpdateKeyTask(this.siteManager, this.site.getGuid()).execute();
    }


    @OnClick(R.id.fab)
    public void onClickOpenDoor(View view) {
        Intent i = new Intent(this, OpenDoorPopupActivity.class);
        i.putExtra("guid", this.site.getGuid());
        startActivityForResult(i, 0);
    }


    private class SiteUpdateKeyTask extends AsyncTask<Object, Float, Boolean> {

        private String guid;

        private SiteManager siteManager;

        public SiteUpdateKeyTask(SiteManager siteManager, String guid) {
            this.siteManager = siteManager;
            this.guid = guid;
        }

        @Override
        protected Boolean doInBackground(Object... obj) {
            try {
                Site site = siteManager.getByGuid(guid);

                DateFormat dateFormat = DateFormat.getDateTimeInstance();
                Key key = Key.generateKey(dateFormat.format(new Date()));
                siteManager.updateDevice(site, key);
                return true;
            } catch (InvalidGUIDException e) {
                e.printStackTrace();
                return false;
            } catch (SiteApi.SiteApiException e) {
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean ok) {
            reload(guid);
        }
    }

}
