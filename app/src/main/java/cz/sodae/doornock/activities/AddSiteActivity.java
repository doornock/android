package cz.sodae.doornock.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.json.JSONException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.sodae.doornock.R;
import cz.sodae.doornock.model.keys.Key;
import cz.sodae.doornock.model.site.Site;
import cz.sodae.doornock.model.site.SiteApi;
import cz.sodae.doornock.model.site.SiteKnockKnock;
import cz.sodae.doornock.model.site.SiteManager;
import cz.sodae.doornock.utils.ApiSender;
import cz.sodae.doornock.utils.InvalidGUIDException;

public class AddSiteActivity extends AppCompatActivity {

    @Bind(R.id.site_url) EditText site_url;
    @Bind(R.id.login_username) EditText login_username;
    @Bind(R.id.login_password) EditText login_password;
    @Bind(R.id.know_login) CheckBox know_login;
    @Bind(R.id.description) EditText description_text;
    @Bind(R.id.btn_scan_qr) ImageButton btn_scan_qr;


    @Bind(R.id.loaded_site_guid) TextView loaded_site_guid;
    @Bind(R.id.loaded_site_title) TextView loaded_site_title;

    final static int QR_CODE_SCAN = 1;

    boolean inCommunication = false;

    SiteManager siteManager;
    SiteApi siteApi;
    SiteKnockKnock loadedSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_site);
        setTitle(R.string.activity_add_site_title);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        know_login.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onKnownLoginChecked(b);
            }
        });
        onKnownLoginChecked(know_login.isChecked());

        if (description_text.getText().toString().equals("")) {
            description_text.setText(Build.MODEL);
        }


        btn_scan_qr.setImageDrawable(new IconicsDrawable(this, CommunityMaterial.Icon.cmd_qrcode).color(Color.BLACK).actionBar());
        btn_scan_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                    startActivityForResult(intent, QR_CODE_SCAN);
                } catch (Exception e) {
                    Toast.makeText(AddSiteActivity.this, getString(R.string.activity_add_site_qr_reader_missing), Toast.LENGTH_LONG).show();
                    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                    startActivity(marketIntent);
                }
            }
        });

        this.siteManager = new SiteManager(this);
        this.siteApi = new SiteApi();
    }

    public synchronized void setKnock(SiteKnockKnock site) {
        loadedSite = site;
        if (site == null) {
            findViewById(R.id.loaded_site_frame).setVisibility(View.GONE);
        } else {
            loaded_site_title.setText(site.getTitle());
            loaded_site_guid.setText(site.getGuid());
            findViewById(R.id.loaded_site_frame).setVisibility(View.VISIBLE);
        }
    }


    @OnClick(R.id.btn_start)
    public void onSubmit(Button button)
    {
        if (inCommunication) return;

        final String s_site_url = site_url.getText().toString();
        final String s_login_name = login_username.getText().toString();
        final String s_login_pass = login_password.getText().toString();
        final String s_description = description_text.getText().toString();
        final boolean s_know_login = know_login.isChecked();

        boolean isUrl = Patterns.WEB_URL.matcher(s_site_url).matches();
        if (s_site_url.equals("") || !isUrl) {
            site_url.setError(getString(R.string.activity_add_site_error_invalid_url));
            site_url.requestFocus();
            return;
        }

        if (s_description.equals("")) {
            description_text.setError(getString(R.string.activity_add_site_error_missing_description));
            description_text.requestFocus();
            return;
        }

        if (know_login.isChecked() && s_login_name.equals("")) {
            login_username.setError(getString(R.string.activity_add_site_error_missing_credentials));
            login_username.requestFocus();
            return;
        }


        Site site = new Site(s_site_url);
        if (s_know_login) {
            site.setCredentials(s_login_name, s_login_pass);
        }

        AsyncTask async = new AddDeviceTask(siteManager, siteApi);
        async.execute(site, s_know_login, s_description);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_CODE_SCAN) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                site_url.setText(contents);
                site_url.setError(null);
                new SiteKnockKnockTask(this.siteApi).execute(contents);
            }
        }
    }


    protected void onKnownLoginChecked(boolean isChecked)
    {
        final LinearLayout login_frame =  (LinearLayout) findViewById(R.id.login_frame);
        login_frame.setVisibility( isChecked ? View.VISIBLE : View.GONE);
        login_username.setEnabled(isChecked);
        login_password.setEnabled(isChecked);

    }

    private class SiteKnockKnockTask extends AsyncTask<Object, Float, SiteKnockKnock> {

        private SiteApi siteApi;

        public SiteKnockKnockTask(SiteApi siteApi) {
            this.siteApi = siteApi;
        }

        @Override
        protected SiteKnockKnock doInBackground(Object... obj) {
            String url = (String) obj[0];
            try {
                return siteApi.knockKnock(url);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(SiteKnockKnock siteKnockKnock) {
            if (siteKnockKnock == null) return;

            AddSiteActivity.this.setKnock(siteKnockKnock);
        }
    }

    private class AddDeviceTask extends AsyncTask<Object, Site, AddDeviceResult> {

        AlertDialog progressDialog;

        private SiteManager siteManager;
        private SiteApi siteApi;

        public AddDeviceTask(SiteManager siteManager, SiteApi siteApi) {
            this.siteManager = siteManager;
            this.siteApi = siteApi;
        }

        @Override
        protected void onPreExecute() {
            AddSiteActivity.this.inCommunication = true;

            AlertDialog.Builder factory  = new AlertDialog.Builder(AddSiteActivity.this);
            factory.setMessage(R.string.activity_add_site_in_progress);
            progressDialog = factory.create();
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected AddDeviceResult doInBackground(Object... obj) {
            Site site = (Site) obj[0];
            Boolean know_login = (Boolean) obj[1];
            String desc = (String) obj[2];

            AddDeviceResult result = new AddDeviceResult(site);

            try {
                SiteKnockKnock knock = siteApi.knockKnock(site.getUrl());
                site.setTitle(knock.getTitle());
                site.setGuid(knock.getGuid());

                Site found = siteManager.getByGuid(knock.getGuid());
                if (found != null) {
                    return result.alreadyAdded(found, knock);
                }

                if (know_login.equals(false)) {
                    siteApi.register(site);
                }

                Key key = Key.generateKey(knock.getTitle());
                site.setKey(key);
                siteApi.addDevice(site, key, desc);
                siteManager.save(site);

                return result;

            } catch (Exception e) {
                return result.setException(e);
            }
        }



        protected void onPostExecute(AddDeviceResult result) {

            progressDialog.hide();
            progressDialog = null;
            AddSiteActivity.this.inCommunication = false;

            if (result.ok) {
                Intent i = new Intent(AddSiteActivity.this, MainActivity.class);
                finish();
                startActivity(i);
                return;
            }

            AddSiteActivity.this.setKnock(result.siteKnockKnock);

            know_login.setChecked(true);
            login_username.setText(result.site.getUsername());
            login_password.setText(result.site.getPassword());

            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(AddSiteActivity.this);


            if (result.exception != null) {
                result.exception.printStackTrace();

                String message = "Unknown error";

                ApiErrorHelper helper = new ApiErrorHelper(AddSiteActivity.this);

                if (result.exception instanceof SiteApi.RegistrationUnsupportedException) {
                    message = getString(R.string.activity_add_site_error_site_registration_is_not_supported);
                } else if (result.exception instanceof SiteApi.InvalidUsernameOrPasswordException) {
                    message = getString(R.string.activity_add_site_error_site_registration_bad_credentials);
                } else if (result.exception instanceof SiteApi.TechnicalProblemException && result.exception.getCause() != null) {
                    message = helper.toMessage(result.exception.getCause());
                } else if (result.exception instanceof ApiSender.ServerErrorException) {
                    message = "Server error:" + result.exception.getMessage() + "#"
                            + ((ApiSender.ServerErrorException) result.exception).getServerCode();
                }

                dlgAlert.setMessage(message);
            }

            dlgAlert.setTitle(R.string.activity_add_site_error_site_registration_failed_title);
            dlgAlert.setPositiveButton(R.string.activity_add_site_error_site_registration_failed_understand_button, null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
        }

    }

    private class AddDeviceResult {

        public AddDeviceResult(Site site) {
            this.site = site;
            this.ok = true;
        }

        public Site site;
        public boolean ok;

        public Exception exception;

        public SiteKnockKnock siteKnockKnock;
        public Site alreadyAddedSite;

        public AddDeviceResult alreadyAdded(Site site, SiteKnockKnock knockKnock) {
            this.alreadyAddedSite = site;
            this.siteKnockKnock = knockKnock;
            this.ok = false;
            return this;
        }

        public AddDeviceResult setException(Exception exception) {
            this.ok = false;
            this.exception = exception;
            return this;
        }
    }

}
