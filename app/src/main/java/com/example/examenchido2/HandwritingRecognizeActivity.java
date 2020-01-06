package com.example.examenchido2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.examenchido2.helper.Imagehelper;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.HandwritingRecognitionOperation;
import com.microsoft.projectoxford.vision.contract.HandwritingRecognitionOperationResult;
import com.microsoft.projectoxford.vision.contract.HandwritingTextLine;
import com.microsoft.projectoxford.vision.contract.HandwritingTextWord;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.rahman.dialog.Activity.SmartDialog;
import com.rahman.dialog.ListenerCallBack.SmartDialogClickListener;
import com.rahman.dialog.Utilities.SmartDialogBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HandwritingRecognizeActivity extends AppCompatActivity {

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The button to select an image
    private Button buttonSelectImage;

    // The URI of the image selected to detect.
    private Uri imagUrl;

    // The image selected to detect.
    private Bitmap bitmap;

    // The edit to show status and result.
    private EditText editText;

    private VisionServiceClient client;

    //max retry times to get operation result
    private int retryCountThreshold = 30;

    private NotaSqliteHelper notaHelper;
    private SQLiteDatabase db;
    private Date currentTime = Calendar.getInstance().getTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__recognize_handwriting);

        if (client == null) {
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }

        buttonSelectImage = (Button) findViewById(R.id.buttonSelectImage);
        editText = (EditText) findViewById(R.id.editTextResult);

        notaHelper = new NotaSqliteHelper(this, "DBTest1", null, 1);
        db = notaHelper.getWritableDatabase();


    }

    public void create(View view) {
        //Si hemos abierto correctamente la base de datos
        if (db != null) {
            //Creamos el registro a insertar como objeto ContentValues
            ContentValues nuevoRegistro = new ContentValues();
            // El ID es auto incrementable como declaramos en nuestro CarsSQLiteHelper
            nuevoRegistro.put("name", editText.getText().toString());
            nuevoRegistro.put("phn", currentTime.toString());

            //Insertamos el registro en la base de datos
            db.insert("Note", null, nuevoRegistro);

            new SmartDialogBuilder(this)
                    .setTitle("Exito")
                    .setSubTitle("Usted ha guardado correctamente la informacion")
                    .setCancalable(false)
                    .setTitleFont(Typeface.SERIF) //set title font
                    .setSubTitleFont(Typeface.SANS_SERIF) //set sub title font
                    .setNegativeButtonHide(true) //hide cancel button
                    .setPositiveButton("OK", new SmartDialogClickListener() {
                        @Override
                        public void onClick(SmartDialog smartDialog) {
                            Toast.makeText(getApplicationContext(), "Aceptado",
                                    Toast.LENGTH_LONG).show();
                            smartDialog.dismiss();
                        }
                    }).setNegativeButton("Cancel", new SmartDialogClickListener() {
                @Override
                public void onClick(SmartDialog smartDialog) {
                    Toast.makeText(getApplicationContext() ,"No se cancelo",Toast.LENGTH_SHORT).show();
                    smartDialog.dismiss();

                }
            }).build().show();
        }
    }


    @Override
    protected void onDestroy() {
        // cerramos conexión base de datos antes de destruir el activity
        db.close();
        super.onDestroy();
    }


    // Called when the "Select Image" button is clicked.
    public void selectImage(View view) {
        editText.setText("");

        Intent intent;
        intent = new Intent(HandwritingRecognizeActivity.this, com.example.examenchido2.SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("AnalyzeActivity", "onActivityResult");
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    imagUrl = data.getData();

                    bitmap = Imagehelper.loadSizeLimitedBitmapFromUri(
                            imagUrl, getContentResolver());
                    if (bitmap != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                        imageView.setImageBitmap(bitmap);

                        // Add detection log.
                        Log.d("AnalyzeActivity", "Image: " + imagUrl + " resized to " + bitmap.getWidth()
                                + "x" + bitmap.getHeight());

                        doRecognize();
                    }
                }
                break;
            default:
                break;
        }
    }


    public void doRecognize() {
        buttonSelectImage.setEnabled(false);
        editText.setText("Analyzing...");

        try {
            new doRequest(this).execute();
        } catch (Exception e) {
            editText.setText("Error encountered. Exception is: " + e.toString());
        }
    }

    private String process() throws VisionServiceException, IOException, InterruptedException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray())) {
                //post image and got operation from API
                HandwritingRecognitionOperation operation = this.client.createHandwritingRecognitionOperationAsync(inputStream);

                HandwritingRecognitionOperationResult operationResult;
                //try to get recognition result until it finished.

                int retryCount = 0;
                do {
                    if (retryCount > retryCountThreshold) {
                        throw new InterruptedException("Can't get result after retry in time.");
                    }
                    Thread.sleep(1000);
                    operationResult = this.client.getHandwritingRecognitionOperationResultAsync(operation.Url());
                }
                while (operationResult.getStatus().equals("NotStarted") || operationResult.getStatus().equals("Running"));

                String result = gson.toJson(operationResult);
                Log.d("result", result);
                return result;

            } catch (Exception ex) {
                throw ex;
            }
        } catch (Exception ex) {
            throw ex;
        }

    }


    private static class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        private WeakReference<HandwritingRecognizeActivity> recognitionActivity;

        public doRequest(HandwritingRecognizeActivity activity) {
            recognitionActivity = new WeakReference<HandwritingRecognizeActivity>(activity);
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                if (recognitionActivity.get() != null) {
                    return recognitionActivity.get().process();
                }
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            if (recognitionActivity.get() == null) {
                return;
            }
            // Display based on error existence
            if (e != null) {
                recognitionActivity.get().editText.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                Gson gson = new Gson();
                HandwritingRecognitionOperationResult r = gson.fromJson(data, HandwritingRecognitionOperationResult.class);

                StringBuilder resultBuilder = new StringBuilder();
                //if recognition result status is failed. display failed
                if (r.getStatus().equals("Failed")) {
                    resultBuilder.append("Error: Recognition Failed");
                } else {
                    for (HandwritingTextLine line : r.getRecognitionResult().getLines()) {
                        for (HandwritingTextWord word : line.getWords()) {
                            resultBuilder.append(word.getText() + " ");
                        }
                        resultBuilder.append("\n");
                    }
                    resultBuilder.append("\n");
                }

                recognitionActivity.get().editText.setText(resultBuilder);
            }
            recognitionActivity.get().buttonSelectImage.setEnabled(true);
        }


    }
}