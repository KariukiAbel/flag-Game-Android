package com.nabesh.flagquizapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    //String used for logging error messages
    private static final String TAG = "ManinActivity";
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
    private TextView answetTextView;
    private TextView questionNumberTextView;
    private ImageView flagImageView;
    private TableLayout buttonTableLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
