package com.example.mobile_trivia;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TriviaActivity extends AppCompatActivity {

    private VideoView videoView;
    private String currentVideoFile;
    private String animeName;
    private String guess;
    private int animeIndex;
    private List<String> otherAnimes = new ArrayList<>();;
    private int[] buttonIDs = new int[] {R.id.answer1, R.id.answer2, R.id.answer3, R.id.answer4};
    private int score;
    private int lives;

    private final int options = 4;


    PlayerView playerView;
    boolean playWhenReady = true;
    int currentWindow = 0;
    long playbackPosition = 0;
    SimpleExoPlayer player;
    String link = "http://openings.moe/video/Ending2-Konosuba.webm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);


        score = 0;
        lives = 3;
        animeName = "none";
        guess = "none";

        playerView = findViewById(R.id.videoView);
        setUpTrivia();
    }


    public void nextSong(View view){

        guess = ((Button) view).getText().toString();
        //Create next trivia question
        if(guess == animeName){
            score++;
        }else {
            lives--;
            if (lives <= 0) {
                Intent intent = new Intent(this, ScoreActivity.class);
                intent.putExtra("SCORE", Integer.toString(score));
                startActivity(intent);
            }
        }
        player.release();
        setUpTrivia();
    }

    private String openJSONResource(){
        String json = "";
        InputStream is = this.getResources().openRawResource(R.raw.animelist);
        try{
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (Exception e){
            //should probably catch this
        } finally {

        }
        return json;
    }

    //Creates video view and updates buttons with random anime
    private void setUpTrivia(){

        //update lives and score
        TextView scoreView = (TextView)findViewById(R.id.score_display);
        scoreView.setText("Score: " + score);
        TextView livesView = (TextView)findViewById(R.id.lives_display);
        livesView.setText("Lives: " + lives);
        TextView previousView = (TextView)findViewById(R.id.previous_anime);
        previousView.setText("Previous Anime: " + animeName);
        TextView guessView = (TextView)findViewById(R.id.previous_guess);
        guessView.setText("You guessed: " + guess);

        //Gets random anime
        String jsonString = openJSONResource();
        try{
            final JSONArray obj = new JSONArray(jsonString);

            //gets the video to display
            int random = new Random().nextInt(obj.length());
            animeIndex = random;
            currentVideoFile = "http://openings.moe/video/" + ((JSONObject)obj.get(random)).get("file");
            animeName = ((JSONObject)obj.get(random)).get("source").toString();

            //Generate 4 other random anime names and add the names to the buttons
            for (int i = 0; i < options; i++){

                int newRand = new Random().nextInt(obj.length());


                String newAnimeName = ((JSONObject)obj.get(newRand)).get("source").toString();
                //loops until unique options are selected
                if(random == newRand || otherAnimes.contains(newAnimeName)){
                    newRand = new Random().nextInt(obj.length());
                    newAnimeName = ((JSONObject)obj.get(newRand)).get("source").toString();
                }

                otherAnimes.add(newAnimeName);
                Button button = (Button)findViewById(buttonIDs[i]);
                button.setText(newAnimeName);
            }

            //randomly place the correct answer in one of the buttons
            random = new Random().nextInt(options - 1);
            Button button = (Button)findViewById(buttonIDs[random]);
            button.setText(animeName);

        } catch (Exception e) {
            //should probably catch this
            e.printStackTrace();
        } finally {
        }

        initializePlayer();
    }

    private void initializePlayer() {

        if(player != null){
            player.release();
        }

        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(), new DefaultLoadControl());

        playerView.setPlayer(player);
        player.setPlayWhenReady(playWhenReady);
        player.setRepeatMode(Player.REPEAT_MODE_ONE);

        Uri uri = Uri.parse(currentVideoFile);
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource, true, false);
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                createMediaSource(uri);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }


}
