package com.example.screenunlock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.screenunlock.utils.RecordFile;
import com.example.screenunlock.utils.Verify;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Called when the user click the Initial Handwritten graphics button
     */
    public void DisplayInitHWGraphicsActivity(View view) {
        Intent intent = new Intent(this, InitHWGraphicsActivity.class);
        startActivity(intent);
    }


    /**
     * Called when the user click train button
     */
    public void trainClick(View v) {
        try {
            Object[] result = Verify.calDtwDistanceEntrance();
            if ((Boolean) result[0] == false) {
                Toast.makeText(this, (String) result[1], Toast.LENGTH_LONG * 5).show();
            } else {
                Toast.makeText(this,
                        "Congratulations!\n" + "Training Finished, distance threshold is " + (Double) result[2],
                        Toast.LENGTH_LONG * 3).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the user click the Verify Handwritten graphics button
     */
    public void DisplayVerifyHWGraphicsActivity(View view) throws IOException {
        if( RecordFile.getSignPaths().size() < 3){
            Toast.makeText(this, "Sorry!\n You should retry handwritten graphics first!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        
        if(Verify.referDSetTrained == false ){
            Toast.makeText(this, "Sorry!\n You need to successfully train the data first!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        
        Intent intent = new Intent(this, VerifyHWGraphicsActivity.class);
        startActivity(intent);
    }

}
