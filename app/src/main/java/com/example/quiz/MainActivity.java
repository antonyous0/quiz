package com.example.quiz;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.YuvImage;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import kotlinx.coroutines.channels.ChannelResult;

public class MainActivity extends AppCompatActivity {

    // creating question list
    private final List<QuestionsList>questionsLists = new ArrayList<>();

    private TextView quizTimer;

    private RelativeLayout option1Layout, option2Layout, option3Layout, option4Layout;
    private TextView option1Tv, option2Tv, option3Tv, option4Tv;
    private ImageView option1Icon, option2Icon,option3Icon,option4Icon;
    private TextView questionTV;
    private TextView totalQuestionTV;
    private TextView currentQuestion;

    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://quizapp-ae0b4-default-rtdb.firebaseio.com");


    private CountDownTimer countDownTimer;
    private int currentQuestionPosition = 0;
    private int selectedOption = 0 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quizTimer = findViewById(R.id.quizTimer);
        option1Layout = findViewById(R.id.option1Layout);
        option2Layout = findViewById(R.id.option2Layout);
        option3Layout = findViewById(R.id.option3Layout);
        option4Layout = findViewById(R.id.option4Layout);

        option1Tv = findViewById(R.id.option1Tv);
        option2Tv = findViewById(R.id.option2Tv);
        option3Tv = findViewById(R.id.option3Tv);
        option4Tv = findViewById(R.id.option4Tv);


        option1Icon = findViewById(R.id.option1Icon);
        option2Icon = findViewById(R.id.option2Icon);
        option3Icon = findViewById(R.id.option3Icon);
        option4Icon = findViewById(R.id.option4Icon);

        questionTV = findViewById(R.id.questionTV);
        totalQuestionTV = findViewById(R.id.totalQuestionsTV);
        currentQuestion = findViewById(R.id.currentQuestionTV);

        final AppCompatButton nextBtn = findViewById(R.id.nextQuestionBtn);

        InstructionsDialog instructionsDialog = new InstructionsDialog(MainActivity.this);
        instructionsDialog.setCancelable(false);
        instructionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        instructionsDialog.show();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Fetch quiz time
                String quizTimeString = snapshot.child("time").getValue(String.class);
                int getQuizTime;

                if (quizTimeString == null || quizTimeString.isEmpty()) {
                    // If the "time" key is missing or empty, use a default value
                    Toast.makeText(MainActivity.this, "Quiz time not found. Using default value (120 seconds).", Toast.LENGTH_SHORT).show();
                    getQuizTime = 120; // Default to 120 seconds
                } else {
                    // Parse the quiz time
                    try {
                        getQuizTime = Integer.parseInt(quizTimeString);
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Invalid quiz time format. Using default value (120 seconds).", Toast.LENGTH_SHORT).show();
                        getQuizTime = 120; // Default fallback value
                    }
                }

                // Load questions
                for (DataSnapshot questions : snapshot.child("questions").getChildren()) {
                    String getQuestion = questions.child("question").getValue(String.class);
                    String getOption1 = questions.child("option1").getValue(String.class);
                    String getOption2 = questions.child("option2").getValue(String.class);
                    String getOption3 = questions.child("option3").getValue(String.class);
                    String getOption4 = questions.child("option4").getValue(String.class);
                    String getAnswerString = questions.child("answer").getValue(String.class);

                    if (getQuestion != null && getOption1 != null && getOption2 != null
                            && getOption3 != null && getOption4 != null && getAnswerString != null) {
                        try {
                            int getAnswer = Integer.parseInt(getAnswerString);
                            QuestionsList questionsList =
                                    new QuestionsList(getQuestion, getOption1, getOption2, getOption3, getOption4, getAnswer);
                            questionsLists.add(questionsList);
                        } catch (NumberFormatException e) {
                            Toast.makeText(MainActivity.this, "Invalid answer format for a question.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                // Update UI elements
                totalQuestionTV.setText("/" + questionsLists.size());
                setQuizTimer(getQuizTime);
                selectQuestion(currentQuestionPosition);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to get data from Firebase", Toast.LENGTH_SHORT).show();
            }
        });


        option1Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedOption = 1;
                selectOption(option1Layout, option1Icon);

            }
        });
        option2Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedOption = 2;

                selectOption(option2Layout, option2Icon);

            }
        });
        option3Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedOption = 3;
                selectOption(option3Layout, option3Icon);

            }
        });
        option4Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedOption = 4;
                selectOption(option4Layout, option4Icon);

            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedOption != 0 ){
                    questionsLists.get(currentQuestionPosition).setUserSelectedAnswer(selectedOption);

                    selectedOption = 0;
                    currentQuestionPosition++;

                    if (currentQuestionPosition < questionsLists.size()){
                        selectQuestion(currentQuestionPosition);

                    }
                    else {
                        countDownTimer.cancel();
                        finishQuiz();
                    }

                }
                else {
                    Toast.makeText(MainActivity.this, "Please select an option" , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void finishQuiz(){
        Intent intent = new Intent(MainActivity.this, QuizResult.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("questions",(Serializable) questionsLists);

        intent.putExtras(bundle);
        startActivity(intent);

        finish();
    }

    private void setQuizTimer(int maxTimeInSeconds){
        countDownTimer  = new CountDownTimer(maxTimeInSeconds * 1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                long getHour  = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                long getMinute = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                long getSecond = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

                String generateTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", getHour,
                        getMinute - TimeUnit.HOURS.toMinutes(getHour),
                        getSecond - TimeUnit.MINUTES.toSeconds(getMinute));

                quizTimer.setText(generateTime);

            }

            @Override
            public void onFinish() {

                finishQuiz();

            }
        };

        countDownTimer.start();

    }

    private void selectQuestion(int questionListPosition){
        resetOptions();
        questionTV.setText(questionsLists.get(questionListPosition).getQuestion());
        option1Tv.setText(questionsLists.get(questionListPosition).getOption1());
        option2Tv.setText(questionsLists.get(questionListPosition).getOption2());
        option3Tv.setText(questionsLists.get(questionListPosition).getOption3());
        option4Tv.setText(questionsLists.get(questionListPosition).getOption4());

        currentQuestion.setText("Question"+ (questionListPosition + 1));
    }

    private void resetOptions(){
        option1Layout.setBackgroundResource(R.drawable.round_back_white50_10);
        option2Layout.setBackgroundResource(R.drawable.round_back_white50_10);
        option3Layout.setBackgroundResource(R.drawable.round_back_white50_10);
        option4Layout.setBackgroundResource(R.drawable.round_back_white50_10);


        option1Icon.setImageResource(R.drawable.round_back_withe50_100);
        option2Icon.setImageResource(R.drawable.round_back_withe50_100);
        option3Icon.setImageResource(R.drawable.round_back_withe50_100);
        option4Icon.setImageResource(R.drawable.round_back_withe50_100);
    }

    private void selectOption(RelativeLayout selectedOptionLayout, ImageView selectedOptionIcon){

        resetOptions();

        selectedOptionIcon.setImageResource(R.drawable.check_icon);
        selectedOptionLayout.setBackgroundResource(R.drawable.round_back_selected_option);

    }


}