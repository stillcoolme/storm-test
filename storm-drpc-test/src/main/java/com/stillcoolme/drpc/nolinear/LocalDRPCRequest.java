package com.stillcoolme.drpc.nolinear;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ChenJiaHao
 * Date: 2019-06-27
 */
public class LocalDRPCRequest {

    private static String extraInfo = "1234";
    private static String key = "1234";
    private static String dbType = "test";
    private static String dbName_alert = "face_alert_db";
    private static String feature = "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";
    private static String url = "http://test/";
    private static String test_local_db = "test_create_db";

    private static String camera_id = "1";
    private static Integer camera_idx = 1;
    private static Integer region_id = 1;

    private static String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

    public static final String ADD_FEATURE_2_LOCAL_DB = "{\n" +
            "     \t\"method\": \"add\",\n" +
            "\t\"url\": \"" + url + "\",\n" +
            "\t\"extraInfo\": \"" + extraInfo + "\",\n" +
            "\t\"key\": \"" + key + "\",\n" +
            "\t\"dbType\": \"" + dbType + "\",\n" +
            "\t\"dbName\": \"" + dbName_alert + "\",\n" +
            "\t\"feature\": \"" + feature + "\"\n" +
            "}";
}
