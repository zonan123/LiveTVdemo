package com.mediatek.wwtv.tvcenter.nav.bean;

import com.mediatek.mtkmdsclient.data.MdsBaseRequestData;

public class NowNextRequestData extends MdsBaseRequestData {
    public static final String TYPE_NOW = "true";
//    public static final String TYPE_NOW_NEXT   = "window";
    public NowNext now_next = new NowNext();
    public NowNextRequestData(String sid, String type) {
        super();
        now_next.sid = sid;
        now_next.type = type;
        etype = "now_next";
    }
    static class  NowNext{
        public String type = "true";
        public String sid;
    }
}
