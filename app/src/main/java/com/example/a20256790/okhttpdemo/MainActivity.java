package com.example.a20256790.okhttpdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;

import com.example.a20256790.okhttpdemo.bean.County;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @BindView(R.id.btn_get)
    Button btnGet;
    @BindView(R.id.btn_post)
    Button btnPost;
    @BindView(R.id.btn_upload)
    Button btnUpload;
    @BindView(R.id.btn_download)
    Button btnDownload;

    private InputStream is;
    private String weatherResult;
    private String areaId;
    private final String TAG = "zoky";
    final String URL_BAIDU = "https://www.baidu.com";
    final String URL_Weather = "http://res.aider.meizu.com/1.0/weather/%s.json";
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");
    private static final int ID = 0;
    private static final int NAME = 1;
    private static final int WEATHER_CODE = 2;
    private List<County> counties;
    private OkHttpClient okHttpClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        areaId = "101281601";
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(800, TimeUnit.MILLISECONDS)
                .cache(new Cache(getExternalCacheDir().getAbsoluteFile(), 10 * 1024 * 1024))
                .writeTimeout(5, TimeUnit.SECONDS);

        okHttpClient = builder.build();


        btnDownload.setOnClickListener(this);
        btnGet.setOnClickListener(this);
        btnPost.setOnClickListener(this);
        btnUpload.setOnClickListener(this);


    }








    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_get:
                Log.d(TAG,"doget");
                doGet(getWeatherCode("广州"));
                break;
            case R.id.btn_post:
                doPost(getWeatherCode("上海"));
                Log.d(TAG,"post");
                break;
            case R.id.btn_upload:
                doUpload();
                Log.d(TAG,"upload");
                break;
            case R.id.btn_download:
                doDownload();
                Log.d(TAG,"download");
        }
    }

    private String doGet(String areaId) {
        String url = String.format(URL_Weather, areaId);
        Request request = new Request.Builder().url(url).build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "请求服务器失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "请求服务器成功:\n"+response.body().string());
            }
        });
        return weatherResult;
    }

    private String doPost(String areaId) {
        try {
            // 请求完整url：http://api.k780.com:88/?app=weather.future&weaid=101281001&&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json
            String url = "http://api.k780.com:88/";

            RequestBody formBody = new FormBody.Builder().add("app", "weather.future")
                    .add("weaid", areaId).add("appkey", "10003").add("sign",
                            "b59bc3ef6191eb9f747dd4e83c99f2a4").add("format", "json")
                    .build();
            Request request = new Request.Builder().url(url).post(formBody).build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    weatherResult = response.body().string();
                    Log.i(TAG, weatherResult);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return weatherResult;
    }

    private void doUpload() {
        File file = new File("/sdcard/zoky.txt");
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, file))
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"上传失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "上传成功：\n" + response.body().string());
            }
        });
    }
    private void doDownload() {

        String url = "http://avatar.csdn.net/5/8/5/1_zoky_ze.jpg";
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"下载失败");
            }

            @Override
            public void onResponse(Call call, Response response) {
                int len;
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream;
                try {
                    fileOutputStream = new FileOutputStream(new File("/sdcard/zoky.jpg"));
                    byte[] buffer = new byte[2048];

                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                    Log.d(TAG, "文件下载成功"+ response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }


    public  String getWeatherCode(final String CityName) {


        if (counties == null){
            try {
                County county;
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            is = MainActivity.this.getAssets().open("CityId.xml");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }



                }).start();

                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(is,"UTF-8");
                int eventType = parser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT){
                    switch (eventType){
                        case XmlPullParser.START_DOCUMENT:
                            counties = new ArrayList<County>();
                            break;
                        case XmlPullParser.START_TAG:

                            if (parser.getName().equals("county")){

                                county = new County();
                                parser.next();
                                county.setId(parser.getAttributeValue(ID));
                                county.setName(parser.getAttributeValue(NAME));
                                county.setWeatherCode(parser.getAttributeValue(WEATHER_CODE));
                                counties.add(county);

                            }
                            break;
                        case XmlPullParser.END_TAG:
                            break;
                    }
                    eventType = parser.next();
                }
                for (County countyBean : counties) {
                    if (CityName.equals(countyBean.getName())) {
                        areaId = countyBean.getWeatherCode();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                }
        }else {
            for (County countyBean : counties) {
                if (CityName.equals(countyBean.getName())) {
                    areaId = countyBean.getWeatherCode();
                }
            }
        }

        return areaId;
    }
}
