package cz.sodae.doornock.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiSender
{
    private OkHttpClient client = new OkHttpClient();

    private final String authHeaderKey = "X-API-Auth-V1";

    private final String signHeaderKey = "X-API-Sign-V1";


    public final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");



    public JSONObject get(String url) throws IOException, ApiException {
        Request.Builder request = new Request.Builder()
                .url(url);

        Response response = client.newCall(request.build()).execute();
        String content = response.body().string();

        return convertSuccessResponse(response, content);
    }


    public JSONObject post(String url, JSONObject post) throws IOException, ApiException {
        Request.Builder request = new Request.Builder()
                .url(url);

        String jsonBody = post.toString();
        request.post(RequestBody.create(JSON, jsonBody));

        Response response = client.newCall(request.build()).execute();
        String content = response.body().string();
        return convertSuccessResponse(response, content);
    }




    public JSONObject get(String url, String id, String apiKey) throws IOException, ApiException {
        Request.Builder request = new Request.Builder()
                .url(url);

        String authHeaderValue = generateAuthHeader("GET", url, id, apiKey, "");
        request.header(authHeaderKey, authHeaderValue);

        Response response = client.newCall(request.build()).execute();
        String content = response.body().string();

        checkResponseSign(response, content, authHeaderValue, apiKey);
        return convertSuccessResponse(response, content);
    }

    public JSONObject post(String url, JSONObject post, String id, String apiKey)
            throws IOException, ApiException {
        Request.Builder request = new Request.Builder()
                .url(url);

        String jsonBody = post.toString();
        request.post(RequestBody.create(JSON, jsonBody));

        String authHeaderValue = generateAuthHeader("POST", url, id, apiKey, jsonBody);
        request.header(authHeaderKey, authHeaderValue);

        Response response = client.newCall(request.build()).execute();
        String content = response.body().string();

        checkResponseSign(response, content, authHeaderValue, apiKey);
        return convertSuccessResponse(response, content);
    }


    private JSONObject convertSuccessResponse(Response response, String content)
            throws ApiException {
        JSONObject json;
        try {
            json = new JSONObject(content);
        } catch (JSONException e) {
            throw new ApiException("Api response is not JSON");
        }

        try {
            if (json.getString("status").equals("ERROR")) {
                JSONObject error = json.getJSONObject("error");
                throw new ServerErrorException(
                        error.getString("message"),
                        error.getInt("code"),
                        response.code()
                );
            }

        } catch (JSONException e) {
            throw new ApiException(e);
        }

        if (!response.isSuccessful()) {
            throw new ApiException("Api response status is not successful");
        }

        return json;
    }


    private void checkResponseSign(Response response, String content, String previousKey, String apiKey)
            throws IOException, ApiException
    {
        try {
            String sign = response.header(signHeaderKey);
            if (sign == null) {
                throw new SignatureException("Api response is not signed!");
            }

            String calc = Hmac256.calculate(apiKey, previousKey + "|" +  content);
            if (!calc.equals(sign)) {
                throw new SignatureException("Api response has bad signature!");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IOException(e);
        }
    }


    private String generateAuthHeader(
            String method,
            String url,
            String deviceId,
            String apiKey,
            String body
    ) throws IOException {
        try {
            long now = (System.currentTimeMillis() / 1000L);
            String input = now + "|" + method + " " + (new URL(url)).getPath() + "|" + body;
            return now + " "  + deviceId + " "  +  Hmac256.calculate(apiKey, input);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IOException(e);
        }
    }

    public class ServerErrorException extends ApiException
    {
        private int serverCode;

        private String serverMessage;

        private int httpCode;

        public ServerErrorException(String serverMessage, int serverCode, int httpCode) {
            super("Api response error: #" + serverCode + " " + serverMessage);
            this.serverMessage = serverMessage;
            this.serverCode = serverCode;
            this.httpCode = httpCode;
        }

        public int getServerCode() {
            return serverCode;
        }

        public String getServerMessage() {
            return serverMessage;
        }

        public int getHttpCode() {
            return httpCode;
        }
    }

    public class SignatureException extends ApiException
    {
        public SignatureException(String detailMessage) {
            super(detailMessage);
        }
    }

    public class ApiException extends Exception
    {

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

}
