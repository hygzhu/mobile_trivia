package com.example.mobile_trivia;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

    private String currentVideoFile;
    private String animeName;
    private String previousAnimeName;
    private String guess;
    private List<String> otherAnimes = new ArrayList<>();
    private int[] buttonIDs = new int[] {R.id.answer1, R.id.answer2, R.id.answer3, R.id.answer4};
    private int score;
    private int lives;
    private int songsPlayed;

    private boolean endless;
    private boolean visible;

    private final int options = 4;


    PlayerView playerView;
    boolean playWhenReady = true;
    int currentWindow = 0;
    SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);

        //load settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        endless = sharedPref.getBoolean("endless_mode_switch", false);
        visible = sharedPref.getBoolean("visible_mode_switch", true);

        if(endless){
            lives = -1;
            TextView livesView = findViewById(R.id.lives_display);
            livesView.setText("Lives: Infinite");

            TextView modeView = findViewById(R.id.mode_display);
            modeView.setText("Mode: Endless");
        }else{
            lives = 3;
        }

        score = 0;
        songsPlayed = 0;

        previousAnimeName = "N/A";
        animeName = "N/A";
        guess = "N/A";

        playerView = findViewById(R.id.videoView);
        setUpTrivia();
    }


    public void backToMenu(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void viewDetailsDialog(View view){
        AlertDialog alertDialog = new AlertDialog.Builder(TriviaActivity.this).create();
        alertDialog.setTitle("Details");
        alertDialog.setMessage("Previous Anime: "+ previousAnimeName + "\nYou guessed: "+ guess);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void nextSong(View view){

        guess = ((Button) view).getText().toString();
        previousAnimeName = animeName;
        //Create next trivia question
        if(guess.equals(animeName)){
            score++;
        }else {
            lives--;
            if (lives <= 0 && !endless) {
                Intent intent = new Intent(this, ScoreActivity.class);
                intent.putExtra("SCORE", Integer.toString(score));
                startActivity(intent);
            }
        }
        songsPlayed++;

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.before_layout);
        linearLayout.setVisibility(View.GONE);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.after_layout);
        relativeLayout.setVisibility(View.VISIBLE);

        TextView status_text = findViewById(R.id.status_text);
        if(guess.equals(animeName)){
            status_text.setText("Correct");
        }else{
            status_text.setText("Incorrect");
        }

        TextView after_details_text = findViewById(R.id.after_details_text);
        after_details_text.setText("Anime name: \n"+ previousAnimeName + "\nYou guessed: \n"+ guess);
    }

    public void continuePlay(View view){


        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.after_layout);
        relativeLayout.setVisibility(View.GONE);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.before_layout);
        linearLayout.setVisibility(View.VISIBLE);

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
        }
        return json;
    }

    //Creates video view and updates buttons with random anime
    private void setUpTrivia(){

        //update values
        TextView scoreView = findViewById(R.id.score_display);
        scoreView.setText("Score: " + score);
        TextView songNumberView = findViewById(R.id.song_number_display);
        songNumberView.setText("Songs Played: " + songsPlayed);
        if(!endless){
            TextView livesView = findViewById(R.id.lives_display);
            livesView.setText("Lives: " + lives);
        }
        //Gets random anime
        String jsonString = openJSONResource();
        try{
            final JSONArray obj = new JSONArray(jsonString);

            //gets the video to display
            int random = new Random().nextInt(obj.length());
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
                Button button = findViewById(buttonIDs[i]);
                button.setText(newAnimeName);
            }

            //randomly place the correct answer in one of the buttons
            random = new Random().nextInt(options);
            Button button = findViewById(buttonIDs[random]);
            button.setText(animeName);

        } catch (Exception e) {
            //should probably catch this
            e.printStackTrace();
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

        if(!visible){
            //turns off video, leaves on audio
            playerView.setAlpha(0f);
        }
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
