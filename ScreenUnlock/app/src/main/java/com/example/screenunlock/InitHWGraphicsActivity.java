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

import com.example.screenunlock.utils.RecordFile;
import com.example.screenunlock.view.MyView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

public class InitHWGraphicsActivity extends AppCompatActivity {
    private MyView view;

    // use to apply permission for write and read file
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE =
            { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private int signCounter;

    // acquire the storage path of system picture
    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }

        // set counter = 0
        this.signCounter = 0;
        // Make sure the Pictures directory exists.
        path.mkdirs();

        setContentView(R.layout.activity_init_h_w_graphics);
        view = (MyView) findViewById(R.id.myView1);
    }

    /**
     * Called when the user click save button
     *
     * */
    public void saveToFileClick(View v) throws IOException {
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
            String string = path.getPath() + "/" + name;
            // save sign fileFF
            view.saveToFile(string);

            // when draw new sig file, calculate dtw distance to the existed files and see if this new one is qualified
            // to be saved
            // if (signCounter >= 2) {
            // Boolean result = Verify.calDtwDistanceForNewDrawed(string + ".sig");
            // }

            // record sign file path
            RecordFile.saveNewSignFilePath(string + ".sig", path.getPath() + "/");
            //
            Log.d("CREATE", string);
            this.signCounter++;
            if (signCounter <= 2) {
                Toast.makeText(this,
                        "Graphic " + signCounter + " saved successfully! \n" + "You should draw at least 3 times!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,
                        "Graphic " + signCounter + " saved successfully! \n" + "You could draw more or start training!",
                        Toast.LENGTH_LONG).show();
            }
            view.clear(this.signCounter);

        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Save failed!\n" + e, Toast.LENGTH_LONG).show();
        }
    }

    /** Called when the user click clear button */
    public void clearClick(View v) {
        view.clear(this.signCounter);
    }

    /** Called when the user click back button */
    public void backClick(View v) {
        if (this.signCounter < 3) {
            Toast.makeText(this, "Please draw at least third,\n " + "failed to initialize handwritten graphics",
                    Toast.LENGTH_LONG).show();
            finish();
        }


        Boolean replaceResult = RecordFile.replace(); // replace the old file with this new one
        if (replaceResult == false) {
            Toast.makeText(this, "System Error, " + "Please try again!\n", Toast.LENGTH_LONG * 2).show();
            finish();
        }

        Toast.makeText(this,
                "Congratulations!\n" + "Succeed to initialize handwritten graphics! You can do trainning now!",
                Toast.LENGTH_LONG).show();
        finish();
    }

}
