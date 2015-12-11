package cz.sodae.doornock.model.site;

import android.util.Base64;

import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cz.sodae.doornock.model.keys.Key;
import cz.sodae.doornock.utils.security.keys.RSAEncryptUtil;

class SiteApi
{
    private OkHttpClient client = new OkHttpClient();

    public void register(Site site)
    {
        try {
            Request request = new Request.Builder()
                    .url(site.getUrl() + "/register")
                    .build();

            String result = client.newCall(request).execute().body().string();
            JSONObject json = new JSONObject(result);

            if (!json.getString("state").equals("OK")) {
                throw new IOException("NOT OK");
            }

            JSONObject data = json.getJSONObject("data");
            site.setCredentials(
                    data.getString("username"),
                    data.getString("password")
            );
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public void addDevice(Site site, Key key, String description)
    {
        try {
            RequestBody rb = new MultipartBuilder()
                    .addFormDataPart("description", description)
                    .addFormDataPart("public_key", RSAEncryptUtil.encodeBASE64(key.getPublicKey().getEncoded()))
                    .build();

            Request request = new Request.Builder()
                    .url(site.getUrl() + "/add-device?username=" + site.getUsername() + "&password=" + site.getPassword())
                    .post(rb)
                    .build();

            String result = client.newCall(request).execute().body().string();
            JSONObject json = new JSONObject(result);

            if (!json.getString("state").equals("OK")) {
                throw new IOException("NOT OK");
            }

            JSONObject data = json.getJSONObject("data");
            site.setApiKey(
                    data.getString("api_key")
            );
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }


    public void updateDevice(Site site, Key key)
    {
        try {
            RequestBody rb = new MultipartBuilder()
                    .addFormDataPart("public_key", RSAEncryptUtil.encodeBASE64(key.getPublicKey().getEncoded()))
                    .build();

            Request request = new Request.Builder()
                    .url(site.getUrl() + "/update-device?api_key=" + site.getApiKey())
                    .post(rb)
                    .build();

            String result = client.newCall(request).execute().body().string();
            JSONObject json = new JSONObject(result);

            if (!json.getString("state").equals("OK")) {
                throw new IOException("NOT OK");
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

}
