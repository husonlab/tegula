/*
 * ImageEncoding.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
/**
 * string encoding and decoding of images
 * Daniel Huson, 5.2019
 */
public class ImageEncoding {

    public static String encodeImage(Image image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "jpg", out);
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    public static Image decodeImage(String encoding) throws IOException {
        final byte[] byteArray = Base64.getDecoder().decode(encoding);
        try (InputStream in = new ByteArrayInputStream(byteArray)) {
            //return SwingFXUtils.toFXImage(ImageIO.read(in),null);
            return new Image(in);
        }
    }
}
