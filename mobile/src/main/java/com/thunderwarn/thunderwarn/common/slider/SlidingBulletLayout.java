package com.thunderwarn.thunderwarn.common.slider;


import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.configuration.LayoutManager;


public class SlidingBulletLayout extends HorizontalScrollView {

    private static final int SIZE_NORMAL = 12;
    private static final int SIZE_SELECTED = 36;

    private LayoutManager layoutManager = LayoutManager.getInstance();
    private SharedResources sharedResources = SharedResources.getInstance();
    private int position = 0;

    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     */
    public interface TabColorizer {

        /**
         * @return return the color of the indicator used when {@code position} is selected.
         */
        int getIndicatorColor(int position);

        /**
         * @return return the color of the divider drawn to the right of {@code position}.
         */
        int getDividerColor(int position);

    }

    private static final int TITLE_OFFSET_DIPS = 24;
    private static final int TAB_VIEW_PADDING_DIPS = 16;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 12;

    private int mTitleOffset;

    private int mTabViewLayoutId;
    private int mTabViewTextViewId;

    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;


    private LinearLayout bullets;

    public SlidingBulletLayout(Context context) {
        this(context, null);
    }

    public SlidingBulletLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingBulletLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);
        // Make sure that the Tab Strips fills this View
        setFillViewport(true);

        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

        this.bullets = new LinearLayout(context);
        this.addView(this.bullets, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }


    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setViewPager(ViewPager viewPager) {
        this.bullets.removeAllViews();

        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.setOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }

    private void populateTabStrip() {
        final PagerAdapter adapter = mViewPager.getAdapter();
        int count = adapter.getCount();
        for (int i = 0; i < count ; i++) {
            ImageView tabView = createBullet(false);

            this.bullets.addView(tabView);
        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.bullets.getLayoutParams();

        if(params == null){
            params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
        }

        params.setMargins(0,20,0,0);
        this.bullets.setLayoutParams(params);

        this.bullets.setGravity(Gravity.CENTER);

    }

    private ImageView createBullet(boolean selected) {
        final View.OnClickListener tabClickListener = new TabClickListener();

        ImageView tabView = new ImageView(sharedResources.getContext());
        tabView.setScaleType(ImageView.ScaleType.FIT_XY);
        tabView.setMaxHeight(7);
        tabView.setMaxWidth(7);
        tabView.setBackgroundColor(LayoutManager.getInstance().getSmoothForegroundColor());

        tabView.setOnClickListener(tabClickListener);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();

        if(params == null){
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        params.setMargins(5,5,5,5);
        tabView.setLayoutParams(params);

        // Choose the image file
        tabView.setImageResource(R.drawable.bb_slider);
        if(selected){
            tabView.setImageResource(R.drawable.bb_slider_selected);
        }

        return tabView;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mViewPager != null) {
            scrollToTab(this.position, 0);
        }
    }

    private void scrollToTab(int tabIndex, int positionOffset) {

        final View.OnClickListener tabClickListener = new TabClickListener();

        final int tabStripChildCount = this.bullets.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }

        // Clear the panel
        boolean isEmpty = this.bullets.getChildCount() == 0;

        // Put everything normal
        for (int i=0 ; i < tabStripChildCount ; i++){
            boolean selected = false;
            if(i == tabIndex){
                selected = true;
            }

            if(isEmpty) {
                ImageView tabView = createBullet(selected);
                this.bullets.addView(tabView);
            }else if(this.bullets.getChildCount() > i){
                View view = this.bullets.getChildAt(i);
                if(view instanceof  ImageView) {
                    ImageView tabView = (ImageView) view;
                    if(selected){
                        tabView.setImageResource(R.drawable.bb_slider_selected);
                    }else{
                        tabView.setImageResource(R.drawable.bb_slider);

                    }
                }
            }
        }
    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;
        private boolean inited = false;
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = SlidingBulletLayout.this.bullets.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            View selectedTitle = SlidingBulletLayout.this.bullets.getChildAt(position);
            int extraOffset = (selectedTitle != null)
                    ? (int) (positionOffset * selectedTitle.getWidth())
                    : 0;


            scrollToTab(position, extraOffset);
            inited = true;
            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                scrollToTab(position, 0);
            }

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }

    }

    private class TabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < bullets.getChildCount(); i++) {
                if (v == bullets.getChildAt(i)) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    }

}
