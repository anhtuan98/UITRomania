package vn.edu.uit.uitromania;

/**
 * Created by natuan on 16/5/15.
 */


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecognitionAsynTask extends AsyncTask<String,UpdateUIObj,Void> {
    MainActivity activity;
    TextView tvCharacter;
    TextView tvResult;
    ImageView ivCharacter;
    UpdateUIObj obj = new UpdateUIObj();
    Mat rPlate;
    Mat rPlateClone;
    Bitmap rTextImg;
    String rTextPlate="";
    List<Rect> rListPlates;

    public RecognitionAsynTask(MainActivity activity){
        this.activity = activity;
        activity.teststtt = 2;
        rListPlates = new ArrayList<Rect>();
        rListPlates.addAll(activity.l_Plates);
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

        rPlateClone = new Mat();
        activity.teststtt = 5;
        obj = new UpdateUIObj();
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        //number plate
        for(int i=0;i<activity.t_arrPlates.length;i++)
        {
            rPlate = activity.mFrame.submat(rListPlates.get(i));
            double s = rPlate.height()*rPlate.width();
            System.out.println("bitmap hight weidth"+rPlate.height()+" "+rPlate.width());
            Bitmap displayImg = Bitmap.createBitmap(rPlate.width(), rPlate.height(), Config.ARGB_8888);
            Imgproc.blur(rPlate, rPlateClone, new Size(4, 4));
            Imgproc.equalizeHist(rPlateClone,rPlateClone);
            Imgproc.adaptiveThreshold(  rPlateClone, rPlateClone, 255,
                                        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                                        Imgproc.THRESH_BINARY_INV, 85,5);

            Imgproc.morphologyEx(rPlateClone, rPlateClone,
                                Imgproc.MORPH_OPEN,
                                Imgproc.getStructuringElement(Imgproc.MORPH_OPEN, new Size(4,4)));

            Imgproc.findContours(rPlateClone, contours, new Mat(),
                                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            Imgproc.dilate(rPlateClone, rPlateClone,
                                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));

            ArrayList<Rect> sortedContour = new ArrayList<Rect>();
            System.out.println("so ky tu"+contours.size());
            for(int j=0;j<contours.size();j++)
            {
                Rect r = Imgproc.boundingRect(contours.get(j));
                if ((r.area())/s>=0.02 &&
                    (r.area())/s<=0.06 &&
                    (r.width/rPlateClone.width())<0.06 &&
                    (r.height/rPlateClone.height()<0.7))
                {
                    sortedContour.add(r);
                }
            }
            Collections.sort(sortedContour,new MyCompare());

            for(int j=0;j<sortedContour.size();j++)
            {
                Mat textMat  = new Mat();
                int startRow = (int) sortedContour.get(j).tl().y,
                    endRow   = (int) sortedContour.get(j).br().y,
                    startCol = (int) sortedContour.get(j).tl().x,
                    endCol   = (int) sortedContour.get(j).br().x;

                if (sortedContour.get(j).tl().y-5>0)
                    startRow = (int)(sortedContour.get(j).tl().y-5);
                if (sortedContour.get(j).br().y+5<sortedContour.get(j).height)
                    endRow   = (int)(sortedContour.get(j).br().y+5);
                if (sortedContour.get(j).tl().x-5>0)
                    startCol=(int)(sortedContour.get(j).tl().x-5);
                if (sortedContour.get(j).br().x+5<sortedContour.get(j).width)
                    endCol=(int)(sortedContour.get(j).br().x+5);

                textMat = rPlate.submat(startRow, endRow, startCol, endCol).clone();
                rTextImg = Bitmap.createBitmap(endCol - startCol,endRow-startRow,Config.ARGB_8888);

                Utils.matToBitmap(textMat, rTextImg);
                obj.setBm(rTextImg);

                //regconition by MLP
                if(rTextImg!=null)
                {
                    //---textMat
                    String txt =activity.rec.reg(textMat);
                    System.out.println("ndang ky tu--"+txt);
                    if(txt!="") rTextPlate +=txt;

                    obj.setText(txt);
                    publishProgress(obj);
                }
            }
            if(activity.t_arrPlates.length>1)   rTextPlate+=" // ";
        } // End for
        return null;
    }

    @Override
    protected void onProgressUpdate(UpdateUIObj... obj) {
        super.onProgressUpdate(obj);
        activity.teststtt = 3;
        activity.iv.setImageBitmap(obj[0].getBm());
        activity.tv.setText(obj[0].getText());
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        activity.isRunning = false;
        Toast.makeText(activity, activity.textPlate, Toast.LENGTH_LONG).show();
        activity.tvOCR.setText(rTextPlate);
    }

}
