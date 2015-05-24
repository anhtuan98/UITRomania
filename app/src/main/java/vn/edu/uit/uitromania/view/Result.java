package vn.edu.uit.uitromania.view;

/**
 * Created by natuan on 16/5/15.
 */

import android.graphics.Bitmap;

// Class de cap nhat thong tin tu asynctask len giao dien
public class Result {
    Bitmap bm;
    Bitmap bm1;
    String text;

    public Result() {
    }

    public Bitmap getBm() {
        return bm;
    }

    public void setBm(Bitmap bm) {
        this.bm = bm;
    }

    public Bitmap getBm1() {
        return bm1;
    }

    public void setBm1(Bitmap bm1) {
        this.bm1 = bm1;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
