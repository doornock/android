package cz.sodae.doornock.activities;

import android.content.Context;

import org.json.JSONException;

import cz.sodae.doornock.R;
import cz.sodae.doornock.model.site.SiteApi;
import cz.sodae.doornock.utils.ApiSender;
import cz.sodae.doornock.utils.InvalidGUIDException;

class ApiErrorHelper {
    private Context context;

    public ApiErrorHelper(Context context) {
        this.context = context;
    }

    public String toMessage(Throwable e) {
        if (e instanceof SiteApi.TechnicalProblemException) {
            e = e.getCause();
        }

        if (e instanceof InvalidGUIDException) {
            return context.getString(R.string.error_api_invalid_guid);
        } else if (e instanceof JSONException) {
            return context.getString(R.string.error_api_invalid_json_input);
        } else if (e instanceof ApiSender.SignatureException) {
            return context.getString(R.string.error_api_invalid_signature);
        } else if (e instanceof ApiSender.ServerErrorException) {
            return "Server error:" + e.getMessage() + "#"
                    + ((ApiSender.ServerErrorException) e).getServerCode();
        } else {
            return "Technical error: " + e.getMessage();
        }
    }
}
