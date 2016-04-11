package cz.sodae.doornock.model.site;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import cz.sodae.doornock.model.keys.Key;
import cz.sodae.doornock.utils.ApiSender;

public class SiteApi {
    private ApiSender apiSender = new ApiSender();

    final int CODE_DEVICE_BLOCKED = 10;
    final int CODE_BAD_CREDENTIALS = 20;
    final int CODE_REGISTRATION_UNSUPPORTED = -1;

    public SiteKnockKnock knockKnock(String url)
            throws TechnicalProblemException {
        try {
            JSONObject json = this.apiSender.get(rtrimSlash(url) + "/v1/site/knock-knock");

            JSONObject data = json.getJSONObject("data").getJSONObject("site");
            SiteKnockKnock site = new SiteKnockKnock(
                    data.getString("guid"),
                    data.getString("title")
            );
            return site;

        } catch (Exception e) {
            throw new TechnicalProblemException(e);
        }

    }


    public void register(Site site)
            throws ApiSender.ApiException,
            RegistrationUnsupportedException,
            TechnicalProblemException  {
        try {

            JSONObject json = apiSender.get(rtrimSlash(site.getUrl()) + "/v1/user/register-random");

            JSONObject data = json.getJSONObject("data");
            site.setCredentials(
                    data.getString("username"),
                    data.getString("password")
            );

        } catch (ApiSender.ServerErrorException e) {
            if (e.getServerCode() == CODE_REGISTRATION_UNSUPPORTED) {
                throw new RegistrationUnsupportedException();
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw new TechnicalProblemException(e);
        }

    }

    public void addDevice(Site site, Key key, String description)
            throws ApiSender.ApiException,
            InvalidUsernameOrPasswordException,
            DeviceIsBlockedException,
            TechnicalProblemException {

        try {

            String encodedKey = Base64.encodeToString(key.getPublicKey().getEncoded(), Base64.DEFAULT);

            JSONObject post = new JSONObject();
            post.put("description", description);
            post.put("public_key", encodedKey);
            post.put("username", site.getUsername());
            post.put("password", site.getPassword());

            JSONObject response = apiSender.post(
                    rtrimSlash(site.getUrl()) + "/v1/device/register",
                    post
            );

            JSONObject data = response.getJSONObject("data");
            site.setDeviceId(
                    data.getString("device_id")
            );
            site.setApiKey(
                    data.getString("api_key")
            );

        } catch (ApiSender.ServerErrorException e) {
            if (e.getServerCode() == CODE_BAD_CREDENTIALS) {
                throw new InvalidUsernameOrPasswordException();
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw new TechnicalProblemException(e);
        }

    }


    public void updateDevice(Site site, Key key)
            throws ApiSender.ApiException,
            InvalidUsernameOrPasswordException,
            DeviceIsBlockedException,
            TechnicalProblemException {
        try {
            JSONObject post = new JSONObject();
            post.put("public_key", Base64.encodeToString(key.getPublicKey().getEncoded(), Base64.DEFAULT));

            JSONObject response = apiSender.post(
                    rtrimSlash(site.getUrl()) + "/v1/device/update",
                    post,
                    site.getDeviceId(),
                    site.getApiKey()
            );
        } catch (ApiSender.ServerErrorException e) {
            if (e.getServerCode() == CODE_DEVICE_BLOCKED) {
                throw new DeviceIsBlockedException();
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw new TechnicalProblemException(e);
        }
    }


    public List<Door> findDoors(Site site)
            throws ApiSender.ApiException,
            DeviceIsBlockedException,
            TechnicalProblemException {

        try {
            JSONObject json = apiSender.get(
                    rtrimSlash(site.getUrl()) + "/v1/device/door/list",
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

        } catch (ApiSender.ServerErrorException e) {
            if (e.getServerCode() == CODE_DEVICE_BLOCKED) {
                throw new DeviceIsBlockedException();
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw new TechnicalProblemException(e);
        }
    }


    public void openDoor(Site site, Door door)
            throws ApiSender.ApiException,
            DeviceIsBlockedException,
            TechnicalProblemException {
        try {
            JSONObject post = new JSONObject();
            post.put("door_id", door.getId());
            JSONObject response = apiSender.post(
                    rtrimSlash(site.getUrl()) + "/v1/device/door/open",
                    post,
                    site.getDeviceId(),
                    site.getApiKey()
            );

        } catch (ApiSender.ServerErrorException e) {
            if (e.getServerCode() == CODE_DEVICE_BLOCKED) {
                throw new DeviceIsBlockedException();
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw new TechnicalProblemException(e);
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


    private static String rtrimSlash(String url) {
        return url.replaceAll("/+$", "");
    }

    public class RegistrationUnsupportedException extends Exception {
    }

    public class InvalidUsernameOrPasswordException extends Exception {
    }

    public class DeviceIsBlockedException extends Exception {
    }

    public class TechnicalProblemException extends Exception {
        public TechnicalProblemException(Throwable throwable) {
            super(throwable);
        }
    }

}