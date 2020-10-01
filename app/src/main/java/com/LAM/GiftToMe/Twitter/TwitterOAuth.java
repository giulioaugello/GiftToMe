package com.LAM.GiftToMe.Twitter;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class TwitterOAuth {

    private String consumerKey;
    private String consumerSecret;
    private String signatureMethod;
    private String token;
    private String tokenSecret;
    private String version;

    private static final String oauth_consumer_key = "oauth_consumer_key";
    private static final String oauth_token = "oauth_token";
    private static final String oauth_signature_method = "oauth_signature_method";
    private static final String oauth_timestamp = "oauth_timestamp";
    private static final String oauth_nonce = "oauth_nonce";
    private static final String oauth_version = "oauth_version";
    private static final String oauth_signature = "oauth_signature";
    private static final String HMAC_SHA1 = "HmacSHA1";

    public TwitterOAuth(String consumerKey, String consumerSecret, String token, String tokenSecret){
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.token = token;
        this.tokenSecret = tokenSecret;
        this.signatureMethod = "HMAC-SHA1";
        this.version = "1.0";
    }

    private String timestampGenerator(){
        return String.valueOf(Math.round((new Date()).getTime() / 1000.0));
    }

    private String nonceGenerator(){
        String firstUUID = String.valueOf(UUID.randomUUID());
        String secondUUID = String.valueOf(UUID.randomUUID());
        return firstUUID + secondUUID;
//        Random random = ThreadLocalRandom.current();
//        byte[] r = new byte[32]; //256 bit
//        random.nextBytes(r);
//        return Base64.encodeToString(r, Base64.DEFAULT);
    }


    private String signatureBaseStringGenerator(String httpMethod, String url, Map<String, String> requestParams, String nonce, String timestamp){
        StringBuilder templateString = new StringBuilder();
        Map<String, String> params = new HashMap<>();

        //metto in params le coppie prese in input
        for(Map.Entry<String,String> entry : requestParams.entrySet()){
            params.put(entry.getKey(), entry.getValue());
        }

        //inserisco in params i valori richiesti per la signatureBaseString
        params.put(oauth_consumer_key, consumerKey);
        params.put(oauth_nonce, nonce);
        params.put(oauth_signature_method, signatureMethod);
        params.put(oauth_timestamp, timestamp);
        params.put(oauth_token, token);
        params.put(oauth_version, version);

        TreeMap<String, String> sortedMapParams = new TreeMap<>(params);

        //ogni template è formato da: chiave = valore &
        for(Map.Entry<String,String> entry : sortedMapParams.entrySet()){
            templateString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        //tolgo l'ultimo &
        templateString.deleteCharAt(templateString.length() - 1);

        return httpMethod.toUpperCase() + "&" + encodeString(url) + "&" + encodeString(templateString.toString());
    }

    private String encodeString(String value) {
        String encoded = "";
        try {
            encoded = URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String updateString = "";
        char current;
        for (int i = 0; i < encoded.length(); i++) {
            current = encoded.charAt(i);
            if (current == '*') {
                updateString += "%2A";
            } else if (current == '+') {
                updateString += "%20";
            } else if (current == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                updateString += '~';
                i += 2;
            } else {
                updateString += current;
            }
        }
        return updateString;
    }


}
