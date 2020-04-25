package com.example.image;


import android.util.Log;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import com.alibaba.fastjson.JSON;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

//该dummy restful站点提供下面格式的数据
// {
//     "date": "2020-04-22",
//     "media_type": "image",
//     "hdurl": "https://apod.nasa.gov/apod/image/2004/ISS002-E-7377_2048c.jpg",
//     "service_version": "v1",
//     "explanation": "No sudden, sharp boundary marks the passage of day into night in this gorgeous view of ocean and clouds over our fair planet Earth. Instead, the shadow line or terminator is diffuse and shows the gradual transition to darkness we experience as twilight. With the Sun illuminating the scene from the right, the cloud tops reflect gently reddened sunlight filtered through the dusty troposphere, the lowest layer of the planet's nurturing atmosphere. A clear high altitude layer, visible along the dayside's upper edge, scatters blue sunlight and fades into the blackness of space. This picture was taken in June of 2001 from the International Space Station orbiting at an altitude of 211 nautical miles. Of course from home, you can check out the Earth Now.  Celebrate: Today is Earth Day",
//     "title": "Planet Earth at Twilight",
//     "url": "https://apod.nasa.gov/apod/image/2004/ISS002-E-7377_1024c.jpg"
// }

public class DummyImage extends ImageRequester<ImageRequesterResponse> {

    private static final String MEDIA_TYPE_IMAGE_VALUE = "image";

    private Escaper pathEscapers = UrlEscapers.urlPathSegmentEscaper();
    private Escaper queryEscapers = UrlEscapers.urlFormParameterEscaper();
    // private Escaper fragmentEscaper= UrlEscapers.urlFragmentEscaper();

    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private OkHttpClient client = new OkHttpClient();
    boolean isLoadingData = false;

    public DummyImage(ImageRequesterResponse responseListener)  {
        super(responseListener);
    }

    @Override
    public void getPhoto() {
        String date = dateFormat.format(calendar.getTime());

////        String urlRequest = String.format("https://api.nasa.gov/%s/%s?date=%s?api_key=%s",
//        String urlRequest = String.format("https://api.nasa.gov/%s/%s?api_key=%s",
//                pathEscapers.escape("planetary"), pathEscapers.escape("apod"),
////                queryEscapers.escape(date),
//                queryEscapers.escape("HUMyibmathd0JhhgpaOxZh8uMgAMLsFfHjPr1oiz"));
        String  urlRequest ="http://192.168.1.146:8080/v2/user/xx";

        okhttp3.Request request = new okhttp3.Request.Builder().url(urlRequest).build();
        isLoadingData = true;

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "e.printStackTrace()");
                isLoadingData = false;
                e.printStackTrace();
            }

            private static final String TAG = "NasaImageRequester";
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                Photo newPhoto = JSON.parseObject(response.body().string(), Photo.class);

                if (newPhoto != null && MEDIA_TYPE_IMAGE_VALUE.equals(newPhoto.getMedia_type()) ) {
                    responseListener.receivedNewPhoto(newPhoto);
                    isLoadingData = false;
                } else {
                    getPhoto();
                }
            }
        });


    }

    @Override
    public boolean isLoadingData() {
        return isLoadingData;
    }

}