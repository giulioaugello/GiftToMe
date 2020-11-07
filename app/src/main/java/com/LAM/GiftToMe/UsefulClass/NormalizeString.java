package com.LAM.GiftToMe.UsefulClass;

import java.util.UUID;

public class NormalizeString {

    public static String normalizeTask(Double lat, Double lon, String name, String description, String category, String username){

        final String hashtag = "#LAM_giftToMe_2020-article";
        String id = UUID.randomUUID().toString();

        return hashtag +
                "{" + "\"id\":" + "\"" + id +"\"," +
                "\"issuer\":"+"\"" + username+ "\"," +
                "\"category\":" + "\"" + category + "\"," +
                "\"name\":" + "\"" + name + "\"," +
                "\"lat\":" + lat + "," + "\"lon\":" + lon + "," +
                "\"description\":" + "\"" + description + "\"}";
    }

}
