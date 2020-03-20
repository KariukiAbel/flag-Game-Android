package com.nabesh.flagquizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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
        //get file name of the next flag and remove it from the list
        String nextImageName = quizCountryList.remove(0);
        correctAnswer = nextImageName; //update the correct answer
        answerTextView.setText("");

        //display the number of current question in the quiz
        questionNumberTextView.setText(getResources().getString(R.string.question)
                + " " +(correctAnswers + 1) + " " + getResources().getString(R.string.of) + " 10");

        //extract the region from the next image's name
        String region = nextImageName.substring(0, nextImageName.indexOf('-'));

        //use AssetManager to load next image from assets folder
        AssetManager assetManager = getAssets();
        InputStream stream; //used to read in flag names

        try {
            //get an inputStream to the asset representing the next flag
            stream = assetManager.open(region + "/" + nextImageName + ".png");

            //load the asset as a Drawable and display on the flagImageView
            Drawable flag = Drawable.createFromStream(stream,nextImageName);
            flagImageView.setImageDrawable(flag);

        } catch (IOException e) {
//            e.printStackTrace();
            Log.e(TAG,"Error loading " +nextImageName,e);
        }

        //Clear all prior answer Buttons from TableRows
        for (int row = 0; row < buttonTableLayout.getChildCount(); ++row)
            ((TableRow) buttonTableLayout.getChildAt(row)).removeAllViews();

            Collections.shuffle(fileNameList);

            //put the correct answers to the end of the fileNameList
            int correct = fileNameList.indexOf(correctAnswer);
            fileNameList.add(fileNameList.remove(correct));

            //get reference to the LayoutInflater service
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //add 3,6 or 9 answer buttons based on the value of guessRows
            for (int row = 0; row < guessRows; row++){
                TableRow currentTableRow = getTableRow(row);

                //place buttons in currentTableRow
                for (int column = 0; column < 3; column ++){

                    //inflate guess_button.xml to create new Button
                    Button newGuessButton = (Button) inflater.inflate(R.layout.guess_button, null);

                    //get country name and set it as newGuessButton's text
                    String fileName = fileNameList.get((row*3) + column);
                    newGuessButton.setText(getCountryName(fileName));

                    //register answerButtonListener to respond to Button clicks
                    newGuessButton.setOnClickListener(newGuessButtonListener);
                    currentTableRow.addView(newGuessButton);
                }
            }

            //randomly replace one button with the correct answer
            int row = random.nextInt(guessRows);
            int column = random.nextInt(3);
            TableRow randomTableRow = getTableRow(row);
            String countryName = getCountryName(correctAnswer);
        ((Button)randomTableRow.getChildAt(column)).setText(countryName);
        }

    //parses the country flag file name and returns the country name
    private String getCountryName(String name) {
        return name.substring(name.indexOf("-") + 1).replace('_',' ');
    }

    //returns a specified TableRow
    private TableRow getTableRow(int row) {
        return (TableRow)buttonTableLayout.getChildAt(row);
    }

  public View.OnClickListener newGuessButtonListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
          submitGuess((Button) view);
      }
  };

    //called when the user submits an answer
    private void submitGuess(Button guessButton) {
        String guess = guessButton.getText().toString();
        String answer = getCountryName(correctAnswer);
        ++totalGuesses; //increment the number of guesses the user has made

        if (guess.equals(answer)){
            ++correctAnswers;

            //display correct in green colour
            answerTextView.setText(answer + "!");
            answerTextView.setTextColor(getResources().getColor(R.color.correct_answe ));
            disableButtons();

            if (correctAnswers == 10){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.reset_quiz);

                //set the AlertDialog's message to display game results
                builder.setMessage(String.format("%d %s, %.02f%% %s",totalGuesses,
                        getResources().getString(R.string.guesses), (1000 / (double) totalGuesses),
                        getResources().getString(R.string.correct)));
                builder.setCancelable(false);

                //add reset quiz button
                builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resetQuiz();
                    }
                });
                AlertDialog resetDialog = builder.create();
                resetDialog.show();
            }else{
                //load next flag after 1- second delay
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextFlag();
                    }
                }, 1000);
            }
        }else{
            //play animation
            flagImageView.startAnimation(shakeAnimation);

            //display incorrect in red
            answerTextView.setText(R.string.incorrect_answer);
            answerTextView.setTextColor(getResources().getColor(R.color.incorect_answer));
            guessButton.setEnabled(false);
        }
    }

    //utility method that disables all answer buttons
    private void disableButtons() {
        for (int row = 0; row < buttonTableLayout.getChildCount(); ++row){
            TableRow tableRow = (TableRow) buttonTableLayout.getChildAt(row);
            for (int i = 0; i < tableRow.getChildCount(); ++i)
                tableRow.getChildAt(i).setEnabled(false);
        }
    }
    //constants for each menu id
    private final int CHOICES_MENU_ID = Menu.FIRST;
    private final int REGIONS_MENU_ID = Menu.FIRST + 1;

    //called when the user accesses the optins menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //add two options to the menu
        menu.add(Menu.NONE, CHOICES_MENU_ID, Menu.NONE,R.string.choices);
        menu.add(Menu.NONE, REGIONS_MENU_ID, Menu.NONE, R.string.regions);

        return true;
    }

    //called when the user selects an option from the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case CHOICES_MENU_ID:
                //create a list of the possible numbers of answer choices
                final String[] possibleChoices = getResources().getStringArray(R.array.guessesList);

                AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(this);
                choiceBuilder.setTitle(R.string.choices);
                choiceBuilder.setItems(R.array.guessesList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //update guessRows to match user's choice
                        guessRows = Integer.parseInt(possibleChoices[i].toString()) / 3;
                        resetQuiz();
                    }
                });

                AlertDialog choicesDialog = choiceBuilder.create();
                choicesDialog.show();
                return true;

            case REGIONS_MENU_ID:
                //get array of world regions
                final String[] regionNames = regionsMap.keySet().toArray(new String[regionsMap.size()]);

//                boolean array representing whether each region is enabled
                boolean[] regionsEnabled = new boolean[regionsMap.size()];
                for (int i = 0; i < regionsEnabled.length; ++i) {
                    regionsEnabled[i] = regionsMap.get(regionNames[i]);
                }

                AlertDialog.Builder regionsBuilder = new AlertDialog.Builder(this);
                regionsBuilder.setTitle(R.string.regions);

                //replace _ with space in region names for display purposes
                String[] displayNames = new String[regionNames.length];
                for (int i = 0; i < regionNames.length; ++i) {
                    displayNames[i] = regionNames[i].replace("_", " ");

                    //add displayNames to the Dialog and set the behaviour when one of the item is clicked
                    regionsBuilder.setMultiChoiceItems(displayNames,
                            regionsEnabled,
                            new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                            //include or exclude the clicked region depending on whether or not it's checked
                            regionsMap.put(regionNames[i].toString(),b);
                        }
                    });

                    //rest quiz when user presses the "Reset quiz" button
                    regionsBuilder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            resetQuiz();
                        }
                    });
                    AlertDialog regionDialog = regionsBuilder.create();
                    regionDialog.show();
                    return true;
                }
        }

        return super.onOptionsItemSelected(item);

    }
}
