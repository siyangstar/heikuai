package com.cqsynet.swifi.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.LinkedList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/10/11
 */
public class SoftKeyboardStateHelper implements ViewTreeObserver.OnGlobalLayoutListener {

    public interface SoftKeyboardStateListener {
        void onSoftKeyboardOpened(int keyboardHeightInPx);
        void onSoftKeyboardClosed();
    }

    private final List<SoftKeyboardStateListener> listeners = new LinkedList<>();
    private final View activityRootView;
    private int        lastSoftKeyboardHeightInPx;
    private boolean    isSoftKeyboardOpened;

    public SoftKeyboardStateHelper(View activityRootView) {
        this(activityRootView, false);
    }

    public SoftKeyboardStateHelper(View activityRootView, boolean isSoftKeyboardOpened) {
        this.activityRootView     = activityRootView;
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        final Rect r = new Rect();
        //r will be populated with the coordinates of your view that area still visible.
        activityRootView.getWindowVisibleDisplayFrame(r);

        int softButton = AppUtil.getSoftButtonsBarHeight((Activity)activityRootView.getContext());
//        final int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
//        if (!isSoftKeyboardOpened && heightDiff > 200) { // if more than 100 pixels, its probably a keyboard...
//            isSoftKeyboardOpened = true;
//            notifyOnSoftKeyboardOpened(heightDiff);
//        } else if (isSoftKeyboardOpened && heightDiff < 200) {
//            isSoftKeyboardOpened = false;
//            notifyOnSoftKeyboardClosed();
//        }
        int heightDiff = activityRootView.getRootView().getHeight() - r.bottom - softButton;
        if (!isSoftKeyboardOpened && heightDiff > 0) {  //小米mix2比较特殊,隐藏虚拟导航后,算出来是<0,所以此处不能用!=0
            isSoftKeyboardOpened = true;
            notifyOnSoftKeyboardOpened(heightDiff);
        } else if (isSoftKeyboardOpened && heightDiff <= 0) {
            isSoftKeyboardOpened = false;
            notifyOnSoftKeyboardClosed();
        }
    }

    public void setIsSoftKeyboardOpened(boolean isSoftKeyboardOpened) {
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
    }

    public boolean isSoftKeyboardOpened() {
        return isSoftKeyboardOpened;
    }

    /**
     * Default value is zero (0)
     * @return last saved keyboard height in px
     */
    public int getLastSoftKeyboardHeightInPx() {
        return lastSoftKeyboardHeightInPx;
    }

    public void addSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.add(listener);
    }

    public void removeSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyOnSoftKeyboardOpened(int keyboardHeightInPx) {
        this.lastSoftKeyboardHeightInPx = keyboardHeightInPx;

        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardOpened(keyboardHeightInPx);
            }
        }
    }

    private void notifyOnSoftKeyboardClosed() {
        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardClosed();
            }
        }
    }
}
