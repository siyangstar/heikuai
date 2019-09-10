/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：可滑动的标签控件
 *
 *
 * 创建标识：zhaosy 20150508
 */
package com.cqsynet.swifi.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.R;

import java.util.Locale;

public class SlidingPagerTabStrip extends HorizontalScrollView {

	public interface IconTabProvider {
		int getPageIconResId(int position);
	}

	private static final int[] ATTRS = new int[] {
		android.R.attr.textSize,
		android.R.attr.textColor
    };

	private LinearLayout.LayoutParams defaultTabLayoutParams;
	private LinearLayout.LayoutParams expandedTabLayoutParams;
	private final PageListener pageListener = new PageListener();
	public OnPageChangeListener delegatePageListener;
	private LinearLayout tabsContainer;
	private ViewPager pager;
	private int tabCount;
	private int currentPosition = 0;
	private float currentPositionOffset = 0f;
	private Paint rectPaint;
	private Paint dividerPaint;
	private float indicatorRadius = 4f;
    private int indicatorWidth = -1; // -1表示自适应宽度
	private int indicatorColor = 0xFF666666;
	private int underlineColor = 0x1A000000;
	private int dividerColor = 0x1A000000;
	private boolean shouldExpand = false;
	private boolean textAllCaps = true;
	private int scrollOffset = 52;
	private int indicatorHeight = 8;
	private int underlineHeight = 2;
	private int dividerPadding = 12;
	private int tabPadding = 24;
	private int dividerWidth = 1;
	private int tabTextSize = 12;
	private int tabSelectedTextSize = 20;
	private int tabTextColor = 0xFF666666;
	private int tabSelectedTextColor = 0xFFDDDDDD;
	private Typeface tabTypeface = null;
	private int tabTypefaceStyle = Typeface.NORMAL;
	private int lastScrollX = 0;
	private int tabBackgroundResId = R.color.transparent;
	private Locale locale;	
	private SPTSOnPageChangedListener mSPTSOnPageChangedListener;

	public SlidingPagerTabStrip(Context context) {
		this(context, null);
	}

	public SlidingPagerTabStrip(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressWarnings("ResourceType")
	public SlidingPagerTabStrip(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setFillViewport(true);
		setWillNotDraw(false);

		tabsContainer = new LinearLayout(context);
		tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
		tabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(tabsContainer);

		DisplayMetrics dm = getResources().getDisplayMetrics();

		scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
		indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
		underlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
		dividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding, dm);
		tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
		dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);
		tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);

		//获取系统样式
		TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

		tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
		tabTextColor = a.getColor(1, tabTextColor);

		a.recycle();

		//获取自定义样式
		a = context.obtainStyledAttributes(attrs, R.styleable.SlidingPagerTabStrip);

		indicatorColor = a.getColor(R.styleable.SlidingPagerTabStrip_pstsIndicatorColor, indicatorColor);
		underlineColor = a.getColor(R.styleable.SlidingPagerTabStrip_pstsUnderlineColor, underlineColor);
		dividerColor = a.getColor(R.styleable.SlidingPagerTabStrip_pstsDividerColor, dividerColor);
		indicatorHeight = a.getDimensionPixelSize(R.styleable.SlidingPagerTabStrip_pstsIndicatorHeight, indicatorHeight);
		underlineHeight = a.getDimensionPixelSize(R.styleable.SlidingPagerTabStrip_pstsUnderlineHeight, underlineHeight);
		dividerPadding = a.getDimensionPixelSize(R.styleable.SlidingPagerTabStrip_pstsDividerPadding, dividerPadding);
		tabPadding = a.getDimensionPixelSize(R.styleable.SlidingPagerTabStrip_pstsTabPaddingLeftRight, tabPadding);
		tabBackgroundResId = a.getResourceId(R.styleable.SlidingPagerTabStrip_pstsTabBackground, tabBackgroundResId);
		shouldExpand = a.getBoolean(R.styleable.SlidingPagerTabStrip_pstsShouldExpand, shouldExpand);
		scrollOffset = a.getDimensionPixelSize(R.styleable.SlidingPagerTabStrip_pstsScrollOffset, scrollOffset);
		textAllCaps = a.getBoolean(R.styleable.SlidingPagerTabStrip_pstsTextAllCaps, textAllCaps);

		a.recycle();

		rectPaint = new Paint();
		rectPaint.setAntiAlias(true);
		rectPaint.setStyle(Style.FILL);

		dividerPaint = new Paint();
		dividerPaint.setAntiAlias(true);
		dividerPaint.setStrokeWidth(dividerWidth);

		defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

		if (locale == null) {
			locale = getResources().getConfiguration().locale;
		}
	}

	public void setViewPager(ViewPager pager, SPTSOnPageChangedListener listener) {
		this.pager = pager;
		mSPTSOnPageChangedListener = listener;
		if (pager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}
		pager.setOnPageChangeListener(pageListener);
		notifyDataSetChanged();
	}

	public void setOnPageChangeListener(OnPageChangeListener listener) {
		this.delegatePageListener = listener;
	}

	public void notifyDataSetChanged() {
		if(pager == null || pager.getAdapter() == null) {
			return;
		}

		tabsContainer.removeAllViews();

		tabCount = pager.getAdapter().getCount();

		for (int i = 0; i < tabCount; i++) {

			if (pager.getAdapter() instanceof IconTabProvider) {
				addIconTab(i, ((IconTabProvider) pager.getAdapter()).getPageIconResId(i));
			} else {
				addTextTab(i, pager.getAdapter().getPageTitle(i).toString());
			}

		}

		updateTabStyles();

		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				} else {
					getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
				currentPosition = pager.getCurrentItem();
				scrollToChild(currentPosition, 0);
			}
		});

	}

	private void addTextTab(final int position, String title) {
		TextView tab = new TextView(getContext());
		tab.setText(title);
		tab.setGravity(Gravity.CENTER);
		tab.setSingleLine();
		addTab(position, tab);
	}

	private void addIconTab(final int position, int resId) {
		ImageButton tab = new ImageButton(getContext());
		tab.setImageResource(resId);
		addTab(position, tab);
	}

	private void addTab(final int position, View tab) {
		tab.setFocusable(true);
		tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                mSPTSOnPageChangedListener.onTabClick(position);
				pager.setCurrentItem(position);
			}
		});

		tab.setPadding(tabPadding, 0, tabPadding, 0);
		tabsContainer.addView(tab, position, shouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);
	}

	private void updateTabStyles() {
		for (int i = 0; i < tabCount; i++) {
			View v = tabsContainer.getChildAt(i);
			v.setBackgroundResource(tabBackgroundResId);
			if (v instanceof TextView) {
				TextView tab = (TextView) v;
				if(i == currentPosition) {
					tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabSelectedTextSize);
					tab.setTextColor(tabSelectedTextColor);
				} else {
					tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
					tab.setTextColor(tabTextColor);
				}
				tab.setTypeface(tabTypeface, tabTypefaceStyle);
//				if (textAllCaps) {
//					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//						tab.setAllCaps(true);
//					} else {
//						tab.setText(tab.getText().toString().toUpperCase(locale));
//					}
//				}
			}
		}

	}

	private void scrollToChild(int position, int offset) {
		if (tabCount == 0) {
			return;
		}
		int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;
		if (position > 0 || offset > 0) {
			newScrollX -= scrollOffset;
		}
		if (newScrollX != lastScrollX) {
			lastScrollX = newScrollX;
			scrollTo(newScrollX, 0);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isInEditMode() || tabCount == 0) {
			return;
		}
		final int height = getHeight();
		rectPaint.setColor(indicatorColor);
		//选中tab下面的标识
		View currentTab = tabsContainer.getChildAt(currentPosition);
		float lineLeft = currentTab.getLeft();
		float lineRight = currentTab.getRight();
		//设置偏移
		if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {
			View nextTab = tabsContainer.getChildAt(currentPosition + 1);
			final float nextTabLeft = nextTab.getLeft();
			final float nextTabRight = nextTab.getRight();

			lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
			lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
		}

//		canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, rectPaint);
        if(indicatorWidth != -1) {
            //左右留白
            float margin = (lineRight - lineLeft - indicatorWidth) / 2;
            if (margin < 0) {
                margin = 0;
            }
            RectF rectF = new RectF(lineLeft + margin, height - indicatorHeight, lineRight - margin, height);
            canvas.drawRoundRect(rectF, indicatorRadius, indicatorRadius, rectPaint);
        } else {
            RectF rectF = new RectF(lineLeft, height - indicatorHeight, lineRight, height);
            canvas.drawRoundRect(rectF, indicatorRadius, indicatorRadius, rectPaint);
        }

		//画背景下划线
		rectPaint.setColor(underlineColor);
		canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, rectPaint);

		//画间隔线
		dividerPaint.setColor(dividerColor);
		for (int i = 0; i < tabCount - 1; i++) {
			View tab = tabsContainer.getChildAt(i);
			canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(), height - dividerPadding, dividerPaint);
		}
	}

	private class PageListener implements OnPageChangeListener {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			currentPosition = position;
			currentPositionOffset = positionOffset;
			scrollToChild(position, (int) (positionOffset * tabsContainer.getChildAt(position).getWidth()));
			invalidate();
			if (delegatePageListener != null) {
				delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
			
			mSPTSOnPageChangedListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_IDLE) {
				scrollToChild(pager.getCurrentItem(), 0);
			}
			if (delegatePageListener != null) {
				delegatePageListener.onPageScrollStateChanged(state);
			}
			
			mSPTSOnPageChangedListener.onPageScrollStateChanged(state);
		}

		@Override
		public void onPageSelected(int position) {
			if (delegatePageListener != null) {
				delegatePageListener.onPageSelected(position);
			}
	
			for (int i = 0; i < tabCount; i++) {
				View v = tabsContainer.getChildAt(i);
				if (v instanceof TextView) {
					TextView tab = (TextView) v;
					if(i == position) {
						tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabSelectedTextSize);
						tab.setTextColor(tabSelectedTextColor);
					} else {
						tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
						tab.setTextColor(tabTextColor);
					}
				}
			}
			
			mSPTSOnPageChangedListener.onPageSelected(position);
		}

	}

    public void setIndicatorWidth(int width) {
        this.indicatorWidth = width;
        invalidate();
    }

    public float getIndicatorWidth() {
        return this.indicatorWidth;
    }

	public void setIndicatorRadius(float r) {
		this.indicatorRadius = r;
		invalidate();
	}

	public float getIndicatorRadius() {
		return this.indicatorRadius;
	}

	public void setIndicatorColor(int indicatorColor) {
		this.indicatorColor = indicatorColor;
		invalidate();
	}

	public void setIndicatorColorResource(int resId) {
		this.indicatorColor = getResources().getColor(resId);
		invalidate();
	}

	public int getIndicatorColor() {
		return this.indicatorColor;
	}

	public void setIndicatorHeight(int indicatorLineHeightPx) {
		this.indicatorHeight = indicatorLineHeightPx;
		invalidate();
	}

	public int getIndicatorHeight() {
		return indicatorHeight;
	}

	public void setUnderlineColor(int underlineColor) {
		this.underlineColor = underlineColor;
		invalidate();
	}

	public void setUnderlineColorResource(int resId) {
		this.underlineColor = getResources().getColor(resId);
		invalidate();
	}

	public int getUnderlineColor() {
		return underlineColor;
	}

	public void setDividerColor(int dividerColor) {
		this.dividerColor = dividerColor;
		invalidate();
	}

	public void setDividerColorResource(int resId) {
		this.dividerColor = getResources().getColor(resId);
		invalidate();
	}

	public int getDividerColor() {
		return dividerColor;
	}

	public void setUnderlineHeight(int underlineHeightPx) {
		this.underlineHeight = underlineHeightPx;
		invalidate();
	}

	public int getUnderlineHeight() {
		return underlineHeight;
	}

	public void setDividerPadding(int dividerPaddingPx) {
		this.dividerPadding = dividerPaddingPx;
		invalidate();
	}

	public int getDividerPadding() {
		return dividerPadding;
	}

	public void setScrollOffset(int scrollOffsetPx) {
		this.scrollOffset = scrollOffsetPx;
		invalidate();
	}

	public int getScrollOffset() {
		return scrollOffset;
	}

	public void setShouldExpand(boolean shouldExpand) {
		this.shouldExpand = shouldExpand;
		requestLayout();
	}

	public boolean getShouldExpand() {
		return shouldExpand;
	}

	public boolean isTextAllCaps() {
		return textAllCaps;
	}

	public void setAllCaps(boolean textAllCaps) {
		this.textAllCaps = textAllCaps;
	}

	public void setTextSize(int textSizePx) {
		this.tabTextSize = textSizePx;
		updateTabStyles();
	}

	public int getTextSize() {
		return tabTextSize;
	}

	public void setSelectedTextSize(int textSizePx) {
		this.tabSelectedTextSize = textSizePx;
		updateTabStyles();
	}
	
	public int getSelectedTextSize() {
		return tabSelectedTextSize;
	}

	public void setTextColor(int textColor) {
		this.tabTextColor = textColor;
		updateTabStyles();
	}

	public void setTextColorResource(int resId) {
		this.tabTextColor = getResources().getColor(resId);
		updateTabStyles();
	}

	public int getTextColor() {
		return tabTextColor;
	}
	
	public void setSelectedTextColor(int textColor) {
		this.tabSelectedTextColor = textColor;
		updateTabStyles();
	}

	public void setSelectedTextColorResource(int resId) {
		this.tabSelectedTextColor = getResources().getColor(resId);
		updateTabStyles();
	}

	public int getSelectedTextColor() {
		return tabSelectedTextColor;
	}

	public void setTypeface(Typeface typeface, int style) {
		this.tabTypeface = typeface;
		this.tabTypefaceStyle = style;
		updateTabStyles();
	}

	public void setTabBackground(int resId) {
		this.tabBackgroundResId = resId;
	}

	public int getTabBackground() {
		return tabBackgroundResId;
	}

	public void setTabPaddingLeftRight(int paddingPx) {
		this.tabPadding = paddingPx;
		updateTabStyles();
	}

	public int getTabPaddingLeftRight() {
		return tabPadding;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		currentPosition = savedState.currentPosition;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.currentPosition = currentPosition;
		return savedState;
	}

	static class SavedState extends BaseSavedState {
		int currentPosition;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentPosition = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentPosition);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
	
	public interface SPTSOnPageChangedListener {
        void onPageScrollStateChanged(int position);
        void onPageScrolled(int position, float arg1, int arg2);
    	void onPageSelected(int position);
		void onTabClick(int position);
	}
}
