package tiler.tiling;
import javafx.scene.Group;
import javafx.scene.shape.Shape;

import java.util.BitSet;

/**
 * Created by Ruediger on 2017.06.20.
 */
public class Handle extends Group {
    private Shape shape;
    private int type;
    private int flag;

    public void setTransX(double dx){shape.setTranslateX(dx);}
    public void setTransY(double dy){shape.setTranslateY(dy);}
    public double getTransX(){return shape.getTranslateX();}
    public double getTransY(){return shape.getTranslateY();}
    public void setShape(Shape s) {this.shape = s;}
    public Shape getShape(){return shape;}
    public void setType(int t){this.type = t;}
    public int getType(){return this.type;}
    public void setFlag(int a){this.flag = a;}
    public int getFlag(){return this.flag;}
}
