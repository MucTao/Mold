@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import java.lang.reflect.Field
import kotlin.math.abs

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/08/02
 * desc  : utils about keyboard
 * </pre> *
 */
object KeyboardUtils {
    /**/
    /////////////////////////////////////////////////////////////////////// */ // interface
    /**/
    /////////////////////////////////////////////////////////////////////// */
    interface OnSoftInputChangedListener {
        fun onSoftInputChanged(height: Int)
    }

    private const val TAG_ON_GLOBAL_LAYOUT_LISTENER = -8

    /** Show the soft input. */
    fun showSoftInput() {
        val imm: InputMethodManager =
            Utils.app.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager? ?: return
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    /** Show the soft input. */
    fun showSoftInput(activity: Activity?) {
        if (activity == null) {
            return
        }
        if (!isSoftInputVisible(activity)) {
            toggleSoftInput()
        }
    }

    /**
     * Show the soft input.
     *
     * @param view The view.
     * @param flags Provides additional operating flags. Currently, may be 0 or have the
     *   [InputMethodManager.SHOW_IMPLICIT] bit set.
     */
    fun showSoftInput(
        view: View,
        flags: Int = 0,
    ) {
        val imm: InputMethodManager =
            Utils.app.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager? ?: return
        view.setFocusable(true)
        view.setFocusableInTouchMode(true)
        view.requestFocus()
        imm.showSoftInput(
            view,
            flags,
            object : ResultReceiver(Handler()) {
                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    if (
                        resultCode == InputMethodManager.RESULT_UNCHANGED_HIDDEN ||
                        resultCode == InputMethodManager.RESULT_HIDDEN
                    ) {
                        toggleSoftInput()
                    }
                }
            },
        )
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    /**
     * Hide the soft input.
     *
     * @param activity The activity.
     */
    fun hideSoftInput(activity: Activity?) {
        if (activity == null) {
            return
        }
        hideSoftInput(activity.window)
    }

    /**
     * Hide the soft input.
     *
     * @param window The window.
     */
    fun hideSoftInput(window: Window?) {
        if (window == null) {
            return
        }
        var view: View? = window.currentFocus
        if (view == null) {
            val decorView: View = window.decorView
            val focusView: View? = decorView.findViewWithTag("keyboardTagView")
            if (focusView == null) {
                view = EditText(window.context)
                view.tag = "keyboardTagView"
                (decorView as ViewGroup).addView(view, 0, 0)
            } else {
                view = focusView
            }
            view.requestFocus()
        }
        hideSoftInput(view)
    }

    /**
     * Hide the soft input.
     *
     * @param view The view.
     */
    fun hideSoftInput(view: View) {
        val imm: InputMethodManager? =
            Utils.app.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private var millis: Long = 0

    /**
     * Hide the soft input.
     *
     * @param activity The activity.
     */
    fun hideSoftInputByToggle(activity: Activity?) {
        if (activity == null) {
            return
        }
        val nowMillis: Long = SystemClock.elapsedRealtime()
        val delta = nowMillis - millis
        if (abs(delta) > 500 && isSoftInputVisible(activity)) {
            toggleSoftInput()
        }
        millis = nowMillis
    }

    /** Toggle the soft input display or not. */
    fun toggleSoftInput() {
        val imm: InputMethodManager =
            Utils.app.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager? ?: return
        imm.toggleSoftInput(0, 0)
    }

    private var sDecorViewDelta = 0

    /**
     * Return whether soft input is visible.
     *
     * @param activity The activity.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isSoftInputVisible(activity: Activity): Boolean {
        return getDecorViewInvisibleHeight(activity.window) > 0
    }

    private fun getDecorViewInvisibleHeight(window: Window): Int {
        val decorView: View = window.decorView
        val outRect = Rect()
        decorView.getWindowVisibleDisplayFrame(outRect)
        Log.d(
            "KeyboardUtils",
            "getDecorViewInvisibleHeight: " + (decorView.bottom - outRect.bottom),
        )
        val delta: Int = abs(decorView.bottom - outRect.bottom)
        if (delta <= BarUtils.navBarHeight + BarUtils.getStatusBarHeight()) {
            sDecorViewDelta = delta
            return 0
        }
        return delta - sDecorViewDelta
    }

    /**
     * Register soft input changed listener.
     *
     * @param activity The activity.
     * @param listener The soft input changed listener.
     */
    fun registerSoftInputChangedListener(
        activity: Activity,
        listener: OnSoftInputChangedListener,
    ) {
        registerSoftInputChangedListener(activity.window, listener)
    }

    /**
     * Register soft input changed listener.
     *
     * @param window The window.
     * @param listener The soft input changed listener.
     */
    fun registerSoftInputChangedListener(
        window: Window,
        listener: OnSoftInputChangedListener,
    ) {
        val flags: Int = window.attributes.flags
        if ((flags and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) != 0) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
        val contentView: FrameLayout = window.findViewById(android.R.id.content)
        val decorViewInvisibleHeightPre = intArrayOf(getDecorViewInvisibleHeight(window))
        val onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener =
            ViewTreeObserver.OnGlobalLayoutListener {
                val height = getDecorViewInvisibleHeight(window)
                if (decorViewInvisibleHeightPre[0] != height) {
                    listener.onSoftInputChanged(height)
                    decorViewInvisibleHeightPre[0] = height
                }
            }
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener)
        contentView.setTag(TAG_ON_GLOBAL_LAYOUT_LISTENER, onGlobalLayoutListener)
    }

    /**
     * Unregister soft input changed listener.
     *
     * @param window The window.
     */
    fun unregisterSoftInputChangedListener(window: Window) {
        val contentView: View = window.findViewById(android.R.id.content) ?: return
        val tag: Any? = contentView.getTag(TAG_ON_GLOBAL_LAYOUT_LISTENER)
        if (tag is ViewTreeObserver.OnGlobalLayoutListener) {
            contentView
                .getViewTreeObserver()
                .removeOnGlobalLayoutListener(tag)
            // 这里会发生内存泄漏 如果不设置为null
            contentView.setTag(TAG_ON_GLOBAL_LAYOUT_LISTENER, null)
        }
    }

    /**
     * Fix the bug of 5497 in Android.
     *
     * Don't set adjustResize
     *
     * @param activity The activity.
     */
    fun fixAndroidBug5497(activity: Activity) {
        fixAndroidBug5497(activity.window)
    }

    /**
     * Fix the bug of 5497 in Android.
     *
     * It will clean the adjustResize
     *
     * @param window The window.
     */
    fun fixAndroidBug5497(window: Window) {
        val softInputMode: Int = window.attributes.softInputMode
        window.setSoftInputMode(
            softInputMode and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE.inv()
        )
        val contentView: FrameLayout = window.findViewById(android.R.id.content)
        val contentViewChild: View = contentView.getChildAt(0)
        val paddingBottom: Int = contentViewChild.paddingBottom
        val contentViewInvisibleHeightPre5497 = intArrayOf(getContentViewInvisibleHeight(window))
        contentView
            .getViewTreeObserver()
            .addOnGlobalLayoutListener {
                val height = getContentViewInvisibleHeight(window)
                if (contentViewInvisibleHeightPre5497[0] != height) {
                    contentViewChild.setPadding(
                        contentViewChild.getPaddingLeft(),
                        contentViewChild.paddingTop,
                        contentViewChild.getPaddingRight(),
                        paddingBottom + getDecorViewInvisibleHeight(window),
                    )
                    contentViewInvisibleHeightPre5497[0] = height
                }
            }
    }

    private fun getContentViewInvisibleHeight(window: Window): Int {
        val contentView: View = window.findViewById(android.R.id.content) ?: return 0
        val outRect = Rect()
        contentView.getWindowVisibleDisplayFrame(outRect)
        Log.d(
            "KeyboardUtils",
            "getContentViewInvisibleHeight: " + (contentView.bottom - outRect.bottom),
        )
        val delta: Int = abs(contentView.bottom - outRect.bottom)
        if (delta <= BarUtils.getStatusBarHeight() + BarUtils.navBarHeight) {
            return 0
        }
        return delta
    }

    /**
     * Fix the leaks of soft input.
     *
     * @param activity The activity.
     */
    fun fixSoftInputLeaks(activity: Activity) {
        fixSoftInputLeaks(activity.window)
    }

    /**
     * Fix the leaks of soft input.
     *
     * @param window The window.
     */
    @SuppressLint("PrivateApi")
    fun fixSoftInputLeaks(window: Window) {
        val imm: InputMethodManager =
            Utils.app.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager? ?: return
        val leakViews = arrayOf("mLastSrvView", "mCurRootView", "mServedView", "mNextServedView")
        loop@ for (leakView in leakViews) {
            runCatching {
                val leakViewField: Field = InputMethodManager::class.java.getDeclaredField(leakView)
                if (!leakViewField.isAccessible) {
                    leakViewField.isAccessible = true
                }
                val obj: Any? = leakViewField.get(imm)
                if (obj !is View) {
                    continue@loop
                }
                val view: View = obj
                if (view.getRootView() == window.decorView.getRootView()) {
                    leakViewField.set(imm, null)
                }
            }
        }
    }

    /**
     * Click blank area to hide soft input.
     *
     * Copy the following code in ur activity.
     */
    fun clickBlankArea2HideSoftInput() {
        Log.i("KeyboardUtils", "Please refer to the following code.")
        /*
        override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                View v = getCurrentFocus();
                if (isShouldHideKeyboard(v, ev)) {
                    KeyboardUtils.hideSoftInput(this);
                }
            }
            return super.dispatchTouchEvent(ev);
        }

        // Return whether touch the view.
        private boolean isShouldHideKeyboard(View v, MotionEvent event) {
            if ((v instanceof EditText)) {
                int[] l = {0, 0};
                v.getLocationOnScreen(l);
                int left = l[0],
                        top = l[1],
                        bottom = top + v.getHeight(),
                        right = left + v.getWidth();
                return !(event.getRawX() > left && event.getRawX() < right
                        && event.getRawY() > top && event.getRawY() < bottom);
            }
            return false;
        }
        */
    }
}
