package tech.rika.tsinghuautils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.LoginFilter;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ch.boye.httpclientandroidlib.impl.client.DefaultServiceUnavailableRetryStrategy;

/**
 * This class echoes a string called from JavaScript.
 */
public class TsinghuaUtils extends CordovaPlugin {
    private Api api;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("login")) {
            String username = args.getString(0);
            String password = args.getString(1);
            cordova.getThreadPool().execute(() -> login(username, password, callbackContext));
            return true;
        }
        if (action.equals("getCalendar")) {
            String start = args.getString(0);
            String end = args.getString(1);
            cordova.getThreadPool().execute(() -> getCalendar(start, end, callbackContext));
            return true;
        }
        callbackContext.error("No function found");
        return false;
    }

    private void login(String username, String password, CallbackContext callbackContext) {
        try {
            SharedPreferences loginInfo = cordova.getActivity().getApplicationContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
            if (username.length() == 0 && password.length() == 0) {
                username = loginInfo.getString("username", username);
                password = loginInfo.getString("password", password);
            }
            SharedPreferences.Editor editor = loginInfo.edit();
            editor.putString("username", username);
            editor.putString("password", password);
            editor.apply();
            if (!api.login(username, password))
                callbackContext.error("Try again");
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void getCalendar(String start, String end, CallbackContext callbackContext) {
        try {
            SharedPreferences eventsInfo = cordova.getActivity().getApplicationContext().getSharedPreferences("eventsInfo", Context.MODE_PRIVATE);
            String retJson = api.getCalendar(start, end);
            callbackContext.success(retJson);
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        api = new Api(cordova);
    }
}
