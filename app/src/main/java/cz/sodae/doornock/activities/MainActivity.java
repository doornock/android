package cz.sodae.doornock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;
import com.mikepenz.materialdrawer.Drawer;

import cz.sodae.doornock.MenuFactory;
import cz.sodae.doornock.R;
import cz.sodae.doornock.activities.fragments.SiteListFragment;
import cz.sodae.doornock.model.site.Site;

public class MainActivity extends AppCompatActivity implements SiteListFragment.OnListFragmentInteractionListener {

    @Override
    public void onListFragmentInteraction(Site item) {
        Intent intent = new Intent(this, SiteDetailActivity.class);
        intent.putExtra(SiteDetailActivity.INTENT_SITE_GUID, item.getGuid());
        startActivity(intent);
        //Toast.makeText(this, item.getUrl(), Toast.LENGTH_SHORT).show();
    }

    private Drawer drawerMenu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerMenu = MenuFactory.create(this, toolbar, savedInstanceState);
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
