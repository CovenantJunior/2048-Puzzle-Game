package com.calculateall.2048;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

class InputListener implements View.OnTouchListener
{
    private static final int SWIPE_MIN_DISTANCE = 0;
    private static final int SWIPE_THRESHOLD_VELOCITY = 25;
    private static final int MOVE_THRESHOLD = 250;
    private static final int RESET_STARTING = 10;
    private final MainView mView;
    private float x;
    private float y;
    private float lastDx;
    private float lastDy;
    private float previousX;
    private float previousY;
    private float startingX;
    private float startingY;
    private int previousDirection = 1;
    private int veryLastDirection = 1;
    private boolean hasMoved = false;

    public InputListener(MainView view)
    {
        super();
        this.mView = view;
    }

    public boolean onTouch(View view, MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                startingX = x;
                startingY = y;
                previousX = x;
                previousY = y;
                lastDx = 0;
                lastDy = 0;
                hasMoved = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                if (mView.game.isActive())
                {
                    float dx = x - previousX;
                    if (Math.abs(lastDx + dx) < Math.abs(lastDx) + Math.abs(dx) && Math.abs(dx) > RESET_STARTING
                            && Math.abs(x - startingX) > SWIPE_MIN_DISTANCE)
                    {
                        startingX = x;
                        startingY = y;
                        lastDx = dx;
                        previousDirection = veryLastDirection;
                    }
                    if (lastDx == 0)
                    {
                        lastDx = dx;
                    }
                    float dy = y - previousY;
                    if (Math.abs(lastDy + dy) < Math.abs(lastDy) + Math.abs(dy) && Math.abs(dy) > RESET_STARTING
                            && Math.abs(y - startingY) > SWIPE_MIN_DISTANCE)
                    {
                        startingX = x;
                        startingY = y;
                        lastDy = dy;
                        previousDirection = veryLastDirection;
                    }
                    if (lastDy == 0)
                    {
                        lastDy = dy;
                    }
                    if (pathMoved() > SWIPE_MIN_DISTANCE * SWIPE_MIN_DISTANCE && !hasMoved)
                    {
                        boolean moved = false;
                        //Vertical
                        if (((dy >= SWIPE_THRESHOLD_VELOCITY && Math.abs(dy) >= Math.abs(dx)) || y - startingY >= MOVE_THRESHOLD) && previousDirection % 2 != 0)
                        {
                            moved = true;
                            previousDirection = previousDirection * 2;
                            veryLastDirection = 2;
                            mView.game.move(2);
                        }
                        else if (((dy <= -SWIPE_THRESHOLD_VELOCITY && Math.abs(dy) >= Math.abs(dx)) || y - startingY <= -MOVE_THRESHOLD) && previousDirection % 3 != 0)
                        {
                            moved = true;
                            previousDirection = previousDirection * 3;
                            veryLastDirection = 3;
                            mView.game.move(0);
                        }
                        //Horizontal
                        if (((dx >= SWIPE_THRESHOLD_VELOCITY && Math.abs(dx) >= Math.abs(dy)) || x - startingX >= MOVE_THRESHOLD) && previousDirection % 5 != 0)
                        {
                            moved = true;
                            previousDirection = previousDirection * 5;
                            veryLastDirection = 5;
                            mView.game.move(1);
                        }
                        else if (((dx <= -SWIPE_THRESHOLD_VELOCITY && Math.abs(dx) >= Math.abs(dy)) || x - startingX <= -MOVE_THRESHOLD) && previousDirection % 7 != 0)
                        {
                            moved = true;
                            previousDirection = previousDirection * 7;
                            veryLastDirection = 7;
                            mView.game.move(3);
                        }
                        if (moved)
                        {
                            hasMoved = true;
                            startingX = x;
                            startingY = y;
                        }
                    }
                }
                previousX = x;
                previousY = y;
                return true;
            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
                previousDirection = 1;
                veryLastDirection = 1;

                //"Menu" inputs
                if (!hasMoved)
                {
                    if (iconPressed(mView.sXNewGame, mView.sYIcons))
                    {
                        new AlertDialog.Builder(mView.getContext())
                                .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        // reset rewards again:
                                        MainActivity.mRewardDeletes = 2;
                                        MainActivity.mRewardDeletingSelectionAmounts = 3;

                                        mView.game.newGame();
                                        mView.game.canUndo = false;
                                    }
                                })
                                .setNegativeButton(R.string.continue_game, null)
                                .setTitle(R.string.reset_dialog_title)
                                .setMessage(R.string.reset_dialog_message)
                                .show();
                    }
                    else if (iconPressed(mView.sXUndo, mView.sYIcons))
                    {
                        mView.game.revertUndoState();
                    }
                    else if(iconPressed(mView.sXRemoveTiles, mView.sYIcons))
                    {
                        mView.game.RemoveTilesWithTrash();
                    }
                    else if(iconPressed(mView.sXLoad, mView.sYIcons))
                    {
                        mView.game.loadCurrentBoard();  // load previous board
                    }
                    else if(iconPressed(mView.sXSave, mView.sYIcons))
                    {
                        mView.game.saveCurrentBoard();  // save current board
                    }
                    else if (isTap(2) && inRange(mView.startingX, x, mView.endingX)
                            && inRange(mView.startingY, x, mView.endingY) && mView.continueButtonEnabled)
                    {
                        mView.game.setEndlessMode();
                    }
                }
        }
        return true;
    }

    private float pathMoved()
    {
        return (x - startingX) * (x - startingX) + (y - startingY) * (y - startingY);
    }

    private boolean iconPressed(int sx, int sy)
    {
        return isTap(1) && inRange(sx, x, sx + mView.iconSize)
                && inRange(sy, y, sy + mView.iconSize);
    }

    private boolean inRange(float starting, float check, float ending)
    {
        return (starting <= check && check <= ending);
    }

    private boolean isTap(int factor)
    {
        return pathMoved() <= mView.iconSize * factor;
    }
}