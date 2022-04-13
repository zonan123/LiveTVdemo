

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for showing open source licenses.
 */
public final class LicenseUtils {
    public final static String LICENSE_FILE = "file:///android_asset/licenses.html";
    public final static String RATING_SOURCE_FILE =
            "file:///android_asset/rating_sources.html";
    //private final static File licenseFile = new File(LICENSE_FILE);

    /**
     * Checks if the license.html asset is include in the apk.
     */
    public static boolean hasLicenses(AssetManager am) {
        try (InputStream is = am.open("licenses.html")) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if the rating_attribution.html asset is include in the apk.
     */
    public static boolean hasRatingAttribution(AssetManager am) {
        try (InputStream is = am.open("rating_sources.html")) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getTextFromResource1(
            Context context, int resourcesIdentifier, long offset, int length) {
        String result = "";
        InputStream stream = null;

        try {
            stream =
                    context.getApplicationContext().getResources().openRawResource(resourcesIdentifier);
            result = getTextFromInputStream(stream, offset, length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static LineNumberReader lreader = null;

    public static void closeSource() {
        try {
            lreader.close();
            lreader = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static LineNumberReader getLreader(Context context, int resourcesIdentifier){
        if(lreader != null){
            return  lreader;
        }
        return new LineNumberReader(new InputStreamReader(context.getApplicationContext().getResources().openRawResource(resourcesIdentifier)));
    }

    public static List<String> getTextFromResource(
            Context context, int resourcesIdentifier, int lineNum, int length) {
        List<String> result = new ArrayList<>();
        String line = null;
        try {

            lreader = getLreader(context,resourcesIdentifier);
            lreader.setLineNumber(lineNum);
            while ((line = lreader.readLine()) != null){
                if(result.size() < length) {
                    result.add(line);
                    lreader.setLineNumber(++lineNum);
                }else {
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String getTextFromInputStream(InputStream stream, long offset, int length) {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream textArray = new ByteArrayOutputStream();

        try {
            long skipResult = stream.skip(offset);
            if(skipResult == -1) {
                throw new RuntimeException("InputStream skip error, return -1.");
            }
            int bytesRemaining = length > 0 ? length : Integer.MAX_VALUE;
            int bytes = 0;

            while (bytesRemaining > 0
                    && (bytes = stream.read(buffer, 0, Math.min(bytesRemaining, buffer.length)))
                    != -1) {
                textArray.write(buffer, 0, bytes);
                bytesRemaining -= bytes;
            }
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read license or metadata text.", e);
        }
        try {
            return textArray.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "Unsupported encoding UTF8. This should always be supported.", e);
        }
    }

    private LicenseUtils() {
    }
}