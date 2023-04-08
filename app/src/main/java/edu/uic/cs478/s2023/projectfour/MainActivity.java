package edu.uic.cs478.s2023.projectfour;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    TableLayout buttonTable;
    Button startBtn;
    TextView announceView;
    private final int RED = 0;
    private final int BLUE = 1;
    GamerThread threadRed = new GamerThread(RED);
    GamerThread threadBlue = new GamerThread(BLUE);
    private  int lastPlayerMoved = RED;
    private int setupRedCount = 0;
    private int setupBlueCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonTable = findViewById(R.id.buttonTable);

        for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
            TableRow currentRow = (TableRow) buttonTable.getChildAt(rowIndex);
            for (int colIndex = 0; colIndex < 3; colIndex++) {
                CardView currentCard = (CardView) currentRow.getChildAt(colIndex);
                currentCard.getChildAt(0).setOnClickListener(view -> Toast.makeText(MainActivity.this, "Clicked on Row " + buttonTable.indexOfChild(currentRow) + ", Col " + currentRow.indexOfChild(currentCard) + " From Thread: " + Thread.currentThread().getName(), Toast.LENGTH_SHORT).show());
            }
        }

//        Start the threads
        threadRed.start();
        threadBlue.start();


        startBtn = findViewById(R.id.buttonStart);
        startBtn.setOnClickListener(new View.OnClickListener() {
//            TODO: Make it so pressing this again will RESET the game.
            @Override
            public void onClick(View view) {
                GamerThread firstPlayer;
                if (new Random().nextBoolean()) {
                    firstPlayer = threadRed;
                }
                else {
                    firstPlayer = threadBlue;
                }
                firstPlayer.handler.post(() -> makeInitialPlacements(firstPlayer.THREAD_TEAM, firstPlayer));
            }
        });

        announceView = findViewById(R.id.announceView);
    }

    public synchronized void makeInitialPlacements(int TEAM, GamerThread givenThread) {
        if ((TEAM == RED && setupRedCount >= 3) || (TEAM == BLUE && setupBlueCount >= 3)) {
            return;
        }
        Random rand = new Random();
        ImageButton cell;
        while (true) {
            int row = rand.nextInt(3);
            int col = rand.nextInt(3);
            TableRow currentRow = (TableRow) buttonTable.getChildAt(row);
            CardView currentCard = (CardView) currentRow.getChildAt(col);
            cell = (ImageButton) currentCard.getChildAt(0);
            if (cell.getContentDescription().toString().equals("N")) {
                currentCard.callOnClick(); //Show it being clicked for my own amusement
                break;
            }
        }
        Log.d("GamerMove", "makeMove: About to send ui modification request from " + givenThread.getName());

        lastPlayerMoved = TEAM;

        Runnable updateTile;
        ImageButton finalCell = cell;
        if (TEAM == RED) {
            updateTile = () -> {
                finalCell.setBackgroundResource(R.drawable.tile_red);
                finalCell.setContentDescription("R");
                Log.d("GamerUIPost", "makeMove: Just modified UI with Red move from " + Thread.currentThread().getName());
                startBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.teamRed));
                if (isWinner(TEAM)){
                    announceView.setText("Red Just won!");
                    return;
                } else {
                    announceView.setText("Red's last move did not result in a win.");
                }
                threadBlue.handler.post(() -> makeInitialPlacements(BLUE, threadBlue));
            };
            setupRedCount += 1;
        } else {
            updateTile = () -> {
                finalCell.setBackgroundResource(R.drawable.tile_blue);
                finalCell.setContentDescription("B");
                Log.d("GamerUIPost", "makeMove: Just modified UI with Blue move from " + Thread.currentThread().getName());
                startBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.teamBlue));
                if (isWinner(TEAM)){
                    announceView.setText("Blue Just won!");
                    return;
                } else {
                    announceView.setText("Blue's last move did not result in a win.");
                }
                threadRed.handler.post(() -> makeInitialPlacements(RED, threadRed));
            };
            setupBlueCount += 1;
        }

//        Wait 3 seconds to make it visible to the users
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        Tell the UI thread what we wanted to do
        runOnUiThread(updateTile);
    }

    private boolean isWinner(int givenTEAM) {
        TableRow topRow = (TableRow) buttonTable.getChildAt(0);
        TableRow midRow = (TableRow) buttonTable.getChildAt(1);
        TableRow btmRow = (TableRow) buttonTable.getChildAt(2);
        ImageButton btnLeftTop = (ImageButton)((CardView) topRow.getChildAt(0)).getChildAt(0);
        ImageButton btnLeftMiddle = (ImageButton)((CardView) midRow.getChildAt(0)).getChildAt(0);
        ImageButton btnLeftBottom = (ImageButton)((CardView) btmRow.getChildAt(0)).getChildAt(0);
        ImageButton btnMiddleTop = (ImageButton)((CardView) topRow.getChildAt(1)).getChildAt(0);
        ImageButton btnMiddleMiddle = (ImageButton)((CardView) midRow.getChildAt(1)).getChildAt(0);
        ImageButton btnMiddleBottom = (ImageButton)((CardView) btmRow.getChildAt(1)).getChildAt(0);
        ImageButton btnRightTop = (ImageButton)((CardView) topRow.getChildAt(2)).getChildAt(0);
        ImageButton btnRightMiddle = (ImageButton)((CardView) midRow.getChildAt(2)).getChildAt(0);
        ImageButton btnRightBottom = (ImageButton)((CardView) btmRow.getChildAt(2)).getChildAt(0);
        ImageButton[][] btnsByRow = {{btnLeftTop,btnMiddleTop,btnRightTop},{btnLeftMiddle,btnMiddleMiddle,btnRightMiddle},{btnLeftBottom,btnMiddleBottom,btnRightBottom}};
        ImageButton[][] btnsByCol = {{btnLeftTop,btnLeftMiddle,btnLeftBottom},{btnMiddleTop,btnMiddleMiddle,btnMiddleBottom},{btnRightTop,btnRightMiddle,btnRightBottom}};

//        First check rows for matching color
        for (ImageButton[] row : btnsByRow) {
            switch (givenTEAM) {
                case RED:
                    if (isRed(row[0]) && isRed(row[1]) && isRed(row[2]))
                        return true;
                    break;
                case BLUE:
                    if (isBlue(row[0]) && isBlue(row[1]) && isBlue(row[2]))
                        return true;
                    break;
            }
        }
//        No win found in rows, check columns
        for (ImageButton[] col : btnsByCol) {
            switch (givenTEAM) {
                case RED:
                    if (isRed(col[0]) && isRed(col[1]) && isRed(col[2]))
                        return true;
                    break;
                case BLUE:
                    if (isBlue(col[0]) && isBlue(col[1]) && isBlue(col[2]))
                        return true;
                    break;
            }
        }
//        No matching row or column found. No winna.
        return false;
    }

    private boolean isRed(ImageButton b) {
        return b.getContentDescription().toString().equals("R");
    }
    private  boolean isBlue(ImageButton b) {
        return b.getContentDescription().toString().equals("B");
    }
}