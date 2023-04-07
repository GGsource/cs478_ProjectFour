package edu.uic.cs478.s2023.projectfour;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    TableLayout buttonTable;
    Button startBtn;
    GamerThread threadRed = new GamerThread();
    GamerThread threadBlue = new GamerThread();
    private final int RED = 0;
    private final int BLUE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonTable = findViewById(R.id.buttonTable);

        for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
            TableRow currentRow = (TableRow) buttonTable.getChildAt(rowIndex);
            for (int colIndex = 0; colIndex < 3; colIndex++) {
                CardView currentCard = (CardView) currentRow.getChildAt(colIndex);
                currentCard.setOnClickListener(view -> Toast.makeText(MainActivity.this, "Clicked on Row " + buttonTable.indexOfChild(currentRow) + ", Col " + currentRow.indexOfChild(currentCard) , Toast.LENGTH_SHORT).show());
            }
        }
        startBtn = findViewById(R.id.buttonStart);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                threadRed.start();
                threadBlue.start();
                if (new Random().nextBoolean()) {
                    threadRed.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            makeMove(RED);
                        }
                    });
                }
                else {
                    threadBlue.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            makeMove(BLUE);
                        }
                    });
                }
            }
        });
    }

    private void makeMove(int TEAM) {
        Random rand = new Random();
        int row = rand.nextInt(3);
        int col = rand.nextInt(3);
        switch (TEAM) {
            case RED:
                break;
            case BLUE:
                break;
        }
    }
}