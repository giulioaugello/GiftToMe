package com.LAM.GiftToMe.Twitter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class TwitterFunctions {

    //ritorna le informazioni dell'utente
    public static void userInfo(final Context mContext, String screenName, final VolleyListener volleyListener) {
        String url = "https://api.twitter.com/1.1/users/show.json";
        String screenNameString = "?screen_name=" + screenName;
        String urlWithParams = url + screenNameString;

        TwitterOAuth generator = new TwitterOAuth(mContext.getResources().getString(R.string.CONSUMER_KEY), mContext.getResources().getString(R.string.CONSUMER_SECRET), MainActivity.token, MainActivity.tokenSecret);
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("screen_name", screenName);
        final String header = generator.generateHeader("GET", url, requestParams);

        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest getUserObjectRequest = new StringRequest(Request.Method.GET, urlWithParams,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        volleyListener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        volleyListener.onError(error);
                        error.printStackTrace();
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", header);
                return headers;
            }
        };
        queue.add(getUserObjectRequest);
    }

    //ritorna i regali dell'utente loggato
    public static void userLoggedTweets(final Context mContext, Integer count, final VolleyListener volleyListener) {

        String url = "https://api.twitter.com/1.1/statuses/user_timeline.json";

        String name = MainActivity.userName;
        String nameString = "?screen_name=" + Uri.encode(name);
        String countString = "&count=" + count;
        String fullTextTweetsString = "&tweet_mode=extended"; //per avere tutto il testo del tweet poichè altrimenti sarebbe troncato

        String urlWithParams = url + nameString + countString + fullTextTweetsString;

        TwitterOAuth generator = new TwitterOAuth(mContext.getResources().getString(R.string.CONSUMER_KEY), mContext.getResources().getString(R.string.CONSUMER_SECRET), MainActivity.token, MainActivity.tokenSecret);
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("screen_name", name);
        requestParams.put("count", String.valueOf(count));
        requestParams.put("tweet_mode", "extended");

        final String header = generator.generateHeader("GET", url, requestParams);
        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest getUserTweetsRequest = new StringRequest(Request.Method.GET, urlWithParams,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        volleyListener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        volleyListener.onError(error);
                    }
                }
        ){

        @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", header);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(getUserTweetsRequest);

    }

    //ritorna i regali degli altri utenti con l'hashtag giusto
    public static void usersTweets(final Context mContext, String q, final VolleyListener volleyListener) {

        String url = "https://api.twitter.com/1.1/search/tweets.json";

        String screenNameString = "?q=" + Uri.encode(q);
        String extendedModeString = "&tweet_mode=extended";
        String countString = "&count=100"; //il massimo numero di tweets ottenibili per ogni chiamata è 100

        String urlWithParams = url + screenNameString + extendedModeString + countString;

        TwitterOAuth generator = new TwitterOAuth(mContext.getResources().getString(R.string.CONSUMER_KEY), mContext.getResources().getString(R.string.CONSUMER_SECRET), MainActivity.token, MainActivity.tokenSecret);
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("q", q);
        requestParams.put("tweet_mode", "extended");
        requestParams.put("count", "100");

        final String header = generator.generateHeader("GET", url, requestParams);

        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest getUserObjectRequest = new StringRequest(Request.Method.GET, urlWithParams,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        volleyListener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        volleyListener.onError(error);
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", header);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(getUserObjectRequest);
    }

    //posta un tweet
    public static void postTweet(String message, String replyId, final Context mContext, final VolleyListener volleyListener) {

        String url = "https://api.twitter.com/1.1/statuses/update.json";
        String status = "?status=";

        String urlWithParams = null;
        try {
            urlWithParams = url + status + URLEncoder.encode(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        TwitterOAuth generator = new TwitterOAuth(mContext.getResources().getString(R.string.CONSUMER_KEY), mContext.getResources().getString(R.string.CONSUMER_SECRET), MainActivity.token, MainActivity.tokenSecret);
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("status", message);

        if (!replyId.isEmpty()) {
            urlWithParams += "&in_reply_to_status_id=" + replyId + "&auto_populate_reply_metadata=true";
            requestParams.put("in_reply_to_status_id", replyId);
            requestParams.put("auto_populate_reply_metadata", "true");
        }


        final String header = generator.generateHeader("POST", url, requestParams);

        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest getUserObjectRequest = new StringRequest(Request.Method.POST, urlWithParams,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        volleyListener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        volleyListener.onError(error);

                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", header);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(getUserObjectRequest);
    }

    //elimina un tweet
    public static void deleteGift(String id, Context mContext, final VolleyListener volleyListener) {
        String url = "https://api.twitter.com/1.1/statuses/destroy/" + id + ".json";

        TwitterOAuth generator = new TwitterOAuth(mContext.getString(R.string.CONSUMER_KEY), mContext.getResources().getString(R.string.CONSUMER_SECRET), MainActivity.token, MainActivity.tokenSecret);
        Map<String, String> requestParams = new HashMap<>();
        final String header = generator.generateHeader("POST", url, requestParams);

        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest getUserObjectRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        volleyListener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", header);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(getUserObjectRequest);
    }

    public static void getGiftName(final Context mContext, String tweetId, final VolleyListener volleyResponseListener){

        String url = "https://api.twitter.com/1.1/statuses/show.json";

        String id = "?id=" + tweetId;
        String extendedModeString = "&tweet_mode=extended";

        String urlWithParams = url + id + extendedModeString;

        TwitterOAuth generator = new TwitterOAuth(mContext.getResources().getString(R.string.CONSUMER_KEY) , mContext.getResources().getString(R.string.CONSUMER_SECRET) , MainActivity.token, MainActivity.tokenSecret);
        Map<String, String> requestParams = new HashMap<>();

        requestParams.put("id", tweetId);
        requestParams.put("tweet_mode", "extended");

        final String header = generator.generateHeader("GET", url, requestParams);

        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest getUserObjectRequest = new StringRequest(Request.Method.GET, urlWithParams,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String text = jsonObject.getString("full_text");

                            if(text.contains("#LAM_giftToMe_2020-article")){

                                String tweetWithoutHashtag = text.replace("#LAM_giftToMe_2020-article","");

                                JSONObject tweetWithoutHashtagJSON = new JSONObject(tweetWithoutHashtag);

                                String giftName = tweetWithoutHashtagJSON.getString("name");
                                volleyResponseListener.onResponse(giftName);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        volleyResponseListener.onError(error);
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", header);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(getUserObjectRequest);
    }

}

