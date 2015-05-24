package vn.edu.uit.uitromania.common;

/**
 * Created by natuan on 16/5/15.
 */


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vn.edu.uit.uitromania.MainActivity;
import vn.edu.uit.uitromania.view.Result;

public class RecognitionAsyncTask extends AsyncTask<String, Result, Void> {
    MainActivity activity;
    Mat src;
    //	TextView tvCharacter;
//	TextView tvResultpublic
//	ImageView ivCharacter;
    Result obj = new Result();
    Mat rPlate;
    Mat rPlateClone;
    Bitmap rTextImg;
    String rTextPlate = "";
    List<Rect> rListPlates;

    public RecognitionAsyncTask(MainActivity activity, Mat src) {
        this.activity = activity;
        this.src = src;
        activity.teststtt = 2;
        rListPlates = new ArrayList<Rect>();
        rListPlates.addAll(Arrays.asList(activity.arrPlates));

    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
        activity.isRunning = true;
    }

    @Override
    protected Void doInBackground(String... params) {
        // TODO Auto-generated method stub
        //bat dau doan viet them xu ly bien so nghieng
        try {
            FormatPlates formatPlate = new FormatPlates();
            Mat newTransform = formatPlate.newTransform(src);
            Bitmap bmP = Bitmap.createBitmap(newTransform.width(), newTransform.height(), Config.ARGB_8888);
            Utils.matToBitmap(newTransform, bmP);
            obj.setBm(bmP);
            ArrayList<Mat> arrPlate = formatPlate.contourCharacter(newTransform);
            rTextPlate = "";
            for (int i = 0; i < arrPlate.size(); i++) {
                // String txt =activity.rec.reg(arrPlate.get(i));
                rTextPlate += activity.rec.reg(arrPlate.get(i));
            }

            obj.setText(rTextPlate);
            publishProgress(obj);
        } catch (Exception ex) {
            Log.e("Ex", ex.toString());
        }


        return null;
    }

    @Override
    protected void onProgressUpdate(Result... obj) {
        // TODO Auto-generated method stub
        super.onProgressUpdate(obj);
        activity.teststtt = 3;
        activity.iv.setImageBitmap(obj[0].getBm());
        //   activity.ivplate.setImageBitmap(obj[0].getBm1());
        activity.tv.setText(obj[0].getText());
    }

    @Override
    protected void onPostExecute(Void result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        activity.isRunning = false;

        activity.tvOCR.setText(rTextPlate);

    }


}
