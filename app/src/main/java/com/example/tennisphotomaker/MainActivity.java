package com.example.tennisphotomaker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.tennisphotomaker.Utils.BitmapUtils.*;

public class MainActivity extends AppCompatActivity {

    /* Layout elements */
    EditText mPlayerName1;
    EditText mPlayerName2;
    EditText mResult;
    Button mSelectPhoto;
    Button mCreatePhoto;

    /* Images */
    Bitmap mPickedImage;
    Bitmap mResultDraw;
    Bitmap mFinalImage;

    /* Script tools */
    private boolean hasSelectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindUI();
        isWriteStoragePermissionGranted();
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
    }

    /* *************************** Select photo ************************/
    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 1) {
            if (data.getData() != null) {
                try {
                    mPickedImage = RotateBitmap(getBitmapFromUri(this, data.getData()), 90);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(mPickedImage != null) Toast.makeText(this, "Correct image", Toast.LENGTH_LONG).show();
                else Toast.makeText(this, "Incorrect image", Toast.LENGTH_LONG).show();
            }
        }
    }



    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    /* ***************************** Create photo *************************** */
    private void createPhoto() {
        mResultDraw = BitmapFactory.decodeResource(getResources(), R.drawable.resultado_torneo);
        Bitmap resultBmp = drawResultBitmap();
        mFinalImage = overlay(mPickedImage, resultBmp);
        saveFile(mFinalImage, mPlayerName1.getText().toString() + "vs" + mPlayerName2.getText().toString() + ".png");
    }

    public void saveFile(Bitmap b, String picName){
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dir = new File(file_path);
        boolean fileExists;
        if(!dir.exists()) fileExists = dir.mkdirs();
        else fileExists = true;

        if(fileExists){
            File file = new File(dir, picName);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                b.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                Toast.makeText(this, "Imagen guardada", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap drawResultBitmap() {

        Bitmap result = Bitmap.createBitmap(mResultDraw.getWidth(), mResultDraw.getHeight(), mResultDraw.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(mResultDraw, 0, 0, null);

        Paint namesPaint = new Paint();
        namesPaint.setColor(Color.WHITE);
        namesPaint.setTextSize(120);
        namesPaint.setAntiAlias(true);

        Paint resultPaint = new Paint();
        resultPaint.setColor(0xFF223667);
        resultPaint.setTextSize(120);
        resultPaint.setAntiAlias(true);

        canvas.drawText(mPlayerName1.getText().toString(), 60, 160, namesPaint);
        canvas.drawText(mPlayerName2.getText().toString(), 60, 380, namesPaint);

        String[] resultString = mResult.getText().toString().split("-");
        if(resultString.length == 2) {
            canvas.drawText(resultString[0].substring(0,1), 860, 160, resultPaint);
            canvas.drawText(resultString[0].substring(1,2), 860, 380, resultPaint);
            canvas.drawText(resultString[1].substring(0,1), 1060, 160, resultPaint);
            canvas.drawText(resultString[1].substring(1,2), 1060, 380, resultPaint);
        } else if(resultString.length == 3) {
            canvas.drawText(resultString[0].substring(0,1), 750, 160, resultPaint);
            canvas.drawText(resultString[0].substring(1,2), 750, 380, resultPaint);
            canvas.drawText(resultString[1].substring(0,1), 900, 160, resultPaint);
            canvas.drawText(resultString[1].substring(1,2), 900, 380, resultPaint);
            canvas.drawText(resultString[2].substring(0,1), 1050, 160, resultPaint);
            canvas.drawText(resultString[2].substring(1,2), 1050, 380, resultPaint);
        }
        return result;
    }

    /* ************************ Permissions ************************** */
    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else {
            return true;
        }
    }
}
