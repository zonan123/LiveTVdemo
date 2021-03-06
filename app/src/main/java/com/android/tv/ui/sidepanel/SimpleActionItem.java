/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.tv.ui.sidepanel;
import android.util.Log;

/** A simple item which shows title and description. */
public class SimpleActionItem extends ActionItem {
    private static final boolean DEBUG = false;

    public SimpleActionItem(String title) {
        super(title);
    }

    public SimpleActionItem(String title, String description) {
        super(title, description);
    }

    @Override
    protected void onSelected() {
        if (DEBUG) {
            Log.d("onSelected", "onSelected");
        }
    }
}
