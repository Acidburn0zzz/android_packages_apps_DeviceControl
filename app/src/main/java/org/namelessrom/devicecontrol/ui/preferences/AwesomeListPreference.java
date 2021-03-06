/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.devicecontrol.ui.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;

/**
 * Automatically handles reading to files to automatically set the value,
 * writing to files on preference change, even with multiple files,
 * handling bootup restoration.
 */
public class AwesomeListPreference extends ListPreference {

    private String category;

    private boolean startUp;
    private boolean multiFile;

    private String   mPath;
    private String[] mPaths;

    public AwesomeListPreference(final Context context) {
        this(context, null);
    }

    public AwesomeListPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AwesomePreference);

        int filePath = -1, filePathList = -1;
        try {
            assert (a != null);
            filePath = a.getResourceId(R.styleable.AwesomePreference_filePath, -1);
            filePathList = a.getResourceId(R.styleable.AwesomePreference_filePathList, -1);
            category = a.getString(R.styleable.AwesomePreference_category);
            startUp = a.getBoolean(R.styleable.AwesomePreference_startup, true);
            multiFile = a.getBoolean(R.styleable.AwesomePreference_multifile, false);
        } finally {
            if (a != null) a.recycle();
        }

        final Resources res = context.getResources();
        if (filePath != -1) {
            mPath = Utils.checkPath(res.getString(filePath));
            mPaths = null;
        } else if (filePathList != -1) {
            mPaths = res.getStringArray(filePathList);
            mPath = Utils.checkPaths(mPaths);
            if (mPath.isEmpty() || !multiFile) {
                mPaths = null;
            }
        } else {
            mPath = "";
            mPaths = null;
        }

        if (category == null || category.isEmpty()) {
            Logger.w(this, "Category is not set! Defaulting to \"default\"");
            category = "default";
        }

        setLayoutResource(R.layout.preference);
    }

    public void initValue() {
        if (isSupported()) {
            setValue(Utils.readOneLine(mPath));
        }
    }

    public String getPath() { return mPath; }

    public boolean isSupported() {
        return ((mPath != null && !mPath.isEmpty()) || (mPaths != null && mPaths.length != 0));
    }

    public void writeValue(final String value) {
        if (isSupported()) {
            if (mPaths != null && multiFile) {
                final int length = mPaths.length;
                for (int i = 0; i < length; i++) {
                    Utils.writeValue(mPaths[i], value);
                    if (startUp) {
                        PreferenceHelper.setBootup(new DataItem(
                                category, getKey() + String.valueOf(i), mPaths[i], value));
                    }
                }
            } else {
                Utils.writeValue(mPath, value);
                if (startUp) {
                    PreferenceHelper.setBootup(new DataItem(category, getKey(), mPath, value));
                }
            }
        }
    }

    @Override public boolean isPersistent() { return false; }

    @Override protected boolean shouldPersist() { return false; }
}
