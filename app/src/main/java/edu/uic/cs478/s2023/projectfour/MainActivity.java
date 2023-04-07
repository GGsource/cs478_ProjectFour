package edu.uic.cs478.s2023.projectfour;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
//TODO: List of 9 booleans initialized to null. Null = blank, 0 = Thread A, 1 = Thread B