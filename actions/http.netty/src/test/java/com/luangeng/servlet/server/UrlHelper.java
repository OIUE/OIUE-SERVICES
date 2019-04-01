package com.luangeng.servlet.server;

import java.util.HashMap;
import java.util.Map;

public class UrlHelper {
    public static Map<String, String> getParams(String uri) {
        Map<String, String> map = new HashMap<>();
        int i = uri.indexOf("?");
        if (i == -1) {
            return map;
        }
        uri = uri.substring(i + 1);
        String[] pair = uri.split("&");
        for (String str : pair) {
            String[] pp = str.split("=");
            if (pp.length == 2) {
                map.put(pp[0], pp[1]);
            } else if (pp.length == 1) {
                map.put(pp[0], "");
            } else {
                //error
                return null;
            }
        }

        return map;
    }
}
