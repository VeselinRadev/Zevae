package com.veselin.zevae.utils;

import java.util.Date;
import java.util.TimeZone;

public class Utils {
    public static Date convertStringToDate(String s){
        int y = Integer.parseInt(s.substring(0,3));
        s = s.substring(4);
        int m = Integer.parseInt(s.substring(0,s.indexOf('.')));
        s = s.substring(s.indexOf('.')+1);
        int d = Integer.parseInt(s.substring(0,s.indexOf('.')));
        s = s.substring(s.indexOf('.')+1);
        int h = Integer.parseInt(s.substring(0,s.indexOf('.'))) + (TimeZone.getDefault().getDSTSavings() + TimeZone.getDefault().getRawOffset())/3600000;
        s = s.substring(s.indexOf('.')+1);
        int min = Integer.parseInt(s.substring(0,s.indexOf('.')));
        s = s.substring(s.indexOf('.')+1);
        int sec = Integer.parseInt(s);
        Date date = new Date(y,m,d,h,min,sec);
        return date;
    }
}
