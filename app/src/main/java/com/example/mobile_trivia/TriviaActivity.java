package com.example.mobile_trivia;

import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.VideoView;

public class TriviaActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_trivia);

        //Creates the video view
        videoView = (VideoView) findViewById(R.id.videoView);
        String videoPath="android.resource://"+getPackageName()+"/"+R.raw.konosuba1;
        videoView.setVideoPath(videoPath);
        videoView.start();

        //loops video on completion
        videoView.setOnCompletionListener ( new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.start();
            }
        });

        Button button = (Button)findViewById(R.id.answer1);
        button.setText("Anime Name");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //prevents the video from re buffering on screen rotate
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void nextSong(View view){
        String videoPath="android.resource://"+getPackageName()+"/"+R.raw.konosuba2;
        videoView.setVideoPath(videoPath);
        videoView.start();

        Button button = (Button)findViewById(R.id.answer1);
        button.setText("Anime Name 2");
    }
}
