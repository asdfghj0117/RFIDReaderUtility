package com.favepc.reader.rfidreaderutility.implments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.favepc.reader.rfidreaderutility.interfaces.SwipeItemTouchHelperActions;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;
import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;

//import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;

enum ButtonsState {
    GONE,
    LEFT_VISIBLE,
    RIGHT_VISIBLE
}


public class SwipeItemTouchHelperCallback extends ItemTouchHelper.Callback {


    private boolean mSwipeBack = false;
    private ButtonsState mButtonShowedState = ButtonsState.GONE;
    private static final float mSwipeButtonWidth = 300;
    private SwipeItemTouchHelperActions mSwipeButtonActions = null;
    private RectF mSwipeButtonInstance = null;
    private RecyclerView.ViewHolder mCurrentItemViewHolder = null;


    public SwipeItemTouchHelperCallback(SwipeItemTouchHelperActions buttonsActions) {
        this.mSwipeButtonActions = buttonsActions;
    }


    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(UP | DOWN, LEFT);
        //return makeMovementFlags(getDragDirs(recyclerView, viewHolder), getSwipeDirs(recyclerView, viewHolder));
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        //int fromPosition = viewHolder.getAdapterPosition();
        //int toPosition = target.getAdapterPosition();
        mSwipeButtonActions.onMoveClicked(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        //Collections.swap(mCustomizes, fromPosition, toPosition);
        //mCustomizeListAdapter.notifyItemMoved(fromPosition, toPosition);
        //Log.i("RecyclerView", "onMove: from " + fromPosition + " to " + toPosition);
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (mSwipeBack) {
            mSwipeBack = mButtonShowedState != ButtonsState.GONE;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ACTION_STATE_SWIPE) {
            if (mButtonShowedState != ButtonsState.GONE) {
                if (mButtonShowedState == ButtonsState.LEFT_VISIBLE) dX = Math.max(dX, mSwipeButtonWidth);
                if (mButtonShowedState == ButtonsState.RIGHT_VISIBLE) dX = Math.min(dX, -mSwipeButtonWidth);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
            else {
                setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
            mCurrentItemViewHolder = viewHolder;
        }

        if (mButtonShowedState == ButtonsState.GONE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

    }


    private void setTouchListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mSwipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                if (mSwipeBack) {
                    if (dX < -mSwipeButtonWidth) mButtonShowedState = ButtonsState.RIGHT_VISIBLE;
                    else if (dX > mSwipeButtonWidth) mButtonShowedState  = ButtonsState.LEFT_VISIBLE;

                    if (mButtonShowedState != ButtonsState.GONE) {
                        setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        setItemsClickable(recyclerView, false);
                    }
                }
                return false;
            }
        });
    }

    private void setTouchDownListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
                return false;
            }
        });
    }

    private void setTouchUpListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    SwipeItemTouchHelperCallback.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);
                    recyclerView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return false;
                        }
                    });
                    setItemsClickable(recyclerView, true);
                    mSwipeBack = false;

                    if (mSwipeButtonActions != null && mSwipeButtonInstance != null && mSwipeButtonInstance.contains(event.getX(), event.getY())) {
                        if (mButtonShowedState == ButtonsState.LEFT_VISIBLE) {
                            mSwipeButtonActions.onLeftClicked(viewHolder.getAdapterPosition());
                        }
                        else if (mButtonShowedState == ButtonsState.RIGHT_VISIBLE) {
                            mSwipeButtonActions.onRightClicked(viewHolder.getAdapterPosition());
                            //int position = viewHolder.getAdapterPosition();
                            //mCustomizes.remove(position);
                            //mCustomizeListAdapter.notifyItemRemoved(position);
                            //mCustomizeListAdapter.notifyItemRangeChanged(position, mCustomizeListAdapter.getItemCount());
                        }
                    }
                    mButtonShowedState = ButtonsState.GONE;
                    mCurrentItemViewHolder = null;
                }
                return false;
            }
        });
    }

    private void setItemsClickable(RecyclerView recyclerView, boolean isClickable) {
        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
            recyclerView.getChildAt(i).setClickable(isClickable);
        }
    }

    private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder) {
        float corners = 16;
        float buttonWidthWithoutPadding = mSwipeButtonWidth - 3;


        View itemView = viewHolder.itemView;
        Paint p = new Paint();

        /*RectF leftButton = new RectF(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + buttonWidthWithoutPadding, itemView.getBottom());
        p.setColor(Color.BLUE);
        c.drawRoundRect(leftButton, corners, corners, p);
        drawText("EDIT", c, leftButton, p);*/

        RectF rightButton = new RectF(itemView.getRight() - buttonWidthWithoutPadding, itemView.getTop() + 3, itemView.getRight() - 6, itemView.getBottom() - 3);
        p.setColor(Color.GRAY);
        c.drawRect(rightButton, p);
        //c.drawRoundRect(rightButton, corners, corners, p);
        drawText("DELETE", c, rightButton, p);

        mSwipeButtonInstance = null;
        if (mButtonShowedState == ButtonsState.RIGHT_VISIBLE) {
            mSwipeButtonInstance = rightButton;
        }
        /*else if (mButtonShowedState == ButtonsState.LEFT_VISIBLE) {
            mSwipeButtonInstance = leftButton;
        }
        */
    }

    private void drawText(String text, Canvas c, RectF button, Paint p) {
        float textSize = 60;
        p.setColor(Color.WHITE);
        p.setAntiAlias(true);
        p.setTextSize(textSize);

        float textWidth = p.measureText(text);
        c.drawText(text, button.centerX()-(textWidth/2), button.centerY()+(textSize/2), p);
    }

    public void onDraw(Canvas c) {
        if (mCurrentItemViewHolder != null) {
            drawButtons(c, mCurrentItemViewHolder);
        }
    }
}
