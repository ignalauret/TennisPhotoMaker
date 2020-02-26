package com.example.tennisphotomaker.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.example.tennisphotomaker.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.tennisphotomaker.Utils.BitmapUtils.*;
import static com.example.tennisphotomaker.Utils.Constants.*;

public class MainActivity extends AppCompatActivity {

    /* Layout elements */
    EditText mPlayerName1;
    EditText mPlayerName2;
    EditText mResult;
    Button mSelectPhoto;
    Button mCreatePhoto;
    Switch mPositionSwitch;
    Spinner mRoundSpinner;

    /* Images */
    Bitmap mPickedImage;
    Bitmap mResultDraw;
    Bitmap mFinalImage;

    /* Script tools */
    private boolean hasSelectedImage;
    private boolean permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindUI();
        permission = isWriteStoragePermissionGranted();
    }

    private void bindUI() {
        mPlayerName1 = findViewById(R.id.player_name1);
        mPlayerName2 = findViewById(R.id.player_name2);
        mResult = findViewById(R.id.result);
        mSelectPhoto = findViewById(R.id.select_photo_btn);
        mCreatePhoto = findViewById(R.id.create_photo_btn);
        mSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPhoto();
            }
        });
        mCreatePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPhoto();
            }
        });
        mPositionSwitch = findViewById(R.id.position_switch);
        mRoundSpinner = findViewById(R.id.spinner);
    }

    /* *************************** Select photo ************************/
    private void selectPhoto() {
        if(permission) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            intent.setType("image/*");
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, PICK_IMGAE_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == PICK_IMGAE_REQUEST_CODE) {
            if (data.getData() != null) {
                try {
                    mPickedImage = RotateBitmap(getBitmapFromUri(this,
                            data.getData()), 90);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(mPickedImage != null) {
                    Toast.makeText(this, "Correct image", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Incorrect image", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /* ***************************** Create photo *************************** */

    private void createPhoto() {
        mResultDraw = BitmapFactory.decodeResource(getResources(), R.drawable.resultado_torneo_australian_open);
        Log.d("Result", "" + mResultDraw.getHeight() + mResultDraw.getWidth());
        Bitmap resultBmp = drawResultBitmap();
        float offsetX = (mPickedImage.getWidth() - mResultDraw.getWidth()) / 2;
        float offsetY = mPositionSwitch.isChecked() ? Y_OFFSET_UP : Y_OFFSET_DOWN;
        mFinalImage = overlay(mPickedImage, resultBmp, offsetX, offsetY);
        Log.d("Final", "" + mFinalImage.getHeight() + mFinalImage.getWidth());
        saveBitmap(this, mFinalImage,
                mPlayerName1.getText().toString() + "vs"
                        + mPlayerName2.getText().toString() + ".png");
    }


    public Bitmap drawResultBitmap() {

        Bitmap result = Bitmap.createBitmap(mResultDraw.getWidth(), mResultDraw.getHeight(),
                mResultDraw.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(mResultDraw, 0, 0, null);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.montserrat_semibold);

        Paint namesPaint = new Paint();
        namesPaint.setColor(NAMES_COLOR);
        namesPaint.setTextSize(TEXT_SIZE);
        namesPaint.setAntiAlias(true);
        namesPaint.setTypeface(typeface);

        Paint resultPaint = new Paint();
        resultPaint.setColor(RESULT_COLOR);
        resultPaint.setTextSize(TEXT_SIZE);
        resultPaint.setAntiAlias(true);
        resultPaint.setTypeface(typeface);

        Paint footerPaint = new Paint();
        footerPaint.setColor(NAMES_COLOR);
        footerPaint.setTextSize(FOOTER_TEXT_SIZE);
        footerPaint.setAntiAlias(true);
        footerPaint.setTypeface(typeface);
        // Draw names
        canvas.drawText(mPlayerName1.getText().toString(), NAMES_X, FIRST_ROW_Y, namesPaint);
        canvas.drawText(mPlayerName2.getText().toString(), NAMES_X, SECOND_ROW_Y, namesPaint);
        //  Draw Result
        String[] resultString = mResult.getText().toString().split("-");
        if(resultString.length == 2) {
            canvas.drawText(resultString[0].substring(0,1),
                    FIRST_RESULT_2_SET, FIRST_ROW_Y, resultPaint);
            canvas.drawText(resultString[0].substring(1,2),
                    FIRST_RESULT_2_SET, SECOND_ROW_Y, resultPaint);
            canvas.drawText(resultString[1].substring(0,1),
                    SECOND_RESULT_2_SET, FIRST_ROW_Y, resultPaint);
            canvas.drawText(resultString[1].substring(1,2),
                    SECOND_RESULT_2_SET, SECOND_ROW_Y, resultPaint);
        } else if(resultString.length == 3) {
            canvas.drawText(resultString[0].substring(0,1),
                    FIRST_RESULT_3_SET, FIRST_ROW_Y, resultPaint);
            canvas.drawText(resultString[0].substring(1,2),
                    FIRST_RESULT_3_SET, SECOND_ROW_Y, resultPaint);
            canvas.drawText(resultString[1].substring(0,1),
                    SECOND_RESULT_3_SET, FIRST_ROW_Y, resultPaint);
            canvas.drawText(resultString[1].substring(1,2),
                    SECOND_RESULT_3_SET, SECOND_ROW_Y, resultPaint);
            canvas.drawText(resultString[2].substring(0,2),
                    THIRD_RESULT_3_SET - 50f, FIRST_ROW_Y, resultPaint);
            canvas.drawText(resultString[2].substring(2,3),
                    THIRD_RESULT_3_SET, SECOND_ROW_Y, resultPaint);
        }
        // Draw footer
        canvas.drawText(TOURNAMENT_NAME + String.valueOf(mRoundSpinner.getSelectedItem()), mResultDraw.getWidth() / 2 - 700, FOOTER_Y, footerPaint);

        return result;
    }

    /* ************************ Permissions ************************** */

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        else {
            return true;
        }
    }
}
