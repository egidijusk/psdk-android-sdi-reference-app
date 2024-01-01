/*
 * Copyright (c) 2021 by VeriFone, Inc.
 * All Rights Reserved.
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
 * AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
 *
 * Use, disclosure, or reproduction is prohibited
 * without prior written approval from VeriFone, Inc.
 */

package com.verifone.psdk.sdiapplication.sdi.utils;

import android.graphics.Bitmap;

public class JavaUtils {

    public static byte[] convertBitmapTo1bpp(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        byte[] data = new byte[((width + 7) / 8) * height];
        int index = 0;
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int color = bitmap.getPixel(w, h);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                if (r * 0.299 + g * 0.587 + b * 0.114 <= 0.5) {
                    data[index] |= (1 << (7 - w % 8));
                }
                if (w % 8 == 7) {
                    index++;
                }
            }
            if (width % 8 != 0) {
                index++;
            }
        }
        return data;
    }
}
