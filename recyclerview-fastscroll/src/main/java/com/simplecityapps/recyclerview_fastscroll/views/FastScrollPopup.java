/*
 * Copyright (c) 2016 Tim Malseed
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.simplecityapps.recyclerview_fastscroll.views;

import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.simplecityapps.recyclerview_fastscroll.utils.Utils;

public class FastScrollPopup {

    private final FastScrollRecyclerView mRecyclerView;

    private final Resources mRes;

    private int mBackgroundSize;
    private int mCornerRadius;

    private final Path mBackgroundPath = new Path();
    private final RectF mBackgroundRect = new RectF();
    private final Paint mBackgroundPaint;
    private int mBackgroundColor = 0xff000000;

    private final Rect mInvalidateRect = new Rect();
    private final Rect mTmpRect = new Rect();

    // The absolute bounds of the fast scroller bg
    private final Rect mBgBounds = new Rect();

    private String mSectionName;

    private final Paint mTextPaint;
    private final Rect mTextBounds = new Rect();

    private float mAlpha = 1;

    private ObjectAnimator mAlphaAnimator;
    private boolean mVisible;

    @FastScroller.PopupTextVerticalAlignmentMode private int mTextVerticalAlignmentMode;
    @FastScroller.PopupPosition private int mPosition;

    FastScrollPopup(Resources resources, FastScrollRecyclerView recyclerView) {

        mRes = resources;

        mRecyclerView = recyclerView;

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setAlpha(0);

        setTextSize(Utils.toScreenPixels(mRes, 32));
        setBackgroundSize(Utils.toPixels(mRes, 62));
    }

    public void setBgColor(int color) {
        mBackgroundColor = color;
        mBackgroundPaint.setColor(color);
        mRecyclerView.invalidate(mBgBounds);
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        mRecyclerView.invalidate(mBgBounds);
    }

    public void setTextSize(int size) {
        mTextPaint.setTextSize(size);
        mRecyclerView.invalidate(mBgBounds);
    }

    public void setBackgroundSize(int size) {
        mBackgroundSize = size;
        mCornerRadius = mBackgroundSize / 2;
        mRecyclerView.invalidate(mBgBounds);
    }

    public void setTypeface(Typeface typeface) {
        mTextPaint.setTypeface(typeface);
        mRecyclerView.invalidate(mBgBounds);
    }

    /**
     * Animates the visibility of the fast scroller popup.
     */
    public void animateVisibility(boolean visible) {
        if (mVisible != visible) {
            mVisible = visible;
            if (mAlphaAnimator != null) {
                mAlphaAnimator.cancel();
            }
            mAlphaAnimator = ObjectAnimator.ofFloat(this, "alpha", visible ? 1f : 0f);
            mAlphaAnimator.setDuration(visible ? 200 : 150);
            mAlphaAnimator.start();
        }
    }

    // Setter/getter for the popup alpha for animations
    @SuppressWarnings("unused")
    @Keep
    public void setAlpha(float alpha) {
        mAlpha = alpha;
        mRecyclerView.invalidate(mBgBounds);
    }

    @SuppressWarnings("unused")
    @Keep
    public float getAlpha() {
        return mAlpha;
    }

    public void setPopupTextVerticalAlignmentMode(@FastScroller.PopupTextVerticalAlignmentMode int mode) {
        mTextVerticalAlignmentMode = mode;
    }

    @SuppressWarnings("unused")
    @FastScroller.PopupTextVerticalAlignmentMode
    public int getPopupTextVerticalAlignmentMode() {
        return mTextVerticalAlignmentMode;
    }

    public void setPopupPosition(@FastScroller.PopupPosition int position) {
        mPosition = position;
    }

    @SuppressWarnings("unused")
    @FastScroller.PopupPosition
    public int getPopupPosition() {
        return mPosition;
    }

    private float[] createRadii() {
        if (mPosition == FastScroller.PopupPosition.CENTER) {
            return new float[]{mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius};
        }

        if (Utils.isRtl(mRes)) {
            return new float[]{mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, 0, 0};
        } else {
            return new float[]{mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, 0, 0, mCornerRadius, mCornerRadius};
        }
    }

    public void draw(Canvas canvas) {
        if (isVisible()) {
            // Draw the fast scroller popup
            int restoreCount = canvas.save();
            canvas.translate(mBgBounds.left, mBgBounds.top);
            mTmpRect.set(mBgBounds);
            mTmpRect.offsetTo(0, 0);

            mBackgroundPath.reset();
            mBackgroundRect.set(mTmpRect);

            float[] radii = createRadii();
            float baselinePosition;
            if (mTextVerticalAlignmentMode == FastScroller.PopupTextVerticalAlignmentMode.FONT_METRICS) {
                Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
                baselinePosition = (mBgBounds.height() - fontMetrics.ascent - fontMetrics.descent) / 2f;
            } else {
                baselinePosition = (mBgBounds.height() + mTextBounds.height()) / 2f;
            }

            mBackgroundPath.addRoundRect(mBackgroundRect, radii, Path.Direction.CW);

            mBackgroundPaint.setAlpha((int) (Color.alpha(mBackgroundColor) * mAlpha));
            mTextPaint.setAlpha((int) (mAlpha * 255));
            canvas.drawPath(mBackgroundPath, mBackgroundPaint);
            canvas.drawText(
                    mSectionName,
                    (mBgBounds.width() - mTextBounds.width()) / 2f,
                    baselinePosition,
                    mTextPaint
            );
            canvas.restoreToCount(restoreCount);
        }
    }

    public void setSectionName(String sectionName) {
        if (!sectionName.equals(mSectionName)) {
            mSectionName = sectionName;
            mTextPaint.getTextBounds(sectionName, 0, sectionName.length(), mTextBounds);
            // Update the width to use measureText since that is more accurate
            mTextBounds.right = (int) (mTextBounds.left + mTextPaint.measureText(sectionName));
        }
    }

    /**
     * Updates the bounds for the fast scroller.
     *
     * @return the invalidation rect for this update.
     */
    public Rect updateFastScrollerBounds(FastScrollRecyclerView recyclerView, int thumbOffsetY) {
        mInvalidateRect.set(mBgBounds);

        if (isVisible()) {
            // Calculate the dimensions and position of the fast scroller popup
            int edgePadding = recyclerView.getScrollBarWidth();
            int bgPadding = Math.round((mBackgroundSize - mTextBounds.height()) / 10f) * 5;
            int bgHeight = mBackgroundSize;
            int bgWidth = Math.max(mBackgroundSize, mTextBounds.width() + (2 * bgPadding));
            if (mPosition == FastScroller.PopupPosition.CENTER) {
                mBgBounds.left = (recyclerView.getWidth() - bgWidth) / 2;
                mBgBounds.right = mBgBounds.left + bgWidth;
                mBgBounds.top = (recyclerView.getHeight() - bgHeight) / 2;
            } else {
                if (Utils.isRtl(mRes)) {
                    mBgBounds.left = (2 * recyclerView.getScrollBarWidth());
                    mBgBounds.right = mBgBounds.left + bgWidth;
                } else {
                    mBgBounds.right = recyclerView.getWidth() - (2 * recyclerView.getScrollBarWidth());
                    mBgBounds.left = mBgBounds.right - bgWidth;
                }
                if (mRecyclerView.getClipToPadding()) {
                    mBgBounds.top = recyclerView.getPaddingTop() - recyclerView.getPaddingBottom() +
                        thumbOffsetY - bgHeight + recyclerView.getScrollBarThumbHeight() / 2;
                    mBgBounds.top = Math.max(recyclerView.getPaddingTop() + edgePadding,
                                             Math.min(mBgBounds.top, recyclerView.getPaddingTop() +
                                                 recyclerView.getHeight() - edgePadding - bgHeight));
                } else {
                    mBgBounds.top = thumbOffsetY - bgHeight + recyclerView.getScrollBarThumbHeight() / 2;
                    mBgBounds.top = Math.max(edgePadding, Math.min(mBgBounds.top, recyclerView.getHeight() - edgePadding - bgHeight));
                }
            }
            mBgBounds.bottom = mBgBounds.top + bgHeight;
        } else {
            mBgBounds.setEmpty();
        }

        // Combine the old and new fast scroller bounds to create the full invalidate rect
        mInvalidateRect.union(mBgBounds);
        return mInvalidateRect;
    }

    public boolean isVisible() {
        return (mAlpha > 0f) && (!TextUtils.isEmpty(mSectionName));
    }
}
