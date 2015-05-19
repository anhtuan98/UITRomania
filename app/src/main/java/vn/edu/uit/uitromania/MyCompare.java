package vn.edu.uit.uitromania;
import org.opencv.core.Rect;
import java.util.Comparator;

/**
 * Created by natuan on 16/5/15.
 */
public class MyCompare implements Comparator<Rect>

{
    @Override
    public int compare(Rect object1, Rect object2) {
        // TODO Auto-generated method stub
        return (int)(object1.tl().x-object2.tl().x);
    }
}