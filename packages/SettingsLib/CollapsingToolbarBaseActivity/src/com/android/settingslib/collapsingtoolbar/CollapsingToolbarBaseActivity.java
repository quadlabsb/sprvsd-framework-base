/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settingslib.collapsingtoolbar;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.resources.TextAppearanceConfig;

/**
 * A base Activity that has a collapsing toolbar layout is used for the activities intending to
 * enable the collapsing toolbar function.
 */
public class CollapsingToolbarBaseActivity extends FragmentActivity implements
        AppBarLayout.OnOffsetChangedListener {

    private static final int TOOLBAR_MAX_LINE_NUMBER = 2;
    private static final int FULLY_EXPANDED_OFFSET = 0;
    private static final String KEY_IS_TOOLBAR_COLLAPSED = "is_toolbar_collapsed";

    @Nullable
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Nullable
    private AppBarLayout mAppBarLayout;
    private boolean mIsToolbarCollapsed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Force loading font synchronously for collapsing toolbar layout
        TextAppearanceConfig.setShouldLoadFontSynchronously(true);
        super.setContentView(R.layout.collapsing_toolbar_base_layout);
        mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        mAppBarLayout = findViewById(R.id.app_bar);
        mAppBarLayout.addOnOffsetChangedListener(this);
        if (savedInstanceState != null) {
            mIsToolbarCollapsed = savedInstanceState.getBoolean(KEY_IS_TOOLBAR_COLLAPSED);
        }

        initCollapsingToolbar();
        disableCollapsingToolbarLayoutScrollingBehavior();

        final Toolbar toolbar = findViewById(R.id.action_bar);
        setActionBar(toolbar);

        // Enable title and home button by default
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        final ViewGroup parent = findViewById(R.id.content_frame);
        if (parent != null) {
            parent.removeAllViews();
        }
        LayoutInflater.from(this).inflate(layoutResID, parent);
    }

    @Override
    public void setContentView(View view) {
        ((ViewGroup) findViewById(R.id.content_frame)).addView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        ((ViewGroup) findViewById(R.id.content_frame)).addView(view, params);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (mCollapsingToolbarLayout != null) {
            mCollapsingToolbarLayout.setTitle(title);
        } else {
            super.setTitle(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        if (mCollapsingToolbarLayout != null) {
            mCollapsingToolbarLayout.setTitle(getText(titleId));
        } else {
            super.setTitle(titleId);
        }
    }

    @Override
    public boolean onNavigateUp() {
        if (!super.onNavigateUp()) {
            finishAfterTransition();
        }
        return true;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        if (offset == FULLY_EXPANDED_OFFSET) {
            mIsToolbarCollapsed = false;
        } else {
            mIsToolbarCollapsed = true;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isChangingConfigurations()) {
            outState.putBoolean(KEY_IS_TOOLBAR_COLLAPSED, mIsToolbarCollapsed);
        }
    }

    /**
     * Returns an instance of collapsing toolbar.
     */
    @Nullable
    public CollapsingToolbarLayout getCollapsingToolbarLayout() {
        return mCollapsingToolbarLayout;
    }

    /**
     * Return an instance of app bar.
     */
    @Nullable
    public AppBarLayout getAppBarLayout() {
        return mAppBarLayout;
    }

    private void disableCollapsingToolbarLayoutScrollingBehavior() {
        if (mAppBarLayout == null) {
            return;
        }
        final CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        final AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(
                new AppBarLayout.Behavior.DragCallback() {
                    @Override
                    public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                        return false;
                    }
                });
        params.setBehavior(behavior);
    }

    @SuppressWarnings("RestrictTo")
    private void initCollapsingToolbar() {
        if (mCollapsingToolbarLayout == null || mAppBarLayout == null) {
            return;
        }
        mCollapsingToolbarLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                if (mIsToolbarCollapsed) {
                    return;
                }
                final int count = mCollapsingToolbarLayout.getLineCount();
                if (count > TOOLBAR_MAX_LINE_NUMBER) {
                    final ViewGroup.LayoutParams lp = mCollapsingToolbarLayout.getLayoutParams();
                    lp.height = getResources()
                            .getDimensionPixelSize(R.dimen.toolbar_three_lines_height);
                    mCollapsingToolbarLayout.setScrimVisibleHeightTrigger(
                            getResources().getDimensionPixelSize(
                                    R.dimen.scrim_visible_height_trigger_three_lines));
                    mCollapsingToolbarLayout.setLayoutParams(lp);
                } else if (count == TOOLBAR_MAX_LINE_NUMBER) {
                    final ViewGroup.LayoutParams lp = mCollapsingToolbarLayout.getLayoutParams();
                    lp.height = getResources()
                            .getDimensionPixelSize(R.dimen.toolbar_two_lines_height);
                    mCollapsingToolbarLayout.setScrimVisibleHeightTrigger(
                            getResources().getDimensionPixelSize(
                                    R.dimen.scrim_visible_height_trigger_two_lines));
                    mCollapsingToolbarLayout.setLayoutParams(lp);
                }
            }
        });
    }
}
