package cz.sodae.doornock.model.site;

import android.util.Base64;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import cz.sodae.doornock.model.keys.Key;
import cz.sodae.doornock.utils.Hmac256;
import cz.sodae.doornock.utils.InvalidGUIDException;

public class SiteApi
{
    private ApiSender apiSender = new ApiSender();

    public SiteKnockKnock knockKnock(String url) throws ApiException, InvalidGUIDException
    {
        try {
            JSONObject json = this.apiSender.get(new Site(url), "/v1/site/knock-knock");

            JSONObject data = json.getJSONObject("data").getJSONObject("site");
            SiteKnockKnock site = new SiteKnockKnock(
                    data.getString("guid"),
                    data.getString("title")
            );
            return site;
        } catch (IllegalArgumentException | JSONException | IOException e) {
            throw new ApiException(e);
        }
    }


    public void register(Site site) throws RegistrationFailedException
    {
        try {
            JSONObject json = apiSender.get(site, "/v1/user/register-random");

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
            String encodedKey = Base64.encodeToString(key.getPublicKey().getEncoded(), Base64.DEFAULT);

            JSONObject post = new JSONObject();
            post.put("description", description);
            post.put("public_key", encodedKey);
            post.put("username", site.getUsername());
            post.put("password", site.getPassword());

            JSONObject response = apiSender.post(site, "/v1/device/register", post);

            JSONObject data = response.getJSONObject("data");
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
            JSONObject post = new JSONObject();
            post.put("public_key",  Base64.encodeToString(key.getPublicKey().getEncoded(), Base64.DEFAULT));

            JSONObject response = apiSender.post(site, "/v1/device/update", post);
        } catch (JSONException | IOException e) {
            throw new ApiException("NOT OK", e);
        }
    }


    public List<Door> findDoors(Site site) throws FindDoorsException
    {
        try {
            JSONObject json = apiSender.get(site, "/v1/device/door/list");
            JSONArray data = json.getJSONArray("data");

            List<Door> list = new LinkedList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject doorData = data.getJSONObject(i);
                list.add(new Door(
                        doorData.getString("id"),
                        doorData.getString("title"),
                        doorData.getBoolean("access")
                ));
            }

            return list;

        } catch (JSONException | IOException e) {
            throw new FindDoorsException(e);
        }
    }


    public void openDoor(Site site, Door door) throws OpenDoorException
    {
        try {
            JSONObject post = new JSONObject();
            post.put("door_id", door.getId());
            apiSender.post(site, "/v1/device/door/open", post);
        } catch (JSONException | IOException e) {
            throw new OpenDoorException(e);
        }
    }



    private class ApiSender
    {
        private OkHttpClient client = new OkHttpClient();

        private final String authHeaderKey = "X-API-Auth-V1";
        public final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");


        public JSONObject get(Site site, String url) throws IOException, JSONException
        {
            Request.Builder request = new Request.Builder()
                    .url(site.getUrl() + url);

            if (site.getApiKey() != null) {
                request.header(authHeaderKey, generateAuthHeader(site, "GET", url, ""));
            }
            return call(request.build());
        }

        public JSONObject post(Site site, String url, JSONObject post) throws IOException, JSONException
        {
            Request.Builder request = new Request.Builder()
                    .url(site.getUrl() + url);

            String jsonBody = post.toString();
            request.post(RequestBody.create(JSON, jsonBody));

            if (site.getApiKey() != null) {
                request.header(authHeaderKey, generateAuthHeader(site, "POST", url, jsonBody));
            }

            return call(request.build());
        }


        private JSONObject call(Request request) throws IOException, JSONException
        {
            String result = client.newCall(request).execute().body().string();
            JSONObject json = new JSONObject(result);

            if (!json.getString("status").equals("OK")) {
                throw new ApiException("NOT OK");
            }
            return json;
        }



        private String generateAuthHeader(Site site, String method, String url, String body) throws IOException
        {
            try {
                long now = (System.currentTimeMillis() / 1000L);
                String input = now + "|" + method + " " + (new URL(site.getUrl() + url)).getPath() + "|" + body;
                return now + " "  + site.getDeviceId() + " "  +  Hmac256.calculate(site.getApiKey(), input);
            } catch (Exception e) {
                throw new IOException(e);
            }
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

    public class FindDoorsException extends Exception
    {
        public FindDoorsException() {
        }

        public FindDoorsException(String detailMessage) {
            super(detailMessage);
        }

        public FindDoorsException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public FindDoorsException(Throwable throwable) {
            super(throwable);
        }
    }

    public class OpenDoorException extends Exception
    {
        public OpenDoorException() {
        }

        public OpenDoorException(String detailMessage) {
            super(detailMessage);
        }

        public OpenDoorException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public OpenDoorException(Throwable throwable) {
            super(throwable);
        }
    }


    private static String encodeQueryParam(String input)
    {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return input;
        }
    }

}
