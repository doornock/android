package cz.sodae.doornock.model.site;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import cz.sodae.doornock.model.keys.Key;
import cz.sodae.doornock.utils.ApiSender;
import cz.sodae.doornock.utils.InvalidGUIDException;

public class SiteApi {
    private ApiSender apiSender = new ApiSender();

    public SiteKnockKnock knockKnock(String url) throws SiteApiException, InvalidGUIDException {
        try {
            JSONObject json = this.apiSender.get(url.replaceAll("/+$", "") + "/v1/site/knock-knock");

            JSONObject data = json.getJSONObject("data").getJSONObject("site");
            SiteKnockKnock site = new SiteKnockKnock(
                    data.getString("guid"),
                    data.getString("title")
            );
            return site;
        } catch (ApiSender.ApiException | JSONException | IOException e) {
            throw new SiteApiException(e);
        }
    }


    public void register(Site site) throws SiteApiException {
        try {
            JSONObject json = apiSender.get(site.getUrl().replaceAll("/+$", "") + "/v1/user/register-random");

            JSONObject data = json.getJSONObject("data");
            site.setCredentials(
                    data.getString("username"),
                    data.getString("password")
            );
        } catch (ApiSender.ApiException | JSONException | IOException e) {
            throw new SiteApiException(e);
        }
    }

    public void addDevice(Site site, Key key, String description) throws SiteApiException {
        try {
            String encodedKey = Base64.encodeToString(key.getPublicKey().getEncoded(), Base64.DEFAULT);

            JSONObject post = new JSONObject();
            post.put("description", description);
            post.put("public_key", encodedKey);
            post.put("username", site.getUsername());
            post.put("password", site.getPassword());

            JSONObject response = apiSender.post(
                    site.getUrl().replaceAll("/+$", "") + "/v1/device/register",
                    post
            );

            JSONObject data = response.getJSONObject("data");
            site.setDeviceId(
                    data.getString("device_id")
            );
            site.setApiKey(
                    data.getString("api_key")
            );
        } catch (ApiSender.ApiException | JSONException | IOException e) {
            throw new SiteApiException(e);
        }
    }


    public void updateDevice(Site site, Key key) throws SiteApiException {
        try {
            JSONObject post = new JSONObject();
            post.put("public_key", Base64.encodeToString(key.getPublicKey().getEncoded(), Base64.DEFAULT));

            JSONObject response = apiSender.post(
                    site.getUrl().replaceAll("/+$", "") + "/v1/device/update",
                    post,
                    site.getDeviceId(),
                    site.getApiKey()
            );
        } catch (ApiSender.ApiException | JSONException | IOException e) {
            throw new SiteApiException(e);
        }
    }


    public List<Door> findDoors(Site site) throws SiteApiException {
        try {
            JSONObject json = apiSender.get(
                    site.getUrl().replaceAll("/+$", "") + "/v1/device/door/list",
                    site.getDeviceId(),
                    site.getApiKey()
            );
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

        } catch (ApiSender.ApiException | JSONException | IOException e) {
            throw new SiteApiException(e);
        }
    }


    public void openDoor(Site site, Door door) throws SiteApiException {
        try {
            JSONObject post = new JSONObject();
            post.put("door_id", door.getId());
            JSONObject response = apiSender.post(
                    site.getUrl().replaceAll("/+$", "") + "/v1/device/door/open",
                    post,
                    site.getDeviceId(),
                    site.getApiKey()
            );
        } catch (ApiSender.ApiException | IOException | JSONException e) {
            throw new SiteApiException(e);
        }
    }


    public class SiteApiException extends IOException {
        public SiteApiException(String detailMessage) {
            super(detailMessage);
        }

        public SiteApiException(String message, Throwable cause) {
            super(message, cause);
        }

        public SiteApiException(Throwable cause) {
            super(cause);
        }
    }


    private static String encodeQueryParam(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return input;
        }
    }

}
