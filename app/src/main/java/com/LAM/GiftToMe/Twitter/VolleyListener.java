package com.LAM.GiftToMe.Twitter;

import com.android.volley.VolleyError;

public interface VolleyListener {

    void onResponse(String response);

    void onError(VolleyError error);

}
