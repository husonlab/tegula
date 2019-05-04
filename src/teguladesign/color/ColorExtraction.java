/*
 * ColorExtraction.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package teguladesign.color;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * extracts colors from the viewport of an image view
 * Daniel Huson, 5.2019
 */
public class ColorExtraction {
    public static int apply (ImageView imageView, double percentSeparation, ObservableList<Color> colors,int maxNumberOfColors) {
        colors.clear();
        if(imageView.getImage()!=null) {
            final PixelReader pixelReader = imageView.getImage().getPixelReader();
            final Map<Color, Integer> color2count = new HashMap<>();
            final Rectangle2D rect = imageView.getViewport();
            for (int x = (int) Math.max(0,rect.getMinX()); x < Math.min(rect.getMaxX(),imageView.getImage().getWidth()); x++) {
                for (int y = (int) Math.max(0,rect.getMinY()); y < Math.min(rect.getMaxY(),imageView.getImage().getHeight()); y++) {
                    final Color color = pixelReader.getColor(x, y);
                    color2count.merge(color, 1, Integer::sum);
                }
            }
            final ArrayList<Map.Entry<Color,Integer>> entries=new ArrayList<>(color2count.entrySet());
            if(entries.size()>0) {
                entries.sort((a, b) -> -Integer.compare(a.getValue(), b.getValue()));

                final double separation=percentSeparation/100.0;
                for (Map.Entry<Color, Integer> entry : entries) {
                    final Color color = entry.getKey();

                    boolean ok=true;
                    for(Color prev:colors) {
                        if(distance(prev,color)<separation) {
                            ok=false;
                            break;
                        }
                    }
                    if (ok) {
                        colors.add(color);
                        if(colors.size()==maxNumberOfColors)
                            break;
                    }
                }
                // move black to end:
                boolean hasBlack=false;
                for(Color color:colors) {
                    if(color.equals(Color.BLACK)) {
                        colors.remove(color);
                        hasBlack=true;
                        break;
                    }
                }
                boolean hasWhite=false;
                for(Color color:colors) {
                    if(color.equals(Color.WHITE)) {
                        colors.remove(color);
                        hasWhite=true;
                        break;
                    }
                }
                if(hasBlack)
                colors.add(Color.BLACK);
                if(hasWhite)
                    colors.add(Color.WHITE);
            }
        }
        return colors.size();
    }

    private static double distance(Color a, Color b) {
        return (Math.abs(a.getRed()-b.getRed())+Math.abs(a.getGreen()-b.getGreen())+Math.abs(a.getBlue()-b.getBlue()))/3.0;
    }

}
