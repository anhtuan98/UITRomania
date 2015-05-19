package vn.edu.uit.uitromania;

/**
 * Created by natuan on 16/5/15.
 */

import java.util.Iterator;
import java.util.List;
import org.opencv.core.Rect;
import android.graphics.Point;

public class FormatPlates {

    public FormatPlates(){

    }
    //type=1: compare with  list plates of previous CameraFrame
    //type=2: compare with  list plates of current CameraFrame
    public boolean isNewPlate(List<Rect> platePointList, Rect platePoint,int type)
    {
        boolean result = true;
        Point p_plate = new Point((platePoint.x + platePoint.width)/2, (platePoint.y + platePoint.height)/2);
        Iterator<Rect> iterator = platePointList.iterator();

        while(iterator.hasNext())
        {
            Rect currentPoint = iterator.next();
            Point point1 = new Point((currentPoint.x + currentPoint.width)/2, (currentPoint.y + currentPoint.height)/2);
            int distance = distanceOfPoint(point1, p_plate);
            if(type == 1){
                if(distance <=10)
                {
                    result = false;
                    break;
                }
            }else if(type==2){
                // because width > height
                if(distance <= platePoint.height/2)
                {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    private int distanceOfPoint(Point point1, Point point2)
    {
        int result = (int) Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
        return result;
    }
}
