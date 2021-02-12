package com.example.screenunlock;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.screenunlock.view.MyView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import static com.example.screenunlock.utils.Verify.verifySignature;

public class VerifyHWGraphicsActivity extends AppCompatActivity {
    private MyView view;
    private int signCounter;

    // acquire the storage path of system picture
    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

    // use to apply permission for write and read file
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE =
            { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_h_w_graphics);

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }

        // Make sure the Pictures directory exists.
        path.mkdirs();

        this.signCounter = 0;

        setContentView(R.layout.activity_verify_h_w_graphics);
        view = (MyView) findViewById(R.id.myView2);
    }

    /** Called when the user click clear button */
    public void clearClick(View v) {
        view.clear(0);
    }

    /** Called when the user click back button */
    public void backClick(View v) {
        finish();
    }

    /** Called when the user click verify button */
    public void verifyClick(View v) throws IOException {
        try {
            String sdState = Environment.getExternalStorageState(); // check SD card exists

            // check SD card is available
            if (!sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
                Toast.makeText(this, "SD card is not ready！", Toast.LENGTH_SHORT).show();
                // break;
                return;
            }

            // create the picture name with timestamp
            Calendar c = Calendar.getInstance();
            String name = "" + c.get(Calendar.YEAR) + c.get(Calendar.MONTH) + c.get(Calendar.DAY_OF_MONTH)
                    + c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) + c.get(Calendar.SECOND);

            // Form a full path
            String string = path.getPath() + "/forg_verify_" + name;
            // save sign file
            view.saveToFile(string);
            Log.d("CREATE", string);

            // verify
            if (verifySignature(string + ".sig")) {
                Toast.makeText(this, "Congratulations!\n" + "Unlock the screen successfully!", Toast.LENGTH_LONG)
                        .show();
                finish();
            } else {
                Toast.makeText(this, "Sorry!\n" + "Failed to unlock screen!", Toast.LENGTH_LONG).show();
                this.signCounter ++;
                view.clear(0);
            }

        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Save failed!\n" + e, Toast.LENGTH_LONG).show();
        }
    }
}
