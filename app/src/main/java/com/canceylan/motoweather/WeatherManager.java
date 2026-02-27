package com.canceylan.motoweather;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class WeatherManager {

    private Context context;
    private RequestQueue queue;

    public interface WeatherCallback {
        void onSuccess(JSONObject response);
        void onError(String message);
    }

    public WeatherManager(Context context) {
        this.context = context;
        this.queue = Volley.newRequestQueue(context);
    }

    public void getWeather(double lat, double lon, WeatherCallback callback) {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon +
                "&current_weather=true&hourly=temperature_2m,weathercode,relativehumidity_2m,apparent_temperature,precipitation_probability,windspeed_10m,surface_pressure&daily=weathercode,temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_probability_max,windspeed_10m_max&timezone=auto";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> callback.onSuccess(response),
                error -> callback.onError("Bağlantı Hatası: " + error.getMessage())
        );
        queue.add(request);
    }
}