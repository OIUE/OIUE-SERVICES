// package com.plugins.geom;
//
// import org.apache.cordova.CallbackContext;
// import org.apache.cordova.CordovaPlugin;
// import org.apache.cordova.PluginResult;
// import org.json.JSONArray;
// import org.json.JSONException;
// import org.json.JSONObject;
//
// import android.content.Context;
// import android.net.wifi.WifiManager;
// import java.net.NetworkInterface;
// import java.util.Collections;
// import java.util.List;
//
/// **
// * The Class getmac.
// */
// public class GeometryConvert extends CordovaPlugin {
//
// public boolean isSynch(String action) {
// if (action.equals("getEncryPoint")) {
// return true;
// }
// return false;
// }
//
// /*
// * (non-Javadoc)
// *
// * @see org.apache.cordova.api.Plugin#execute(java.lang.String,
// * org.json.JSONArray, java.lang.String)
// */
// @Override
// public boolean execute(String action, JSONArray args,
// CallbackContext callbackContext) {
//
// if (action.equals("getEncryPoint")) {
// try {
// double lng = args.getDouble(0);
//
// double lat = args.getDouble(1);
//
// Converter conver = new Converter();
//
// Point p = conver.getEncryPoint(lng, lat);
//
// String lnglat = p.getX() + "," + p.getY();
//
// JSONObject result = new JSONObject();
// result.put("lnglat", lnglat);
// PluginResult r = new PluginResult(PluginResult.Status.OK,
// result);
// callbackContext.success(lnglat);
// r.setKeepCallback(true);
// callbackContext.sendPluginResult(r);
// return true;
// } catch (JSONException e) {
// PluginResult r = new PluginResult(
// PluginResult.Status.JSON_EXCEPTION);
// callbackContext.error("error");
// r.setKeepCallback(true);
// callbackContext.sendPluginResult(r);
// return true;
// }
// }
// return false;
// }
//
// }
