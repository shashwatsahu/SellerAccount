package com.example.hp.selleraccount;

import android.graphics.drawable.Drawable;

/**
 * Created by hp on 18-03-2018.
 */

public class DrawerList {



    private static final String TAG = "DrawerList";


    String text;
    Drawable drawable;

    public DrawerList(String text, Drawable drawable){
        this.text = text;
        this.drawable = drawable;
    }

    public String getText(){
        return this.text;
    }

    public Drawable getDrawable() {
        //Log.i(TAG, "drawable" + drawable.toString());
        return this.drawable;
    }


}
