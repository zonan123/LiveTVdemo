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

package com.mediatek.wwtv.tvcenter.tiftimeshift;

import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class that includes convenience methods for accessing TvProvider database.
 */
public final class Utils {
    public static final String EXTRA_KEY_KEYCODE = "keycode";
    public static final String EXTRA_KEY_ACTION = "action";
    public static final String EXTRA_ACTION_SHOW_TV_INPUT ="show_tv_input";
    public static final String EXTRA_KEY_FROM_LAUNCHER = "from_launcher";

    /**
     * Converts time in milliseconds to a String.
     */
    public static String toTimeString(long timeMillis) {
        return new Date(timeMillis).toString();
    }

    /**
     * Converts time in milliseconds to a ISO 8061 string.
     */
    public static String toIsoDateTimeString(long timeMillis) {
        SimpleDateFormat isoStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        return isoStr.format(new Date(timeMillis));
    }

    /**
     * Returns a {@link String} object which contains the layout information of the {@code view}.
     */
    public static String toRectString(View view) {
        return "{"
                + "l=" + view.getLeft()
                + ",r=" + view.getRight()
                + ",t=" + view.getTop()
                + ",b=" + view.getBottom()
                + ",w=" + view.getWidth()
                + ",h=" + view.getHeight() + "}";
    }

    /**
     * Floors time to the given {@code timeUnit}. For example, if time is 5:32:11 and timeUnit is
     * one hour (60 * 60 * 1000), then the output will be 5:00:00.
     */
    public static long floorTime(long timeMs, long timeUnit) {
        return timeMs - (timeMs % timeUnit);
    }

    /**
     * Ceils time to the given {@code timeUnit}. For example, if time is 5:32:11 and timeUnit is
     * one hour (60 * 60 * 1000), then the output will be 6:00:00.
     */
    public static long ceilTime(long timeMs, long timeUnit) {
        return timeMs + timeUnit - (timeMs % timeUnit);
    }

    @Override
    public String toString() {
        return "Utils"; // To fix pmd issue
    }
}
