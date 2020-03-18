package com.nabesh.flagquizapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    //String used for logging error messages
    private static final String TAG = "MainActivity";
    private List<String> fileNameList;
    private List<String> quizCountryList;
    private Map<String, Boolean> regionsMap;
    private String correctAnswer;
    private int totalGuesses;
    private int correctAnswers;
    private int guessRows;
    private Random random;
    private Handler handler; // for delaying next flag
    private Animation shakeAnimation; //animation for incorrect guesses
    private TextView answerTextView;
    private TextView questionNumberTextView;
    private ImageView flagImageView;
    private TableLayout buttonTableLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //instantiating the variables
        fileNameList = new ArrayList<String>();
        quizCountryList = new ArrayList<String>();
        regionsMap = new HashMap<String, Boolean>();
        guessRows = 1;
        random = new Random();
        handler = new Handler();

        //load the shake animation
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3); //animation repeats 3 times

        //get array of world regions from strings.xml
        String[] regionNames = getResources().getStringArray(R.array.regionsList);

        //by default countries are chosen from all regions
        for (String region: regionNames
             ) {
            regionsMap.put(region,true);
        }
        //getting reference to GUI elements
        questionNumberTextView = findViewById(R.id.questionNumberTextView);
        flagImageView = findViewById(R.id.flagImageView);
        buttonTableLayout = findViewById(R.id.buttonTableLayout);
        answerTextView = findViewById(R.id.answerTextView);

        //set questionNumberTextView's text
        questionNumberTextView.setText(getResources().getString(R.string.question) + " 1 "
                + getResources().getString(R.string.of) +" 10");

        resetQuiz();

    }

    //set up and start the next quiz
    private void resetQuiz() {
        //use AssetManager to get the image flag
        //file names for only the enabled regions
        AssetManager assetManager = getAssets();
        fileNameList.clear();

        try {
            Set<String> regions = regionsMap.keySet(); //get set of regions
            for (String region: regions
                 ) {
                if (regionsMap.get(region)){ //if region is enabled
                    //get list of all flag image files in this region
                    String[] paths = assetManager.list(region);

                    for (String path: paths
                         ) {
                        fileNameList.add(path.replace(".png",""));
                    }
                }
            }
        } catch (IOException e) {
//            e.printStackTrace();
            Log.e(TAG, "Error loading image file names", e);
        }
        correctAnswers = 0;
        totalGuesses = 0;
        quizCountryList.clear();

        //Add 10 random file names to the quizCountryList
        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();
        while (flagCounter < 10){
            int randomIndex = random.nextInt(numberOfFlags); //random index

            //get the random file name
            String fileName = fileNameList.get(randomIndex);

            //if region is enabled and it hasn't already been choosen
            if (!quizCountryList.contains(fileName)){
                quizCountryList.add(fileName);
                ++flagCounter;
            }
        }
        loadNextFlag();
    }

    private void loadNextFlag() {
    }
}
