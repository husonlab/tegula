/*
 * Utilities.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.core.fundamental.utils;

import tegula.core.fundamental.data.*;

/**
 * Ultilities to help port C code to Java
 * Created by huson on 3/28/16.
 */
public class Utilities {
    /**
     * compute the length of a null-termininated string
     *
     * @param str
     * @return length
     */
    public static int length(char[] str) {
        int i = 0;
        while (i < str.length) {
            if (str[i] == 0)
                return i;
            else
                i++;
        }
        return i;
    }

    /**
     * makes room for a new item in an array
     *
     * @param unusedMessage
     * @param old
     * @param length
     * @param newLength
     * @param unusedObjectSize
     * @return new array
     */
    public static EDG[] alloc(String unusedMessage, EDG[] old, Wrap<Integer> length, int newLength, Object unusedObjectSize) {
        final EDG[] array = new EDG[newLength];

        if (length != null) {
            length.set(newLength);
        }
        int top = 0;
        if (old != null) {
            top = Math.min(old.length, newLength);
            System.arraycopy(old, 0, array, 0, top);
        }
        for (int i = top; i < array.length; i++)
            array[i] = new EDG();
        return array;
    }

    /**
     * makes room for a new item in an array
     *
     * @param unusedMessage
     * @param old
     * @param length
     * @param newLength
     * @param unusedObjectSize
     * @return new array
     */
    public static NOD[] alloc(String unusedMessage, NOD[] old, Wrap<Integer> length, int newLength, Object unusedObjectSize) {
        final NOD[] array = new NOD[newLength];

        if (length != null) {
            length.set(newLength);
        }
        int top = 0;
        if (old != null) {
            top = Math.min(old.length, newLength);
            System.arraycopy(old, 0, array, 0, top);
        }
        for (int i = top; i < array.length; i++)
            array[i] = new NOD();
        return array;
    }

    /**
     * makes room for a new item in an array
     *
     * @param unusedMessage
     * @param old
     * @param length
     * @param newLength
     * @param unusedObjectSize
     * @return new array
     */
    public static ORB[] alloc(String unusedMessage, ORB[] old, Wrap<Integer> length, int newLength, Object unusedObjectSize) {
        final ORB[] array = new ORB[newLength];

        if (length != null) {
            length.set(newLength);
        }
        int top = 0;
        if (old != null) {
            top = Math.min(old.length, newLength);
            System.arraycopy(old, 0, array, 0, top);
        }
        for (int i = top; i < array.length; i++)
            array[i] = new ORB();
        return array;
    }

    /**
     * makes room for a new item in an array
     *
     * @param unusedMessage
     * @param old
     * @param length
     * @param newLength
     * @param unusedObjectSize
     * @return new array
     */
    public static NCR[] alloc(String unusedMessage, NCR[] old, Wrap<Integer> length, int newLength, Object unusedObjectSize) {
        final NCR[] array = new NCR[newLength];

        if (length != null) {
            length.set(newLength);
        }
        int top = 0;
        if (old != null) {
            top = Math.min(old.length, newLength);
            System.arraycopy(old, 0, array, 0, top);
        }
        for (int i = top; i < array.length; i++)
            array[i] = new NCR();
        return array;
    }

    /**
     * makes room for a new item in an array
     *
     * @param unusedMessage
     * @param old
     * @param length
     * @param newLength
     * @param unusedObjectSize
     * @return new array
     */
    public static OCR[] alloc(String unusedMessage, OCR[] old, Wrap<Integer> length, int newLength, Object unusedObjectSize) {
        final OCR[] array = new OCR[newLength];

        if (length != null) {
            length.set(newLength);
        }
        int top = 0;
        if (old != null) {
            top = Math.min(old.length, newLength);
            System.arraycopy(old, 0, array, 0, top);
        }
        for (int i = top; i < array.length; i++)
            array[i] = new OCR();
        return array;
    }

    /**
     * makes room for a new item in an array
     *
     * @param unusedMessage
     * @param old
     * @param length
     * @param newLength
     * @param unusedObjectSize
     * @return new array
     */
    public static ECR[] alloc(String unusedMessage, ECR[] old, Wrap<Integer> length, int newLength, Object unusedObjectSize) {
        final ECR[] array = new ECR[newLength];

        if (length != null) {
            length.set(newLength);
        }
        int top = 0;
        if (old != null) {
            top = Math.min(old.length, newLength);
            System.arraycopy(old, 0, array, 0, top);
        }
        for (int i = top; i < array.length; i++)
            array[i] = new ECR();
        return array;
    }

    /**
     * makes room for a new item in an array
     *
     * @param unusedMessage
     * @param old
     * @param length
     * @param newLength
     * @param unusedObjectSize
     * @return new array
     */
    public static int[] alloc(String unusedMessage, int[] old, Wrap<Integer> length, int newLength, Object unusedObjectSize) {
        final int[] array = new int[newLength];

        if (length != null) {
            length.set(newLength);
        }
        if (old != null) {
            int top = Math.min(old.length, newLength);
            System.arraycopy(old, 0, array, 0, top);
        }
        return array;
    }
}
