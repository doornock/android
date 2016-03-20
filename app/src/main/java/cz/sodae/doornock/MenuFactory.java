package cz.sodae.doornock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import cz.sodae.doornock.activities.AddSiteActivity;
import cz.sodae.doornock.activities.KeyRingActivity;
import cz.sodae.doornock.activities.MainActivity;

public class MenuFactory
{
    final static int MENU_ID_MAIN_ACTIVITY = 1;
    final static int MENU_ID_KEY_RING_ACTIVITY = 2;
    final static int MENU_ID_ADD_SITE_ACTIVITY = 3;


    private static IDrawerItem createMainDrawer(Activity context)
    {
        SecondaryDrawerItem drawerItem = new SecondaryDrawerItem()
                .withName(R.string.app_name)
                .withIcon(R.mipmap.ic_launcher)
                .withIdentifier(MENU_ID_MAIN_ACTIVITY);
        if (context instanceof MainActivity) {
            drawerItem.withSetSelected(true);
        }
        return drawerItem;
    }

/*
    private static IDrawerItem createKeyRingDrawer(Activity context)
    {
        SecondaryDrawerItem drawerItem = new SecondaryDrawerItem().withName(R.string.menu_activity_key_ring)
                .withIcon(GoogleMaterial.Icon.gmd_vpn_key)
                .withIdentifier(MENU_ID_KEY_RING_ACTIVITY);
        if (context instanceof KeyRingActivity) {
            drawerItem.withSetSelected(true);
        }
        return drawerItem;
    }
*/

    private static IDrawerItem createAddSiteDrawer(Activity context)
    {
        SecondaryDrawerItem drawerItem = new SecondaryDrawerItem().withName(R.string.menu_activity_add_site)
                .withIcon(GoogleMaterial.Icon.gmd_vpn_key)
                .withIdentifier(MENU_ID_ADD_SITE_ACTIVITY);
        if (context instanceof AddSiteActivity) {
            drawerItem.withSetSelected(true);
        }
        return drawerItem;
    }



    public static Drawer create(final Activity context, Toolbar toolbar, Bundle savedInstanceState)
    {
        return new DrawerBuilder()
                .withActivity(context)
                .withToolbar(toolbar)
                .withHeader(R.layout.drawer_header)
                .withHeaderDivider(false)
                .addDrawerItems(
                        createMainDrawer(context),
                        //createKeyRingDrawer(context),
                        createAddSiteDrawer(context)
                ) // add the items we want to use with our Drawer
                /*
                .withOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
                    @Override
                    public boolean onNavigationClickListener(View clickedView) {
                        return true;
                    }
                })
                */
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem.getIdentifier() == MENU_ID_KEY_RING_ACTIVITY) {
                            if (context instanceof KeyRingActivity) return false;
                            Intent i = new Intent(context, KeyRingActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            context.finish();
                            context.overridePendingTransition(0, 0);
                            context.startActivity(i);
                            return true;
                        } else if (drawerItem.getIdentifier() == MENU_ID_MAIN_ACTIVITY) {
                            if (context instanceof MainActivity) return false;
                            Intent i = new Intent(context, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            context.finish();
                            context.overridePendingTransition(0, 0);
                            context.startActivity(i);
                            return true;
                        } else if (drawerItem.getIdentifier() == MENU_ID_ADD_SITE_ACTIVITY) {
                            if (context instanceof AddSiteActivity) return false;
                            Intent i = new Intent(context, AddSiteActivity.class);
                            context.startActivity(i);
                            return true;
                        }
                        return false;
                    }
                })
                /*
                .addStickyDrawerItems(
                        new SecondaryDrawerItem().withName("Nastavení").withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(10)
                )
                */
                .withSavedInstance(savedInstanceState)
                .build();
    }
}
