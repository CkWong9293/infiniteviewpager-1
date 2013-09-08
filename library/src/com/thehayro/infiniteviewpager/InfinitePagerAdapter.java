/*
 * Copyright (C) 2013 Onur-Hayri Bakici
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
package com.thehayro.infiniteviewpager;

import com.thehayro.infiniteviewpager.internal.Constants;
import com.thehayro.infiniteviewpager.internal.PageModel;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 * @param <T>
 */
public abstract class InfinitePagerAdapter<T> extends PagerAdapter {

    private static final int PAGE_COUNT = 3;

    private PageModel<T>[] mPageModels = new PageModel[PAGE_COUNT];

    private T mCurrentIndicator;

    public InfinitePagerAdapter(T initValue) {
        mCurrentIndicator = initValue;

    }

    @Override
    public final Object instantiateItem(final ViewGroup container, final int position) {
        if (Constants.DEBUG) {
            Log.i("InfiniteViewPager", String.format("instantiating position %s", position));
        }
        final PageModel<T> model = createPageModel(position);
        mPageModels[position] = model;
        container.addView(model.getParentView());
        return model;
    }

    void fillPage(final int position) {
        if (Constants.DEBUG) {
            Log.d("InfiniteViewPager", "setup Page " + position);
            printPageModels("before newPage");
        }
        final PageModel<T> oldModel = mPageModels[position];
        final PageModel<T> newModel = createPageModel(position);
        if (oldModel == null || newModel == null) {
            Log.w(Constants.LOG_TAG, "fillPage no model found " + oldModel + " " + newModel);
            return;
        }
        // moving the new created views to the page of the viewpager
        oldModel.removeAllChildren();
        for (final View newChild : newModel.getChildren()) {
            newModel.removeViewFromParent(newChild);
            oldModel.addChild(newChild);
        }

        mPageModels[position].setIndicator(newModel.getIndicator());
    }

    private PageModel<T> createPageModel(final int pagePosition) {
        final T indicator = getIndicatorFromPagePosition(pagePosition);
        final ViewGroup view = instantiateItem(indicator);

        return new PageModel<T>(view, indicator);
    }

    protected final T getCurrentIndicator() {
        return mCurrentIndicator;
    }

    public abstract T getNextIndicator();

    public abstract T getPreviousIndicator();

    public abstract ViewGroup instantiateItem(T indicator);

    public String getStringRepresentation(final T currentIndicator) {
        return "";
    }

    public T convertToIndicator(final String representation) {
        return getCurrentIndicator();
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public boolean isViewFromObject(final View view, final Object o) {
        return view == ((PageModel)o).getParentView();
    }

    private T getIndicatorFromPagePosition(final int pagePosition) {
        T indicator = null;
        switch (pagePosition) {
            case Constants.PAGE_POSITION_LEFT:
                indicator = getPreviousIndicator();
                break;
            case Constants.PAGE_POSITION_CENTER:
                indicator = getCurrentIndicator();
                break;
            case Constants.PAGE_POSITION_RIGHT:
                indicator = getNextIndicator();
                break;
        }
        return indicator;
    }

    void movePageContents(final int from, final int to) {
        final PageModel<T> fromModel = mPageModels[from];
        final PageModel<T> toModel = mPageModels[to];
        if (fromModel == null || toModel == null) {
            Log.w(Constants.LOG_TAG, "fillPage no model found " + fromModel + " " + toModel);
            return;
        }
        if (Constants.DEBUG) {
            Log.d("InfiniteViewPager",
                String.format("Moving page %s to %s, indicator from %s to %s", from, to,
                fromModel.getIndicator(), toModel.getIndicator()));
            printPageModels("before");
        }


        toModel.removeAllChildren();
        for (View view : fromModel.getChildren()) {
            fromModel.removeViewFromParent(view);
            toModel.addChild(view);
        }

        if (Constants.DEBUG) {
            printPageModels("transfer");
        }
        mPageModels[to].setIndicator(fromModel.getIndicator());
        if (Constants.DEBUG) {
            printPageModels("after");
        }
    }



    void setCurrentIndicator(final T indicator) {
        mCurrentIndicator = indicator;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        final PageModel model = (PageModel) object;
        container.removeView(model.getParentView());
    }




    // Debug related methods

    private void printPageModels(final String tag) {
        for (int i = 0; i < PAGE_COUNT; i++) {
            printPageModel(tag, mPageModels[i], i);
        }
    }

    private void printPageModel(final String tag, final PageModel model, int modelPos) {
        final String builder = String.format("%s: ModelPos %s, indicator %s, " +
            "Childcount %s viewChildCount %s tag %s",
            tag, modelPos,
            model.getIndicator(), model.getChildren().size(), model.getParentView().getChildCount(),
            model.getParentView().getTag());
        Log.d("InfiniteViewPager", builder);
    }


}
