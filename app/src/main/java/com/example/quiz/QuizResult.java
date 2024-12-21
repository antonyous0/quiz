package com.example.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class QuizResult extends AppCompatActivity {
    private List<QuestionsList> questionsLists = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz_result);

        final TextView scoreTV = findViewById(R.id.scoreTV);
        final TextView totalScoreTV = findViewById(R.id.totalScoreTV);
        final TextView correctTV = findViewById(R.id.correctTV);
        final TextView incorrectTV = findViewById(R.id.inCorrectTV);
        final AppCompatButton shareBtn = findViewById(R.id.shareBtn);
        final AppCompatButton reTakeBtn = findViewById(R.id.reTakeQuizBtn);

        questionsLists =(List<QuestionsList>) getIntent().getSerializableExtra("questions");
        totalScoreTV.setText("/"+questionsLists.size());
        scoreTV.setText(getCorrectAnswers()+"");
        correctTV.setText(getCorrectAnswers() + "");
        incorrectTV.setText(String.valueOf(questionsLists.size() - getCorrectAnswers()));

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sendIntent  = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "My Score  = " + scoreTV.getText());

                Intent shareIntent = Intent.createChooser(sendIntent,"Share Via");
                startActivity(shareIntent);


            }
        });

        reTakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(QuizResult.this, MainActivity.class ));
                finish();
            }
        });
    }

    private int getCorrectAnswers(){
        int correctAnswer = 0;
        for (int i = 0 ; i < questionsLists.size() ; i++){
            int getUserSelectedOption = questionsLists.get(i).getUserSelectedAnswer();
            int getQuestionAnswer = questionsLists.get(i).getAnswer();
            if (getQuestionAnswer == getUserSelectedOption){
                correctAnswer++;
            }
        }

        return correctAnswer;
    }
}