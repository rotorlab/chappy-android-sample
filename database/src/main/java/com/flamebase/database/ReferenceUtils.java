package com.flamebase.database;

/**
 * Created by efraespada on 29/06/2017.
 */

public class ReferenceUtils {

    private ReferenceUtils() {
        // nothing to do here
    }

    public static String hex2String(String value) {
        String val = "";
        for (int i = 0; i < value.length(); i += 2) {
            String s = value.substring(i, (i + 2));
            int decimal = Integer.parseInt(s, 16);
            val = val + (char) decimal;
        }
        return val;
    }

}
