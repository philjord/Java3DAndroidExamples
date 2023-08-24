package org.jogamp.java3d.examples.fps_counter;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import java.util.Calendar;


public class GLWindowOverLay {
    private Context context;
    private PopupWindow popupWindow;
    private View popupView;
    private int mCurrentX = 0;
    private int mCurrentY = 0;
    private int gravity;
    private View parent;
    private boolean movable = false;
    private View.OnClickListener listener;


    public GLWindowOverLay(Context context, View parent, int viewId, int gravity) {
        this(context, parent, viewId, gravity, false);
    }

    public GLWindowOverLay(Context context, View parent, int viewId, int gravity, boolean movable) {
        this(context, parent, viewId, gravity, movable, 0, 0);
    }

    /** note right gravity REVERSES the direction of x coords! same for bottom for y
     *
     * @param context
     * @param parent
     * @param viewId
     * @param gravity
     * @param movable
     * @param offSetX
     * @param offSetY
     */
    public GLWindowOverLay(Context context, View parent, int viewId, int gravity, boolean movable, int offSetX, int offSetY) {
        this.context = context;
        this.parent = parent;
        this.mCurrentX = offSetX;
        this.mCurrentY = offSetY;
        this.gravity = gravity;
        this.movable = movable;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(viewId, null);

        // convert to pixel for the location
        mCurrentX = (int) (mCurrentX * context.getResources().getDisplayMetrics().density);
        mCurrentY = (int) (mCurrentY * context.getResources().getDisplayMetrics().density);

        popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    }

    public void showTooltip() {
        popupWindow.showAtLocation(parent, gravity, mCurrentX, mCurrentY);

        if (movable || listener != null) {
            // add a listener for drags that aren't caught by other buttons
            popupView.setOnTouchListener(new View.OnTouchListener() {
                private static final int MAX_CLICK_DURATION = 200;
                private long startClickTime;
                private float mDx;
                private float mDy;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    // note right gravity REVERSES the direction of x coords! same for bottom for y
                    float ex = (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT ? -event.getRawX() : event.getRawX();
                    float ey = (gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM ? -event.getRawY() : event.getRawY();
                    if (action == MotionEvent.ACTION_DOWN) {
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        mDx = mCurrentX - ex;
                        mDy = mCurrentY - ey;
                    } else if (action == MotionEvent.ACTION_MOVE) {
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if (clickDuration >= MAX_CLICK_DURATION && movable) {
                            mCurrentX = (int) (ex + mDx);
                            mCurrentY = (int) (ey + mDy);
                            popupWindow.update(mCurrentX, mCurrentY, -1, -1);
                        }
                    } else if (action == MotionEvent.ACTION_UP) {
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if (clickDuration < MAX_CLICK_DURATION && listener != null) {
                            listener.onClick(popupView);
                        }
                    }
                    return true;
                }
            });
        }
    }

    public void hideTooltip() {
        if (popupWindow != null & popupWindow.isShowing() && !((Activity) GLWindowOverLay.this.context).isFinishing())
            popupWindow.dismiss();
    }

    public View getButton(int viewId) {
        return popupView.findViewById(viewId);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }
}
