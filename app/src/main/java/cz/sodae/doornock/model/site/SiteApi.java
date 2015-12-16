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
import cz.sodae.doornock.utils.InvalidGUIDException;
import cz.sodae.doornock.utils.security.keys.RSAEncryptUtil;

public class SiteApi
{
    private OkHttpClient client = new OkHttpClient();


    public SiteKnockKnock knockKnock(String url) throws ApiException, InvalidGUIDException
    {
        try {
            Request request = new Request.Builder()
                    .url(url + "/knock-knock")
                    .build();

            String result = client.newCall(request).execute().body().string();
            JSONObject json = new JSONObject(result);

            if (!json.getString("status").equals("OK")) {
                throw new ApiException("NOT OK");
            }

            JSONObject data = json.getJSONObject("data").getJSONObject("site");
            SiteKnockKnock site = new SiteKnockKnock(data.getString("guid"), data.getString("title"));
            return site;
        } catch (JSONException | IOException e) {
            throw new ApiException(e);
        }
    }


    public void register(Site site) throws RegistrationFailedException
    {
        try {
            Request request = new Request.Builder()
                    .url(site.getUrl() + "/register")
                    .build();

            String result = client.newCall(request).execute().body().string();
            JSONObject json = new JSONObject(result);

            if (!json.getString("status").equals("OK")) {
                throw new ApiException("NOT OK");
            }

            JSONObject data = json.getJSONObject("data");
            site.setCredentials(
                    data.getString("username"),
                    data.getString("password")
            );
        } catch (JSONException | IOException e) {
            throw new RegistrationFailedException(e);
        }
    }

    public void addDevice(Site site, Key key, String description) throws AddDeviceFailedException
    {
        try {
            String encodedKey = RSAEncryptUtil.encodeBASE64(key.getPublicKey().getEncoded());
            RequestBody rb = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("description", description)
                    .addFormDataPart("public_key", encodedKey)
                    .build();

            Request request = new Request.Builder()
                    .url(site.getUrl() + "/add-device?username=" + site.getUsername() + "&password=" + site.getPassword())
                    .post(rb)
                    .build();

            String result = client.newCall(request).execute().body().string();
            JSONObject json = new JSONObject(result);

            if (!json.getString("status").equals("OK")) {
                throw new ApiException("NOT OK");
            }

            JSONObject data = json.getJSONObject("data");
            site.setDeviceId(
                    data.getString("device_id")
            );
            site.setApiKey(
                    data.getString("api_key")
            );
        } catch (JSONException | IOException e) {
            throw new AddDeviceFailedException(e);
        }
    }


    public void updateDevice(Site site, Key key) throws ApiException
    {
        try {

            RequestBody rb = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("public_key", RSAEncryptUtil.encodeBASE64(key.getPublicKey().getEncoded()))
                    .build();

            Request request = new Request.Builder()
                    .url(site.getUrl() + "/update-device?api_key=" + site.getApiKey())
                    .post(rb)
                    .build();

            String result = client.newCall(request).execute().body().string();
            JSONObject json = new JSONObject(result);

            if (!json.getString("status").equals("OK")) {
                throw new ApiException("NOT OK");
            }

        } catch (JSONException | IOException e) {
            throw new ApiException("NOT OK", e);
        }
    }

    public class ApiException extends IOException
    {
        public ApiException() {
        }

        public ApiException(String detailMessage) {
            super(detailMessage);
        }

        public ApiException(String message, Throwable cause) {
            super(message, cause);
        }

        public ApiException(Throwable cause) {
            super(cause);
        }
    }


    public class RegistrationFailedException extends Exception
    {
        public RegistrationFailedException() {
        }

        public RegistrationFailedException(String detailMessage) {
            super(detailMessage);
        }

        public RegistrationFailedException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public RegistrationFailedException(Throwable throwable) {
            super(throwable);
        }
    }

    public class AddDeviceFailedException extends Exception
    {
        public AddDeviceFailedException() {
        }

        public AddDeviceFailedException(String detailMessage) {
            super(detailMessage);
        }

        public AddDeviceFailedException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public AddDeviceFailedException(Throwable throwable) {
            super(throwable);
        }
    }


}
