package com.example.mobile_trivia;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //sets background image
        ImageView img = findViewById(R.id.background);
        int rand_image = 1 + (int)(Math.random() * ((359 - 1) + 1));
        String mDrawableName = "background_" + rand_image;
        int resID = getResources().getIdentifier(mDrawableName , "drawable", getPackageName());
        img.setImageResource(resID);
    }

    public void startTrivia(View view){
        Intent intent = new Intent(this, TriviaActivity.class);
        startActivity(intent);
    }

    public void openSettings(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
