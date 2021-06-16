package com.example.pptviewing;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MainActivity extends AppCompatActivity {

    File file = null;
    Uri wordUri = null;
    String idProofFileTypes;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button openWordFile = findViewById(R.id.button);
        Button getWordFile = findViewById(R.id.getWordFile);


        String[] mimetypes = {"application/doc", "application/msword", "application/docx"};
        getWordFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); //one attachment is allowed
                i.setType(mimetypes.length == 1 ? mimetypes[0] : "*/*");

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    i.setType(mimetypes.length == 1 ? mimetypes[0] : "*/*");
//                    if (mimetypes.length > 0) {
//                        i.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
//                    }
//                } else {
//                    idProofFileTypes = "";
//                    for (String mimeType : mimetypes) {
//                        idProofFileTypes += mimeType + "|";
//                    }
//                    i.setType(idProofFileTypes.substring(0, idProofFileTypes.length() - 1));
//                }


                someActivityResultLauncher.launch(i);

            }
        });
        openWordFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(Intent.ACTION_VIEW);
                String type = "application/msword";
                intent.setDataAndType(wordUri, type);
                startActivity(intent);
            }
        });

    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();


                        file = null;
                        try {
                            file = getFileFromUri(MainActivity.this, data.getData());
                            wordUri = FileProvider.getUriForFile(MainActivity.this, this.getClass().getPackage() + ".provider", file);

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }
                }
            });

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK) {
                    file = null;
                    try {
                        file = getFileFromUri(this, data.getData());
                        wordUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public static File getFileFromUri(final Context context, final Uri uri) throws
            Exception {

     /*   if (isGoogleDrive(uri)) // check if file selected from google drive
        {*/
        return saveFileIntoExternalStorageByUri(context, uri);
       /* }else
            // do your other calculation for the other files and return that file
            return null;*/
    }

    public static File saveFileIntoExternalStorageByUri(Context context, Uri
            uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        int originalSize = inputStream.available();

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        String fileName = getFileName(context, uri);
        File file = makeEmptyFileIntoExternalStorageWithTitle(fileName);
        bis = new BufferedInputStream(inputStream);
        bos = new BufferedOutputStream(new FileOutputStream(
                file, false));

        byte[] buf = new byte[originalSize];
        bis.read(buf);
        do {
            bos.write(buf);
        } while (bis.read(buf) != -1);

        bos.flush();
        bos.close();
        bis.close();

        return file;
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static File makeEmptyFileIntoExternalStorageWithTitle(String title) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        return new File(root, title);
    }

}