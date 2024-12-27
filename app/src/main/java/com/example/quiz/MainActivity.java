package com.example.quiz;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class MainActivity extends AppCompatActivity {

    // Firebase authentication
    private FirebaseAuth auth;
    private FirebaseUser user;

    // UI Elements
    private TextView quizTimer, questionTV, totalQuestionTV, currentQuestionTV;
    private RelativeLayout option1Layout, option2Layout, option3Layout, option4Layout;
    private TextView option1Tv, option2Tv, option3Tv, option4Tv;
    private ImageView option1Icon, option2Icon, option3Icon, option4Icon;

    // Quiz data
    private final List<QuestionsList> questionsLists = new ArrayList<>();
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://quizapp-ae0b4-default-rtdb.firebaseio.com");
    private CountDownTimer countDownTimer;
    private int currentQuestionPosition = 0;
    private int selectedOption = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            // Redirect to login if user is not authenticated
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize UI elements
        quizTimer = findViewById(R.id.quizTimer);
        questionTV = findViewById(R.id.questionTV);
        totalQuestionTV = findViewById(R.id.totalQuestionsTV);
        currentQuestionTV = findViewById(R.id.currentQuestionTV);

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


        // Display user email
        //TextView userEmail = findViewById(R.id.userEmail);
        //userEmail.setText(user.getEmail());

        // Logout button functionality

        // Show instructions dialog
        InstructionsDialog instructionsDialog = new InstructionsDialog(MainActivity.this);
        instructionsDialog.setCancelable(false);
        instructionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        instructionsDialog.show();

        // Load quiz data from Firebase
        loadQuizData();

        // Set up option click listeners
        setupOptionClickListeners();

        // Next question button
        findViewById(R.id.nextQuestionBtn).setOnClickListener(v -> handleNextQuestion());
    }

    private void loadQuizData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Fetch quiz time
                String quizTimeString = snapshot.child("time").getValue(String.class);
                int quizTime = parseQuizTime(quizTimeString);

                // Load questions
                for (DataSnapshot questionSnapshot : snapshot.child("questions").getChildren()) {
                    String question = questionSnapshot.child("question").getValue(String.class);
                    String option1 = questionSnapshot.child("option1").getValue(String.class);
                    String option2 = questionSnapshot.child("option2").getValue(String.class);
                    String option3 = questionSnapshot.child("option3").getValue(String.class);
                    String option4 = questionSnapshot.child("option4").getValue(String.class);
                    String answerString = questionSnapshot.child("answer").getValue(String.class);

                    if (isValidQuestion(question, option1, option2, option3, option4, answerString)) {
                        int answer = Integer.parseInt(answerString);
                        questionsLists.add(new QuestionsList(question, option1, option2, option3, option4, answer));
                    }
                }

                // Initialize quiz UI
                totalQuestionTV.setText("/" + questionsLists.size());
                setQuizTimer(quizTime);
                selectQuestion(currentQuestionPosition);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to get data from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int parseQuizTime(String quizTimeString) {
        if (quizTimeString == null || quizTimeString.isEmpty()) {
            Toast.makeText(this, "Quiz time not found. Using default value (120 seconds).", Toast.LENGTH_SHORT).show();
            return 120;
        }
        try {
            return Integer.parseInt(quizTimeString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quiz time format. Using default value (120 seconds).", Toast.LENGTH_SHORT).show();
            return 120;
        }
    }

    private boolean isValidQuestion(String question, String option1, String option2, String option3, String option4, String answerString) {
        return question != null && option1 != null && option2 != null && option3 != null && option4 != null && answerString != null;
    }

    private void setQuizTimer(int maxTimeInSeconds) {
        countDownTimer = new CountDownTimer(maxTimeInSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                quizTimer.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                finishQuiz();
            }
        };
        countDownTimer.start();
    }

    private void setupOptionClickListeners() {
        option1Layout.setOnClickListener(v -> selectOption(1, option1Layout, option1Icon));
        option2Layout.setOnClickListener(v -> selectOption(2, option2Layout, option2Icon));
        option3Layout.setOnClickListener(v -> selectOption(3, option3Layout, option3Icon));
        option4Layout.setOnClickListener(v -> selectOption(4, option4Layout, option4Icon));
    }

    private void handleNextQuestion() {
        if (selectedOption == 0) {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
            return;
        }
        questionsLists.get(currentQuestionPosition).setUserSelectedAnswer(selectedOption);
        selectedOption = 0;
        currentQuestionPosition++;
        if (currentQuestionPosition < questionsLists.size()) {
            selectQuestion(currentQuestionPosition);
        } else {
            countDownTimer.cancel();
            finishQuiz();
        }
    }

    private void finishQuiz() {
        Intent intent = new Intent(MainActivity.this, QuizResult.class);
        intent.putExtra("questions", (Serializable) questionsLists);
        startActivity(intent);
        finish();
    }

    private void selectQuestion(int questionPosition) {
        resetOptions();
        QuestionsList currentQuestion = questionsLists.get(questionPosition);
        questionTV.setText(currentQuestion.getQuestion());
        option1Tv.setText(currentQuestion.getOption1());
        option2Tv.setText(currentQuestion.getOption2());
        option3Tv.setText(currentQuestion.getOption3());
        option4Tv.setText(currentQuestion.getOption4());
        currentQuestionTV.setText("Question " + (questionPosition + 1));
    }

    private void resetOptions() {
        resetOptionStyle(option1Layout, option1Icon);
        resetOptionStyle(option2Layout, option2Icon);
        resetOptionStyle(option3Layout, option3Icon);
        resetOptionStyle(option4Layout, option4Icon);
    }

    private void resetOptionStyle(RelativeLayout layout, ImageView icon) {
        layout.setBackgroundResource(R.drawable.round_back_white50_10);
        icon.setImageResource(R.drawable.round_back_withe50_100);
    }

    private void selectOption(int option, RelativeLayout layout, ImageView icon) {
        resetOptions();
        selectedOption = option;
        layout.setBackgroundResource(R.drawable.round_back_selected_option);
        icon.setImageResource(R.drawable.check_icon);
    }
}
