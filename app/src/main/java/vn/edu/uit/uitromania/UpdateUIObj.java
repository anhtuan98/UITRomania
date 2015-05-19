package vn.edu.uit.uitromania;

/**
 * Created by natuan on 16/5/15.
 */

import android.graphics.Bitmap;

public class UpdateUIObj {
    Bitmap bm;
    String text;
    public UpdateUIObj(){}
    public Bitmap getBm() {
        return bm;
    }
    public void setBm(Bitmap bm) {
        this.bm = bm;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

}
