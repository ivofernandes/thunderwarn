package com.thunderwarn.thunderwarn.common.slider;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONObject;

/**
 * Created by Ivo on 28-06-2015.
 */
public abstract class SlidingItem extends LinearLayout{

    public SlidingItem(Context context) {
        super(context);
    }

    public abstract View viewForPosition(ViewGroup container, int position);



}
