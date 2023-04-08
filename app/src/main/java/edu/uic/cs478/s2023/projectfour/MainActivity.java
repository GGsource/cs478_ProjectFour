package edu.uic.cs478.s2023.projectfour;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    TableLayout buttonTable;
    AppCompatButton startBtn;
    TextView announceView;
    private final int RED = 0;
    private final int BLUE = 1;
    GamerThread threadRed = new GamerThread(RED);
    GamerThread threadBlue = new GamerThread(BLUE);
    private  int lastPlayerMoved = RED;
    private int setupRedCount = 0;
    private int setupBlueCount = 0;
    private ArrayList<ImageButton> buttonListRed = new ArrayList<>();
    private ArrayList<ImageButton> buttonListBlue = new ArrayList<>();

    private int delayMS = 2000;

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
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[] {ContextCompat.getColor(this, R.color.teamRed), ContextCompat.getColor(this, R.color.teamBlue)}
        );
//        startBtn.setForeground(gd);
        startBtn.setBackground(gd);
        startBtn.setOnClickListener(new View.OnClickListener() {
//            FIXME: Clicking start while match is happening causes crash. Because reset assumes theres 3 places already
            @Override
            public void onClick(View view) {
//            Reset all in case a previous game was taking place
                boolean needsReset = buttonListBlue.size() > 0 || buttonListRed.size() > 0;
                if (needsReset) {
                    threadRed.looper.quitSafely();
                    threadBlue.looper.quitSafely();
                    threadRed = new GamerThread(RED);
                    threadBlue = new GamerThread(BLUE);
                    threadRed.start();
                    threadBlue.start();
                    setupRedCount = 0;
                    for (int i = buttonListRed.size()-1; i >= 0; i--) {
                        ImageButton curRed = buttonListRed.remove(i);
                        curRed.setContentDescription("N");
                        curRed.setBackgroundResource(R.drawable.tile_neutral);
                    }
                    setupBlueCount = 0;
                    for (int i = buttonListBlue.size()-1; i >= 0; i--) {
                        ImageButton curBlue = buttonListBlue.remove(i);
                        curBlue.setContentDescription("N");
                        curBlue.setBackgroundResource(R.drawable.tile_neutral);
                    }
                    announceView.setTextColor(Color.WHITE);
                    announceView.setText("Game Reset. Deciding who goes first...");
                    startBtn.setBackgroundDrawable(gd);
                }
                startBtn.setText("Restart Game");
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
//            We have enough pieces on the board and presumably no winners, begin the main game.
            Log.d("GamerInitEnd", "Initialization Ended: testing how long til this prints! Now beginning main game loop");
            takeTurn(TEAM);
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
                buttonListRed.add(finalCell);
                finalCell.setBackgroundResource(R.drawable.tile_red);
                finalCell.setContentDescription("R");
                Log.d("GamerUIPost", "makeMove: Just modified UI with Red move from " + Thread.currentThread().getName());
                startBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.teamRed));
                if (isWinner(TEAM)){
                    announceView.setText("Red Just won!");
                    announceView.setTextColor(ContextCompat.getColor(this, R.color.teamRed));
                    startBtn.setText("Start New Game");
                    return;
                } else {
                    announceView.setText("Red's last move did not result in a win.");
                }
                threadBlue.handler.post(() -> makeInitialPlacements(BLUE, threadBlue));
            };
            setupRedCount += 1;
        } else {
            updateTile = () -> {
                buttonListBlue.add(finalCell);
                finalCell.setBackgroundResource(R.drawable.tile_blue);
                finalCell.setContentDescription("B");
                Log.d("GamerUIPost", "makeMove: Just modified UI with Blue move from " + Thread.currentThread().getName());
                startBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.teamBlue));
                if (isWinner(TEAM)){
                    announceView.setText("Blue Just won!");
                    announceView.setTextColor(ContextCompat.getColor(this, R.color.teamBlue));
                    startBtn.setText("Start New Game");
                    return;
                } else {
                    announceView.setText("Blue's last move did not result in a win.");
                }
                threadRed.handler.post(() -> makeInitialPlacements(RED, threadRed));
            };
            setupBlueCount += 1;
        }

//        Wait 1 seconds to make it visible to the users
        try {
            Thread.sleep(delayMS);
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
    private boolean isBlue(ImageButton b) {
        return b.getContentDescription().toString().equals("B");
    }

    private synchronized void takeTurn(int givenTEAM) {
//        givenTeam must now move one of their tiles to an empty spot
        Random rand = new Random();
        ImageButton destinationCell;
        while (true) {
            int row = rand.nextInt(3);
            int col = rand.nextInt(3);
            TableRow currentRow = (TableRow) buttonTable.getChildAt(row);
            CardView currentCard = (CardView) currentRow.getChildAt(col);
            destinationCell = (ImageButton) currentCard.getChildAt(0);
            if (destinationCell.getContentDescription().toString().equals("N")) {
                break;
            }
        }
//        We now have a valid empty destination, pick what place will be moved from
        int replaceNdx = rand.nextInt(3);
        ImageButton sourceCell;
        if (givenTEAM == RED) {
            sourceCell = buttonListRed.get(replaceNdx);
        }
        else {
            sourceCell = buttonListBlue.get(replaceNdx);
        }

        //        Wait 1 seconds to make it visible to the users
        try {
            Thread.sleep(delayMS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

//        Let main thread know what to change
        ImageButton finalDestinationCell = destinationCell;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finalDestinationCell.setContentDescription(sourceCell.getContentDescription());
                sourceCell.setContentDescription("N");
                sourceCell.setBackgroundResource(R.drawable.tile_neutral);
                switch (givenTEAM) {
                    case RED:
                        finalDestinationCell.setBackgroundResource(R.drawable.tile_red);
                        buttonListRed.remove(sourceCell);
                        buttonListRed.add(finalDestinationCell);
                        startBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.teamRed));
                        if (isWinner(givenTEAM)) {
//                    This move resulted in a winner!
                            announceView.setText("Red Just won!");
                            announceView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.teamRed));
                            startBtn.setText("Start New Game");
                            return;
                        } else {
                            announceView.setText("Red's last move did not result in a win.");
                        }
                        threadBlue.handler.post(()->takeTurn(BLUE));
                        break;
                    case BLUE:
                        finalDestinationCell.setBackgroundResource(R.drawable.tile_blue);
                        buttonListBlue.remove(sourceCell);
                        buttonListBlue.add(finalDestinationCell);
                        startBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.teamBlue));
                        if (isWinner(givenTEAM)) {
//                    This move resulted in a winner!
                            announceView.setText("Blue Just won!");
                            announceView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.teamBlue));
                            startBtn.setText("Start New Game");
                            return;
                        } else {
                            announceView.setText("Blue's last move did not result in a win.");
                        }
                        threadRed.handler.post(()->takeTurn(RED));
                        break;
                }
            }
        });
    }
}