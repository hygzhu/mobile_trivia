package com.example.mobile_trivia;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TriviaActivity extends AppCompatActivity {

    private VideoView videoView;
    private String currentVideoFile;
    private String animeName;
    private int animeIndex;
    private List<String> otherAnimes = new ArrayList<>();;
    private int[] buttonIDs = new int[] {R.id.answer1, R.id.answer2, R.id.answer3, R.id.answer4};
    private int score;
    private int lives;

    private final int options = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        score = 0;
        lives = 3;

        setUpTrivia();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //prevents the video from re buffering on screen rotate
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void nextSong(View view){
        //Create next trivia question
        if(((Button) view).getText() == animeName){
            score++;
        }else{
            lives--;
            if(lives <= 0){
                Intent intent = new Intent(this, ScoreActivity.class);
                intent.putExtra("SCORE", Integer.toString(score));
                startActivity(intent);
            }
        }
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

        //Creates the video view
        videoView = (VideoView) findViewById(R.id.videoView);
        //String videoPath="android.resource://"+getPackageName()+"/"+R.raw.konosuba1;
        String videoPath = currentVideoFile;
        videoView.setVideoPath(videoPath);
        videoView.start();

        //loops video on completion
        videoView.setOnCompletionListener ( new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.start();
            }
        });
    }
}
