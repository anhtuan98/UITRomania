package vn.edu.uit.uitromania;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.edu.uit.uitromania.common.FormatPlates;
import vn.edu.uit.uitromania.common.MyCompare;
import vn.edu.uit.uitromania.common.RecognitionAsyncTask;
import vn.edu.uit.uitromania.ocr.RecognitionCharacter;

public class MainActivity extends Activity implements CvCameraViewListener2 {
    public ImageView iv;
    public ImageView ivplate;
    public TextView tv;
    public TextView tvOCR;
    public int teststtt = 0;
    public Rect[] arrPlates;
    public boolean isRunning = false;
    public RecognitionCharacter rec;
    JavaCameraView jvCamera;
    RelativeLayout layoutMain;
    String TAG = "Log";
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    File mCascadeFile;

                    loadCascade(3);
                    //-------------------------load file OCR

                    loadOCR(3);
                }
                break;
                case LoaderCallbackInterface.INIT_FAILED: {

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    String t_character = "";
    Rect[] t_arrPlates;//----reg
    List<Rect> l_Plates = new ArrayList<Rect>();
    List<Rect> pre_l_Plates = new ArrayList<Rect>();
    List<Mat> l_mat_Plates = new ArrayList<Mat>();
    Mat mFrame;
    Mat plate;//----reg
    Mat plateClone;//----reg
    String textPlate;//----reg
    Bitmap textImg;//----reg
    Runnable recognitionText = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            isRunning = true;
            ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();

            //number plate
            for (int i = 0; i < t_arrPlates.length; i++) {

                plate = mFrame.submat(t_arrPlates[i]);
                double s = plate.height() * plate.width();
                System.out.println("bitmap hight weidth" + plate.height() + " " + plate.width());
                Bitmap displayImg = Bitmap.createBitmap(plate.width(), plate.height(), Config.ARGB_8888);

                Imgproc.blur(plate, plateClone, new Size(4, 4));
                Imgproc.equalizeHist(plateClone, plateClone);
                Imgproc.adaptiveThreshold(plateClone, plateClone, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 85, 5);
                Imgproc.morphologyEx(plateClone, plateClone, Imgproc.MORPH_OPEN, Imgproc.getStructuringElement(Imgproc.MORPH_OPEN, new Size(4, 4)));
                Mat hierarchy = new Mat(plateClone.rows(), plateClone.cols(), CvType.CV_8UC1, new Scalar(0));

                Imgproc.findContours(plateClone, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.dilate(plateClone, plateClone, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
                ArrayList<Rect> sortedContour = new ArrayList<Rect>();
                System.out.println("so ky tu" + contours.size());


                for (int j = 0; j < contours.size(); j++) {
                    //character
                    Rect r = Imgproc.boundingRect(contours.get(j));
                    if ((r.area()) / s >= 0.02 && (r.area()) / s <= 0.06 && (r.width / plateClone.width()) < 0.5 && (r.tl().x / plateClone.width()) > 0.05) {
                        //Core.rectangle(mrbga, r.tl(),r.br(), new Scalar(0, 0, 0, 100),2);
                        sortedContour.add(r);
                    }
                }
                Collections.sort(sortedContour, new MyCompare());

                for (int j = 0; j < sortedContour.size(); j++) {
                    Mat textMat = new Mat();
                    int startRow = (int) sortedContour.get(j).tl().y,
                            endRow = (int) sortedContour.get(j).br().y,
                            startCol = (int) sortedContour.get(j).tl().x,
                            endCol = (int) sortedContour.get(j).br().x;
                    if (sortedContour.get(j).tl().y - 5 > 0)
                        startRow = (int) (sortedContour.get(j).tl().y - 5);
                    if (sortedContour.get(j).br().y + 5 < sortedContour.get(j).height)
                        endRow = (int) (sortedContour.get(j).br().y + 5);
                    if (sortedContour.get(j).tl().x - 5 > 0)
                        startCol = (int) (sortedContour.get(j).tl().x - 5);
                    if (sortedContour.get(j).br().x + 5 < sortedContour.get(j).width)
                        endCol = (int) (sortedContour.get(j).br().x + 5);
                    textMat = plate.submat(startRow, endRow, startCol, endCol).clone();
                    textImg = Bitmap.createBitmap(endCol - startCol, endRow - startRow, Config.ARGB_8888);
                    Imgproc.equalizeHist(textMat, textMat);
                    Imgproc.threshold(textMat, textMat, 100, 255, 0);
                    Utils.matToBitmap(textMat, textImg);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            iv.setImageBitmap(textImg);
                        }
                    });

                    //regconition by MLP
                    if (textImg != null) {

                        //---textMat
                        //String txt =regText(textImg);
                        String txt = rec.reg(textMat);
                        System.out.println("ndang ky tu--" + txt);
                        t_character = txt;
                        if (txt != "") {


                            textPlate += txt;
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                tv.setText(t_character);
                            }
                        });
                    }

                    try {
                        t.sleep(200);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }


                if (t_arrPlates.length != 1) textPlate += " // ";
            }
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Toast.makeText(MainActivity.this, textPlate, Toast.LENGTH_LONG).show();
                    tvOCR.setText(textPlate);
                    textPlate = "";
                }
            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            isRunning = false;

        }
    };
    Thread t;
    int testdigit;
    String[] actions = new String[]{
            "Netherlands",
            "Brazil",
            "Vietnam",
            "Romania"
    };
    String[] countries = new String[]{
            "netherlands",
            "brazil",
            "vietnam",
            "romania"
    };
    Mat mrbga;
    Mat mtemp;
    FormatPlates formatPlate = new FormatPlates();
    //private File mCascadeFile;
    private File mMlpOcrFile;
    private CascadeClassifier mJavaDetector;
    private CascadeClassifier mJavaDetectorType[] = new CascadeClassifier[4];
    private File mMlpOcrFileType[] = new File[4];
    // rat quan trong
    private float mRelativePlateSize = 0.05f;
    private int mAbsolutePlateSize = 0;

    private void loadCascade(int cas) {

        try {
            //-------------------------load file cascade

            File mCascadeFile;


            if (mJavaDetectorType[cas] == null) {
                InputStream is;

                switch (cas) {
                    case 0:
                        is = getResources().openRawResource(R.raw.cascade_netherlands);
                        break;
                    case 1:
                        is = getResources().openRawResource(R.raw.cascade_brazil);
                        break;
                    case 2:
                        is = getResources().openRawResource(R.raw.cascade_vietnam);
                        break;
                    case 3:
                        //  is = getResources().openRawResource(R.raw.cascade_romania);break;
                        is = getResources().openRawResource(R.raw.cascade);
                        break;
                    default:

                        is = getResources().openRawResource(R.raw.cascade);
                        break;
                }


                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                mCascadeFile = new File(cascadeDir, "cascade.xml");
                if (!mCascadeFile.exists())
                    mCascadeFile.createNewFile();
                FileOutputStream os = new FileOutputStream(mCascadeFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();

                mJavaDetectorType[cas] = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                Log.i(TAG, "CascadeFile from" + mCascadeFile.getAbsolutePath());
                cascadeDir.delete();
            }
            mJavaDetector = mJavaDetectorType[cas];
            if (mJavaDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mJavaDetector = null;
            } else
                Log.i(TAG, "Loaded cascade classifier ");


            jvCamera.enableView();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }

    private void loadOCR(int ocr) {
        try {

            //InputStream isOcr = getResources().openRawResource(R.raw.mlpocr_netherlands);
            InputStream isOcr;
            if (mMlpOcrFile == null) {
                switch (ocr) {
                    case 0:
                        isOcr = getResources().openRawResource(R.raw.mlpocr_netherlands_99);
                        break;
                    case 1:
                        isOcr = getResources().openRawResource(R.raw.mlpocr_brazil);
                        break;
                    case 2:
                        isOcr = getResources().openRawResource(R.raw.mlpocr_vietnam_96);
                        break;
                    case 3:
                        isOcr = getResources().openRawResource(R.raw.mlpocr_romania_new_2);
                        break;
                    case 4:
                        isOcr = getResources().openRawResource(R.raw.drtuannn5layer256x512x256x64);
                        break;
                    default:
                        isOcr = getResources().openRawResource(R.raw.mlpocr_romania);
                        break;
                }
                File mlpOcrDir = getDir("mlpocr", Context.MODE_PRIVATE);
                mMlpOcrFile = new File(mlpOcrDir, "mlpocr.xml");

                FileOutputStream osOcr = new FileOutputStream(mMlpOcrFile);


                byte[] bufferOcr = new byte[4096];
                int bytesReadOCr;
                while ((bytesReadOCr = isOcr.read(bufferOcr)) != -1) {
                    osOcr.write(bufferOcr, 0, bytesReadOCr);
                }
                isOcr.close();
                osOcr.close();
            }
            rec = new RecognitionCharacter(mMlpOcrFile.getAbsolutePath());
            Mat img;
            try {
                img = Utils.loadResource(getApplicationContext(), R.drawable.test1, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
                // String filename =getResources().getResourceName(R.raw.mlpocr);
                String filename = mMlpOcrFile.getAbsolutePath();

                System.out.println("hinhsrc" + img.total() + "heigh" + img.height() + "width" + img.width() + "-filename-" + filename);

                //testdigit =rec.reg(img,filename);
                System.out.println("kq-" + rec.reg(img) + "--");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //Toast.makeText(getApplicationContext(), testdigit+"---", 1000000).show();
            //-------------------------

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        layoutMain = (RelativeLayout) findViewById(R.id.layoutmain);
        iv = (ImageView) findViewById(R.id.iv);
        ivplate = (ImageView) findViewById(R.id.ivplate);
        tv = (TextView) findViewById(R.id.tv);
        tvOCR = (TextView) findViewById(R.id.tvocr);

        jvCamera = (JavaCameraView) findViewById(R.id.jvcamera);
        jvCamera.setVisibility(SurfaceView.VISIBLE);
        jvCamera.setCvCameraViewListener(this);


        iv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), testdigit + "---", Toast.LENGTH_LONG).show();
            }
        });


        tv.setText("bien testttt" + teststtt);
        Spinner spinner = (Spinner) findViewById(R.id.sp);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, actions);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                       long arg3) {
                if (arg2 == 0) {

                } else if (arg2 == 1) {

                } else if (arg2 == 2) {

                } else {

                }

            }
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    } // End of onCreate()

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_7, this,
                mLoaderCallback);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        // TODO Auto-generated method stub
        plateClone = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        // TODO Auto-generated method stub

    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // TODO Auto-generated method stub
        //Get array plate numbers.
        MatOfRect plates = new MatOfRect();
        Mat mGray = inputFrame.gray();

        //Mat mrbga = inputFrame.rgba();
        mrbga = inputFrame.rgba();
        //
        l_mat_Plates = new ArrayList<Mat>();
        if (mAbsolutePlateSize == 0) {
            int h = mGray.rows();
            if (Math.round(h * mRelativePlateSize) > 0) {
                mAbsolutePlateSize = Math.round(h * mRelativePlateSize);
            }
        }


        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(mGray, plates, 1.1, 2, 2,
                    new org.opencv.core.Size(mAbsolutePlateSize, mAbsolutePlateSize),
                    new org.opencv.core.Size());
        // detect Rec plates
        arrPlates = plates.toArray();

        Boolean isNewPlate = true;
        if (l_Plates != null) {
            l_Plates.clear();
        }

        if (arrPlates.length > 0) {
            for (int j = 0; j < arrPlates.length; j++) {
                pre_l_Plates.add(arrPlates[j]);
                // 	rectangle(Mat img, Point pt1, Point pt2, Scalar color, int thickness)--up-right
                Core.rectangle(mrbga, arrPlates[j].tl(), arrPlates[j].br(), new Scalar(0, 255, 0, 255), 3);
                if (!isRunning) {
                    mFrame = mGray.clone();
                    t_arrPlates = arrPlates.clone();
                    l_mat_Plates.add(mGray.submat(arrPlates[j]));
                    mtemp = mGray.submat(arrPlates[j]);
                    //						t= new Thread(recognitionText);
                    //						t.start();

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Bitmap matTransfom = Bitmap.createBitmap(mtemp.cols(), mtemp.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(mtemp, matTransfom);
                           // ivplate.setImageBitmap(matTransfom); // Tuan comments on 24/5/15
                        }
                    });

                    new RecognitionAsyncTask(this, mtemp).execute();

                }
            }
        }
        return mrbga;
    } // End of onCameraFrame()


}//End activity

