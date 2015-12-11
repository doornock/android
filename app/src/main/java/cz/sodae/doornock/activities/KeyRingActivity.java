package cz.sodae.doornock.activities;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.Drawer;

import cz.sodae.doornock.MenuFactory;
import cz.sodae.doornock.R;
import cz.sodae.doornock.model.keys.Key;
import cz.sodae.doornock.model.keys.KeyRing;

public class KeyRingActivity extends AppCompatActivity {

    private KeyRing keyRing;

    private Drawer drawerMenu = null;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_ring);
        setTitle(R.string.activity_key_ring_title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerMenu = MenuFactory.create(this, toolbar, savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Generování", Snackbar.LENGTH_LONG).show();
                AsyncTask asyncTask = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        Key key = Key.generateKey("New key");
                        keyRing.persist(key);
                        return key;
                    }
                };
                asyncTask.execute();

            }
        });
        fab.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_add).color(Color.WHITE).actionBar());

        keyRing = new KeyRing(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = drawerMenu.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (drawerMenu != null && drawerMenu.isDrawerOpen()) {
            drawerMenu.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}
