package com.studyandroid.myyoutubechannel.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.studyandroid.myyoutubechannel.R;
import com.studyandroid.myyoutubechannel.Utility.Util;
import com.studyandroid.myyoutubechannel.controller.VideoAdapter;
import com.studyandroid.myyoutubechannel.model.VideoModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public static final String API_KEY = "AIzaSyCNI-afpXz5tf64BaU5Iwi4t160dG-2dXo";
    private ListView lstVideo;
    private ProgressBar prgLoading;
    private VideoAdapter adapter;
    private ArrayList<VideoModel> arrVideo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // attaching layout xml
        setContentView(R.layout.activity_main);

        // find views
        lstVideo = (ListView) findViewById(R.id.lst_video);
        prgLoading = (ProgressBar) findViewById(R.id.prg_loading);
        // load data from server
        getData();

        lstVideo.setOnItemClickListener(this);
    }

    private void getData() {
        //show progress bar
        prgLoading.setVisibility(View.VISIBLE);

        // get data from URL
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                // get data from URL
                String channelId = "UCFy-4UvcY7Jbie_Ei8qd0nw";
                String url = "https://www.googleapis.com/youtube/v3/search?key="
                            + API_KEY
                            + "&channelId="
                            + channelId
                            + "&part=snippet,id&order=date&maxResults=50";
                String result = Util.getDataFromURL(url);
                Message msg = new Message();
                msg.obj = result;
                // send message to handler
                networkingHandler.sendMessage(msg);
            }
        };
        thread.start();
    }

    // Handler
    Handler networkingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String result = msg.obj.toString();

            // hide progress bar
            prgLoading.setVisibility(View.GONE);
            lstVideo.setVisibility(View.VISIBLE);
            // get arraylist country
            ArrayList<VideoModel> arr = parserData(result);

            // update total country
            arrVideo.addAll(arr);
            if(adapter == null) {
                adapter = new VideoAdapter(MainActivity.this, arrVideo);
                lstVideo.setAdapter(adapter);

            }else
            {
                adapter.notifyDataSetChanged();
            }
        }
    };
    // parser data from String --> Object
    private ArrayList<VideoModel> parserData(String result) {
        ArrayList<VideoModel> arr = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(result);

            // get array videos
            JSONArray arrVideo = (JSONArray) root.getJSONArray("items");
            for (int i = 0; i < arrVideo.length(); i++) {
                JSONObject video = (JSONObject) arrVideo.get(i);
                VideoModel model = new VideoModel();
                // create video model if this item is video
                if(video.getJSONObject("id").has("videoId") == true) {
                    model.id = video.getJSONObject("id").getString("videoId");
                    JSONObject snippet = (JSONObject) video.getJSONObject("snippet");
                    model.name = snippet.getString("title");
                    model.description = snippet.getString("description");
                    model.uploadTime = snippet.getString("publishedAt");

                    // thumbnails
                    JSONObject thumbnails = (JSONObject) snippet.getJSONObject("thumbnails");
                    model.imgThumbDefault = ((JSONObject) thumbnails.getJSONObject("default")).getString("url");
                    model.imgThumbMedium = ((JSONObject) thumbnails.getJSONObject("medium")).getString("url");
                    model.imgThumbHigh = ((JSONObject) thumbnails.getJSONObject("high")).getString("url");

                    // add model to arraylist
                    arr.add(model);
                }
            }
            return arr;

        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+arrVideo.get(position).id)));
        Log.i("Video", "Video Playing....");

    }
}
