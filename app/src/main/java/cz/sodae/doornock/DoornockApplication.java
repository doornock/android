package cz.sodae.doornock;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class DoornockApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}