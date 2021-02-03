package com.LAM.GiftToMe.Twitter;

import android.util.Base64;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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


    public String generateHeader(String httpMethod, String url, Map<String, String> requestParams) {

        String nonce = nonceGenerator();
        String timestamp = timestampGenerator();
        String baseSignatureString = generateSignatureBaseString(httpMethod, url, requestParams, nonce, timestamp);
        String signature = signingKeyGenerator(baseSignatureString);


        StringBuilder headerString = new StringBuilder();

        headerString.append("OAuth ");
        append(headerString, oauth_consumer_key, consumerKey);
        append(headerString, oauth_nonce, nonce);
        append(headerString, oauth_signature, signature);
        append(headerString, oauth_signature_method, signatureMethod);
        append(headerString, oauth_timestamp, timestamp);
        append(headerString, oauth_token, token);
        append(headerString, oauth_version, version);

        //elimino la virgola
        headerString.deleteCharAt(headerString.length() - 1);

        return headerString.toString();
    }

    private String nonceGenerator() {
        return UUID.randomUUID().toString();
    }

    private String timestampGenerator() {
        return Math.round((new Date()).getTime() / 1000.0) + "";
    }

    private String generateSignatureBaseString(String httpMethod, String url, Map<String, String> requestParams, String nonce, String timestamp) {
        Map<String, String> params = new HashMap<>();

        //metto in params le coppie prese in input
        for(Map.Entry<String,String> entry : requestParams.entrySet()){
            put(params, entry.getKey(), entry.getValue());
        }

        //inserisco in params i valori richiesti per la signatureBaseString
        put(params, oauth_consumer_key, consumerKey);
        put(params, oauth_nonce, nonce);
        put(params, oauth_signature_method, signatureMethod);
        put(params, oauth_timestamp, timestamp);
        put(params, oauth_token, token);
        put(params, oauth_version, version);
        TreeMap<String, String> sortedParams = new TreeMap<String, String>(params);

        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<String,String> entry : sortedParams.entrySet()){
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        //tolgo l'ultimo &
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        String baseString = httpMethod.toUpperCase() + "&" + encode(url) + "&" + encode(stringBuilder.toString());

        return baseString;
    }

    private String signingKeyGenerator(String input) {
        String secret = new StringBuilder().append(encode(consumerSecret)).append("&").append(encode(tokenSecret)).toString();
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = new SecretKeySpec(keyBytes, HMAC_SHA1);
        Mac mac;
        try {
            mac = Mac.getInstance(HMAC_SHA1);
            mac.init(key);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
        byte[] signatureBytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.encode(signatureBytes,Base64.DEFAULT));
    }

    private String encode(String value) {
        String encoded = "";
        try {
            encoded = URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sb = "";
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                sb += "%2A";
            } else if (focus == '+') {
                sb += "%20";
            } else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                sb += '~';
                i += 2;
            } else {
                sb += focus;
            }
        }
        return sb;
    }

    private void put(Map<String, String> map, String key, String value) {
        map.put(encode(key), encode(value));
    }

    private void append(StringBuilder builder, String key, String value) {
        builder.append(encode(key)).append("=\"").append(encode(value)).append("\",");
    }


}
