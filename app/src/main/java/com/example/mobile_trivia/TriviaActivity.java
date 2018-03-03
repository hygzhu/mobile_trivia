package com.example.mobile_trivia;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;

public class TriviaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);

        final VideoView videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVideoPath("http://openings.moe/video/Ending1-Konosuba.webm");
        videoView.start();
    }
}
