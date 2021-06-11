package com.andorid.avtranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.andorid.avtranslator.api.Api;
import com.andorid.avtranslator.api.ApiClient;
import com.andorid.avtranslator.api.response.LanguageResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {

    private ImageView imageViewCaptured;
    private Button buttonCapture;
    private TextView textViewResult, textViewLanguage, textViewResultTranslate;
    private Spinner spinnerLanguage;

    private InputImage inputImage;
    private TextRecognizer recognizer;
    private Api api;
    private ProgressDialog progressDialog;

    private ArrayList<String> listSpinner;
    private ArrayList<LanguageResponse.LanguageModel> list;

    private String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageViewCaptured = findViewById(R.id.imageViewCaptured);
        buttonCapture = findViewById(R.id.buttonCapture);
        textViewResult = findViewById(R.id.textViewResult);
        textViewLanguage = findViewById(R.id.textViewLanguage);
        textViewResultTranslate = findViewById(R.id.textViewResultTranslate);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);

        listSpinner = new ArrayList<>();
        recognizer = TextRecognition.getClient();
        api = ApiClient.getClient();
        progressDialog = new ProgressDialog(this);

        buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(CameraActivity.this);
            }
        });

        api.getLanguage().enqueue(new Callback<LanguageResponse>() {
            @Override
            public void onResponse(Call<LanguageResponse> call, Response<LanguageResponse> response) {
                if (response.body() != null) {
                    if (!response.body().error) {
                        list = response.body().data;
                        LanguageResponse.LanguageModel model = new LanguageResponse.LanguageModel();
                        model.code = "00";
                        model.country = "-";
                        list.add(0, model);
                        setSpinnerItem(response.body().data);
                    }
                }
            }

            @Override
            public void onFailure(Call<LanguageResponse> call, Throwable t) {
                Log.e("getLanguage", t.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    inputImage = InputImage.fromFilePath(this, resultUri);
                    recognizer.process(inputImage)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text text) {
                                    detectLanguage(text.getText());
                                    textViewResult.setText(text.getText());
                                    //translate(text.getText());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CameraActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageViewCaptured.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void setSpinnerItem(ArrayList<LanguageResponse.LanguageModel> list) {
        ArrayAdapter<LanguageResponse.LanguageModel> adapter = new ArrayAdapter<LanguageResponse.LanguageModel>(this, android.R.layout.simple_spinner_dropdown_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    progressDialog.setMessage("Please Wait...");
                    progressDialog.show();
                }
                translate(textViewResult.getText().toString(), from, list.get(position).code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void detectLanguage(String s) {
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(s)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                if (languageCode.equals("und")) {
                                    Log.i("detectLanguage", "Can't identify language.");
                                } else {
                                    Log.i("detectLanguage", "Language: " + languageCode);
                                    for (LanguageResponse.LanguageModel model : list) {
                                        if (model.code.contentEquals(languageCode)) {
                                            textViewLanguage.setText(model.country);
                                            from = model.code;
                                        }
                                    }
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("detectLanguage", e.getMessage());
                            }
                        });
    }

    public void translate(String text, String from, String to) {
        // Create an English-German translator:
        if (!to.equalsIgnoreCase("00")) {
            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage(from)
                    .setTargetLanguage(to)
                    .build();
            final Translator translator = Translation.getClient(options);

            DownloadConditions conditions = new DownloadConditions.Builder()
                    .requireWifi()
                    .build();

            translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void v) {
                                    // Model downloaded successfully. Okay to start translating.
                                    // (Set a flag, unhide the translation UI, etc.)
                                    translator.translate(text)
                                            .addOnSuccessListener(
                                                    new OnSuccessListener<String>() {
                                                        @Override
                                                        public void onSuccess(@NonNull String translatedText) {
                                                            // Translation successful.
                                                            Log.e("Translate", translatedText);
                                                            textViewResultTranslate.setText(translatedText);
                                                            if (progressDialog.isShowing()) {
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    })
                                            .addOnFailureListener(
                                                    new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Error.
                                                            // ...
                                                            Log.e("Translate", e.getMessage());
                                                        }
                                                    });
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Model couldn’t be downloaded or other internal error.
                                    // ...
                                    Log.e("Translate", e.getMessage());
                                }
                            });
        } else {
            textViewResultTranslate.setText("-");
        }
        //englishGermanTranslator.close();
    }
}