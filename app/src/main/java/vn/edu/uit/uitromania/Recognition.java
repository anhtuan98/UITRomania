package vn.edu.uit.uitromania;

/**
 * Created by natuan on 16/5/15.
 */

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvANN_MLP;

//import org.opencv.ml.*;

public class Recognition {

    public static final int ATTRIBUTES = 256;
    public static final int CLASSES = 36;

    char digit[] ={ '0','1','2','3','4','5','6','7','8','9','A','B','C','D',
                    'E','F','G','H','I','J','K','L','M','N','O','P','Q','R',
                    'S','T','U','V','W','X','Y','Z'};
    Mat data;
    Mat outputdata= new Mat(1,ATTRIBUTES,CvType.CV_32F);
    Mat datasubmat;
    CvANN_MLP nnetwork;

    Recognition(String filename){
        nnetwork = new CvANN_MLP();
        nnetwork.load(filename); // Load file ket qua cua NN training.

    }
    void scaleDownImage(Mat originalImg,Mat output )
    {
        int i=0;
        for(int x=0;x<16;x++)
        {
            for(int y=0;y<16 ;y++)
            {
                int yd =(int) Math.round((float)(y*originalImg.cols()/16));
                int xd =(int) Math.round((float)(x*originalImg.rows()/16));
                byte [] pixel = new byte[1];

                if(originalImg.get(xd, yd, pixel)==255){
                    output.put(0,yd, 1);
                }else{
                    output.put(0,yd, 0);
                }
                i++;
            }
        }
    }

    void scaleDownImage( )
    {
        int i=0;
        for(int x=0;x<16;x++)
        {
            for(int y=0;y<16 ;y++)
            {
                int yd =(int) Math.round((float)(y*datasubmat.cols()/16));
                int xd =(int) Math.round((float)(x*datasubmat.rows()/16));
                float [] v= new float[1];
                System.out.print (datasubmat.get(xd, yd)[0] + " --");
                if(datasubmat.get(xd, yd)[0]==255){
                    v[0]=1;
                    outputdata.put(0,i, v);

                }else{
                    v[0]=0;
                    outputdata.put(0,i, v);
                }

                i++;

            }
        }
    }

    public void cropImage()
    {
        int row = data.rows();
        int col = data.cols();
        int tlx,tly,bry,brx;//t=top r=right b=bottom l=left
        tlx=tly=bry=brx=0;
        float suml=0;
        float sumr=0;
        int flag=0;

        /**************************top edge***********************/
        for(int x=1;x<row;x++)
        {
            for(int y = 0;y<col;y++)
            {
                if(data.get(x,y)[0]==0)
                {

                    flag=1;
                    tly=x;
                    break;
                }

            }
            if(flag==1)
            {
                flag=0;
                break;
            }

        }
        /*******************bottom edge***********************************/
        for(int x=row-1;x>0;x--)
        {
            for(int y = 0;y<col;y++)
            {
                if(data.get(x,y)[0]==0)
                {

                    flag=1;
                    bry=x;
                    break;
                }

            }
            if(flag==1)
            {
                flag=0;
                break;
            }

        }
        /*************************left edge*******************************/

        for(int y=0;y<col;y++)
        {
            for(int x = 0;x<row;x++)
            {
                if(data.get(x,y)[0]==0)
                {

                    flag=1;
                    tlx=y;
                    break;
                }

            }
            if(flag==1)
            {
                flag=0;
                break;
            }
        }

        /**********************right edge***********************************/

        for(int y=col-1;y>0;y--)
        {
            for(int x = 0;x<row;x++)
            {
                if(data.get(x,y)[0]==0)
                {

                    flag=1;
                    brx= y;
                    break;
                }

            }
            if(flag==1)
            {
                flag=0;
                break;
            }
        }
        int width = brx-tlx;
        int height = bry-tly;
        System.out.println("gtri"+"tlx"+tlx+"tly"+tly+"brx"+brx+"bry"+bry);
        datasubmat = data.submat(tly,bry,tlx,brx);


    }

    public static void printCvMat (Mat mat){

        for (int i = 0; i < mat.rows(); i++){
            for (int j = 0; j < mat.cols(); j++){
                System.out.print (mat.get(i,j)[0] + " --");
            }
            System.out.println("aaa");
        }
    }
    public static void printCvMatD (Mat mat){
        byte [] pixel = new byte[1];
        for (int i = 0; i < mat.rows(); i++){
            for (int j = 0; j < mat.cols(); j++){
                System.out.print (mat.get(i,j)[0] + " --");
            }
            System.out.println();
        }
    }
    public String reg(Mat img){
        int maxIndex = 0;
        try{
//				CvANN_MLP nnetwork = new CvANN_MLP();
//				nnetwork.load(filename);

            //your code here
            //			Mat data= new Mat(1,ATTRIBUTES,CvType.CV_32F);
            //			//CvMat data= CvMat.create(1,ATTRIBUTES,CvType.CV_32F);
            //			Mat output=new Mat();
            data=new Mat();
            Imgproc.GaussianBlur(img, data, new Size(5,5),0);
            //System.out.println("truoc khi chuyen nhi phan");
            //printCvMatD(data);
            Imgproc.equalizeHist(data,data);
            Imgproc.threshold(data, data, 50,255,0);
            Imgproc.dilate(data, data, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
            //printCvMatD(img);
            System.out.println("sau khi chuyen nhi phan");
            //printCvMatD(data);
            cropImage();
            scaleDownImage();
            Mat classOut= new Mat(1,CLASSES,CvType.CV_32F);
            //prediction
            nnetwork.predict(outputdata, classOut);
            double value;
            double maxValue=classOut.get(0, 0)[0];

            for(int index=1;index<CLASSES;index++)
            {
                value = classOut.get(0, index)[0];

                if(value>maxValue)
                {   maxValue = value;
                    maxIndex=index;
                }

            }

            Log.e("giatri",maxIndex+"--");
        }catch(Exception ex){
            maxIndex = 36;
            System.out.println("loi nhan dang"+ex.toString());
        }finally{
            if(maxIndex!=36)
                return digit[maxIndex]+"";
            else
                return "";
        }

    }


}
