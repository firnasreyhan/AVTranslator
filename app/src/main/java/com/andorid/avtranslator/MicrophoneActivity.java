package com.andorid.avtranslator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MicrophoneActivity extends AppCompatActivity {

    private TextView textViewResult;
    private Button buttonSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microphone);

        textViewResult = findViewById(R.id.textViewResult);
        buttonSpeech = findViewById(R.id.buttonSpeech);

        buttonSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "jv");
                Toast.makeText(MicrophoneActivity.this, Locale.getDefault().getCountry(), Toast.LENGTH_SHORT).show();
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi Speak Something");

                try {
                    startActivityForResult(intent, 1);
                } catch (ActivityNotFoundException e) {
                    Log.e("ErrorIntent", e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                textViewResult.setText(result.get(0));
            }
        }
    }
}