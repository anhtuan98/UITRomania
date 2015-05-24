package vn.edu.uit.uitromania.common;

/**
 * Created by natuan on 16/5/15.
 */

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FormatPlates {

    public FormatPlates() {

    }

    public static ArrayList<MatOfPoint> removeContour(double area_contour, ArrayList<MatOfPoint> contours1, Mat res, int step) throws Exception {
        ArrayList<Double> vectorArea = new ArrayList<Double>();
        ArrayList<Integer> vectorHeight = new ArrayList<Integer>();
        ArrayList<Integer> vectorWidth = new ArrayList<Integer>();
        int leng_bounding = contours1.size();
        System.out.println("leng_bounding.size()" + leng_bounding + "max area" + area_contour);
        //remove last
        for (int i = leng_bounding - 1; i > -1; i--) {
            Rect boundRect = Imgproc.boundingRect(contours1.get(i));
            if (boundRect.area() >= area_contour / 3) {
                contours1.remove(i);
            } else {
                vectorArea.add(boundRect.area());
                vectorHeight.add(boundRect.height);
                vectorWidth.add(boundRect.width);
            }
        }

        Collections.sort(vectorArea);
        Collections.sort(vectorHeight);
        Collections.sort(vectorWidth);

        System.out.println("vectorArea.size()" + vectorArea.size());
        float min_area = 0;
        float min_height = 0, max_height = 0;
        float min_width = 0, max_width = 0;
        leng_bounding = contours1.size();
        System.out.println("leng_bounding.size()" + leng_bounding);
        int value_thr_number = 5;
        for (int i = leng_bounding - 1; i >= leng_bounding - value_thr_number; i--) {
            min_area += vectorArea.get(i);
            min_height += vectorHeight.get(i);
            min_width += vectorWidth.get(i);
        }
        max_height = min_height * 3 / value_thr_number;
        max_width = min_width * 3 / value_thr_number;

        min_area = min_area / (value_thr_number * 3);
        //min_height=4*min_height/(5*value_thr_number);
        min_height = min_height / (2 * value_thr_number);
        min_width = min_width / (value_thr_number * 4);
        leng_bounding = contours1.size();

        for (int i = leng_bounding - 1; i > -1; i--) {

            Rect boundRect = Imgproc.boundingRect(contours1.get(i));
            if ((boundRect.area() < min_area) || (boundRect.height < min_height) || (boundRect.width < min_width) ||
                    (boundRect.height > max_height) || (boundRect.width > max_width)) {


                if (step == 2)
                    Imgproc.drawContours(res, contours1, i, new Scalar(255, 255, 255), -1);
                contours1.remove(i);


            }

        }
        return contours1;
    }

    //type=1: compare with  list plates of previous CameraFrame
    //type=2: compare with  list plates of current CameraFrame
    public boolean isNewPlate(List<Rect> platePointList, Rect platePoint, int type) {
        boolean result = true;
        org.opencv.core.Point p_plate = new org.opencv.core.Point((platePoint.x + platePoint.width) / 2, (platePoint.y + platePoint.height) / 2);
        Iterator<Rect> iterator = platePointList.iterator();

        while (iterator.hasNext()) {
            Rect currentPoint = iterator.next();
            org.opencv.core.Point point1 = new org.opencv.core.Point((currentPoint.x + currentPoint.width) / 2, (currentPoint.y + currentPoint.height) / 2);
            int distance = distanceOfPoint(point1, p_plate);
            if (type == 1) {
                if (distance <= 10) {
                    result = false;
                    break;
                }
            } else if (type == 2) {
                // because width > height
                if (distance <= platePoint.height / 2) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    private int distanceOfPoint(org.opencv.core.Point point1, org.opencv.core.Point point2) {
        int result = (int) Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
        return result;
    }

    int[] hw(org.opencv.core.Point p1, org.opencv.core.Point p2, org.opencv.core.Point p3, org.opencv.core.Point p4) {
        int a[] = new int[2];
        if (distanceOfPoint(p1, p2) < distanceOfPoint(p3, p4))
            a[0] = distanceOfPoint(p3, p4);
        else
            a[0] = distanceOfPoint(p1, p2);
        if (distanceOfPoint(p2, p3) < distanceOfPoint(p4, p1))
            a[1] = distanceOfPoint(p4, p1);
        else
            a[1] = distanceOfPoint(p2, p3);

        return a;
    }

    public Mat processingWarpPerspective(org.opencv.core.Point P1, org.opencv.core.Point P2, org.opencv.core.Point P3, org.opencv.core.Point P4, Mat src) {
        int hw_a = hw(P1, P2, P3, P4)[1];
        int hw_b = hw(P1, P2, P3, P4)[0];
        Mat transformed = new Mat();
//        if (hw_b>hw_a){
//            transformed = Mat.zeros(hw_a, hw_b, CvType.CV_8UC3);
//        }
//        else{
//            transformed = Mat.zeros(hw_b, hw_a, CvType.CV_8UC3);
//        }
        hw_a = 110;
        hw_b = 520;
        transformed = Mat.zeros(hw_a, hw_b, CvType.CV_8UC3);
        ArrayList<org.opencv.core.Point> quad_pts = new ArrayList<org.opencv.core.Point>();
        ArrayList<org.opencv.core.Point> squre_pts = new ArrayList<org.opencv.core.Point>();

        quad_pts.add(P1);
        quad_pts.add(P2);
        quad_pts.add(P3);
        quad_pts.add(P4);

        Mat startM = Converters.vector_Point2f_to_Mat(quad_pts);
        squre_pts.add(new org.opencv.core.Point(0, 0));
        squre_pts.add(new org.opencv.core.Point(transformed.cols(), 0));
        squre_pts.add(new org.opencv.core.Point(transformed.cols(), transformed.rows()));
        squre_pts.add(new org.opencv.core.Point(0, transformed.rows()));
        Mat endM = Converters.vector_Point2f_to_Mat(squre_pts);
        Mat transmtx = Imgproc.getPerspectiveTransform(startM, endM);
        Imgproc.warpPerspective(src, transformed, transmtx, transformed.size());
        //contourCharacter(transformed);
        return transformed;
    }

    public ArrayList<Mat> contourCharacter(Mat res) throws Exception {
        //show( convert(res),"after transform");
        Mat src = res.clone();
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        ArrayList<MatOfPoint> contours1 = new ArrayList<MatOfPoint>();
        ArrayList<Mat> character = new ArrayList<Mat>();
        ArrayList<Rect> rectCharacter = new ArrayList<Rect>();
        Mat hierarchy = new Mat(res.rows(), res.cols(), CvType.CV_8UC1, new Scalar(0));
        Imgproc.threshold(src.clone(), src, 70, 255, Imgproc.THRESH_BINARY_INV);
        Imgproc.findContours(src.clone(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.findContours(src.clone(), contours1, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double area_max = src.height() * src.width();
        contours = removeContour(area_max, contours1, res, 2);
        for (int j = 0; j < contours.size(); j++) {
            //Imgproc.drawContours(src, contours, j, new Scalar(255,255,255),1);
            Rect boundRect = Imgproc.boundingRect(contours.get(j));
            rectCharacter.add(boundRect);

        }
        Collections.sort(rectCharacter, new MyCompare());
        for (int k = 0; k < rectCharacter.size(); k++) {
            character.add(res.submat(rectCharacter.get(k)));
        }

        return character;
    }

    public Mat newTransform(Mat src) throws Exception {

        org.opencv.core.Point P1 = new org.opencv.core.Point();
        org.opencv.core.Point P2 = new org.opencv.core.Point();
        org.opencv.core.Point P3 = new org.opencv.core.Point();
        org.opencv.core.Point P4 = new org.opencv.core.Point();
        Mat thr = new Mat();//src.width(),src.height(),CvType.CV_8UC1);
        Imgproc.medianBlur(src.clone(), src, 1);//tothon
        Imgproc.threshold(src, thr, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        //Imgproc.adaptiveThreshold(src, thr, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 75,10);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        int largest_contour_index = 0;
        double largest_area = 0;

        Mat dst = new Mat(src.rows(), src.cols(), CvType.CV_8UC1, new Scalar(0)); //create destination image
        Imgproc.findContours(thr.clone(), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE); // Find the contours in the image
        System.out.print("contours size" + contours.size());
        for (int i = 0; i < contours.size(); i++) {

            double a = Imgproc.contourArea(contours.get(i), false);  //  Find the area of contour
            if (a > largest_area) {
                largest_area = a;
                largest_contour_index = i;                //Store the index of largest contour
            }

        }
        Imgproc.drawContours(dst, contours, largest_contour_index, new Scalar(255, 255, 255), -1);
        Mat res = new Mat();
        Core.bitwise_and(thr, dst, res);
        ArrayList<MatOfPoint> contours1 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(res.clone(), contours1, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        //double area_max=Imgproc.boundingRect(contours.get(largest_contour_index)).area();
        double area_max = Imgproc.contourArea(contours.get(largest_contour_index));
        contours1 = removeContour(area_max, contours1, res, 1);

        int min_left = 0;
        int max_right = 0;
        Rect boundRectLeft = new Rect();
        Rect boundRectRight = new Rect();
        for (int i = contours1.size() - 1; i > -1; i--) {
            Rect boundRect = Imgproc.boundingRect(contours1.get(i));
            if (min_left == 0 || min_left > boundRect.tl().x) {
                min_left = (int) boundRect.tl().x;
                boundRectLeft = boundRect;
            }
            if (max_right == 0 || max_right < boundRect.br().x) {
                max_right = (int) boundRect.br().x;
                boundRectRight = boundRect;
            }
        }

        int err_transform = boundRectLeft.height / 15;
        err_transform = 0;
        P1.x = boundRectLeft.tl().x - err_transform;
        P1.y = boundRectLeft.tl().y - err_transform;

        P2.x = boundRectRight.br().x + err_transform;
        P2.y = boundRectRight.br().y - boundRectRight.height - err_transform;

        P3.x = boundRectRight.br().x + err_transform;
        P3.y = boundRectRight.br().y + err_transform;

        P4.x = boundRectLeft.tl().x - err_transform;
        P4.y = boundRectLeft.tl().y + boundRectLeft.height + err_transform;

        return processingWarpPerspective(P1, P2, P3, P4, res);

    }


}
