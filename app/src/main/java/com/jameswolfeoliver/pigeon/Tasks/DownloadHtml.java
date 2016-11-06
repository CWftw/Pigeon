package com.jameswolfeoliver.pigeon.Tasks;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class DownloadHtml {
    public static final String LOG_TAG = DownloadHtml.class.getSimpleName();

    public DownloadHtml() {
        //Empty
    }

    public static void getHtml(Context context, final String url, final Callbacks callback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        Log.i(LOG_TAG, String.format(" --> GET %s", url));

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onResponse(true, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onResponse(false, error.getMessage());
                    }
                }
        ) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Log.i(LOG_TAG, String.format(" <-- %s %s (%dms, %dBytes)",
                        response.statusCode, url, response.networkTimeMs, response.data.length));
                return super.parseNetworkResponse(response);
            }
        };

        queue.add(request);
    }

    public interface Callbacks {
        public void onResponse(boolean succeeded, String response);
    }
}
