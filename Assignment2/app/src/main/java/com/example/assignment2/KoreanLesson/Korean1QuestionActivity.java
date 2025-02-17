package com.example.assignment2.KoreanLesson;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignment2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class Korean1QuestionActivity extends AppCompatActivity {
    public static final String EXTRA_SCORE = "extraScore";
    private static final long COUNTDOWN_IN_MILLIS = 30000;

    private static final String KEY_SCORE = "keyScore";
    private static final String KEY_QUESTION_COUNT = "keyQuestionCount";
    private static final String KEY_MILLIS_LEFT = "keyMillisLeft";
    private static final String KEY_ANSWERED = "keyAnswered";
    private static final String KEY_QUESTION_LIST = "keyQuestionList";

    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;

    private TextView textViewResult;
    private TextView textViewDifficulty;
    private TextView textViewCountDown;
    private RadioGroup rbGroup;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;
    private Button buttonConfirmNext;

    private ColorStateList textColorDefaultRb;
    private ColorStateList getTextColorDefaultCd;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    private ArrayList<Korean1Question> questionList;
    private int questionCounter;
    private int questionCountTotal;
    private Korean1Question currentQuestion;

    private int score;
    private boolean answered;

    private long backPressedTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_quiz_layout);
        textViewResult = findViewById(R.id.text_view_result);
        textViewQuestion = findViewById(R.id.text_view_question);
        textViewScore = findViewById(R.id.text_view_score);
        textViewQuestionCount = findViewById(R.id.text_view_question_count);

        textViewDifficulty = findViewById(R.id.text_view_difficulty);
        textViewCountDown = findViewById(R.id.text_view_countdown);
        rbGroup = findViewById(R.id.radio_group);
        rb1 = findViewById(R.id.radio_button1);
        rb2 = findViewById(R.id.radio_button2);
        rb3 = findViewById(R.id.radio_button3);
        buttonConfirmNext = findViewById(R.id.button_confirm_next);

        textColorDefaultRb = rb1.getTextColors();
        getTextColorDefaultCd = textViewCountDown.getTextColors();

        // Get difficulty level from intent
        Intent intent = getIntent();
        String difficulty = intent.getStringExtra(Korean1Activity.EXTRA_DIFFICULTY);

        textViewDifficulty.setText("Difficulty: " + difficulty);


        if (savedInstanceState == null) {
            // Initialize QuestionDatabase class
            Korean1QuestionDatabase dbHelper = new Korean1QuestionDatabase(this);
            // Fill the questionList with data
            questionList = dbHelper.getQuestions(difficulty);
            questionCountTotal = questionList.size();
            // Shuffle the questions to be in random order on create
            Collections.shuffle(questionList);

            showNextQuestion();

            // Retrieve the values from the savedInstanceState
        } else {
            questionList = savedInstanceState.getParcelableArrayList(KEY_QUESTION_LIST);
            questionCountTotal = questionList.size();
            questionCounter = savedInstanceState.getInt(KEY_QUESTION_COUNT);
            currentQuestion = questionList.get(questionCounter - 1);
            score = savedInstanceState.getInt(KEY_SCORE);
            timeLeftInMillis = savedInstanceState.getLong(KEY_MILLIS_LEFT);
            answered = savedInstanceState.getBoolean(KEY_ANSWERED);
            if (!answered) {
                startCountDown();
            } else {
                updateCountDownText();
                showSolution();
            }
        }
        buttonConfirmNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the user has selected a radio button before checking the answer
                if (!answered) {
                    if (rb1.isChecked() || rb2.isChecked() || rb3.isChecked()) {
                        checkAnswer();
                    } else {
                        Toast.makeText(Korean1QuestionActivity.this, "Please select an answer", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showNextQuestion();
                }
            }
        });
    }

    private void showNextQuestion() {
        // Reset the text color of the radio buttons
        rb1.setTextColor(textColorDefaultRb);
        rb2.setTextColor(textColorDefaultRb);
        rb3.setTextColor(textColorDefaultRb);
        // Clear the radio selection
        rbGroup.clearCheck();

        // If there are still questions left, proceed to the next one, if not finish the activity then return to home
        if (questionCounter < questionCountTotal) {
            currentQuestion = questionList.get(questionCounter);
            // Set questions to textView, and answer options to radio buttons
            textViewQuestion.setText(currentQuestion.getQuestion());
            rb1.setText(currentQuestion.getOption1());
            rb2.setText(currentQuestion.getOption2());
            rb3.setText(currentQuestion.getOption3());

            // Increment
            questionCounter++;
            // Set the textView to show the current total number of questions
            textViewQuestionCount.setText("Question: " + questionCounter + "/" + questionCountTotal);
            textViewResult.setText("");
            answered = false;
            buttonConfirmNext.setText("Choose");

            timeLeftInMillis = COUNTDOWN_IN_MILLIS;
            startCountDown();
        } else {
            // Finish the activity when there is no question left
            finishQuestion();
        }
    }
    // Method to start CountdownTimer, interval of 1 sec
    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }
            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDownText();
                checkAnswer();
            }
        }.start();
    }
    // Configure the format of the countdown to display in the textView
    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textViewCountDown.setText(timeFormatted);
        if (timeLeftInMillis < 10000) {
            textViewCountDown.setTextColor(Color.RED);
        } else {
            textViewCountDown.setTextColor(getTextColorDefaultCd);
        }
    }
    // Method to check the correct answer number with the radio button selected
    private void checkAnswer() {
        answered = true;

        countDownTimer.cancel();

        RadioButton rbSelected = findViewById(rbGroup.getCheckedRadioButtonId());
        int answerNr = rbGroup.indexOfChild(rbSelected) + 1;

        // If correct, increment score
        if (answerNr == currentQuestion.getAnswerNr()) {
            textViewResult.setText("Correct!");
            score++;
            textViewScore.setText("Points earned: " + score);
            final MediaPlayer mp = MediaPlayer.create(this,R.raw.correct);
            mp.start();
        }
        // Else show the solution
        else{
            textViewResult.setText("Wrong!");
            final MediaPlayer mp = MediaPlayer.create(this,R.raw.error);
            mp.start();
            showSolution();
        }
    }
    // Method to display the correct answer
    private void showSolution() {
        rb1.setTextColor(Color.YELLOW);
        rb2.setTextColor(Color.YELLOW);
        rb3.setTextColor(Color.YELLOW);

        int showAnswer = currentQuestion.getAnswerNr();

        switch (currentQuestion.getAnswerNr()) {
            case 1:
                rb1.setTextColor(Color.BLACK);
                textViewQuestion.setText("The correct answer is number " + showAnswer);
                break;
            case 2:
                rb2.setTextColor(Color.BLACK);
                textViewQuestion.setText("The correct answer is number " + showAnswer);
                break;
            case 3:
                rb3.setTextColor(Color.BLACK);
                textViewQuestion.setText("The correct answer is number " + showAnswer);
                break;
        }
        if (questionCounter < questionCountTotal) {
            buttonConfirmNext.setText("Next");
        } else {
            buttonConfirmNext.setText("Submit");
        }
    }
    // Method to finish the activity then return to the home screen, passing the score that user gets
    private void finishQuestion() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SCORE, score);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    // Method allowing the user to quit the activity within the specified time frame
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finishQuestion();
        } else {
            Toast.makeText(this, "Press back again to back to home", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    // Save selected data into the bundle to restore later
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SCORE, score);
        outState.putInt(KEY_QUESTION_COUNT, questionCounter);
        outState.putLong(KEY_MILLIS_LEFT, timeLeftInMillis);
        outState.putBoolean(KEY_ANSWERED, answered);
        outState.putParcelableArrayList(KEY_QUESTION_LIST, questionList);
    }
    // When the activity is closed, stop the countdownTimer
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
