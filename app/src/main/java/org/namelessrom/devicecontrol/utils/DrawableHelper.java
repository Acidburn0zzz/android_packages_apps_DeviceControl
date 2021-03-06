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
package org.namelessrom.devicecontrol.utils;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;

import org.namelessrom.devicecontrol.Application;

/**
 * Helps with ddddddrawwabllessr5hhwr5hbwb
 */
public class DrawableHelper {

    public static Drawable applyColorFilter(final Drawable d, final int color,
            final boolean isRes) {
        return applyColorFilter(d, isRes ? Application.get().getColor(color) : color);
    }

    public static Drawable applyColorFilter(final Drawable drawable, final int color) {
        final LightingColorFilter lightingColorFilter = new LightingColorFilter(Color.BLACK, color);
        drawable.setColorFilter(lightingColorFilter);
        return drawable;
    }

    public static Drawable applyAccentColorFilter(final int drawableRes) {
        return applyColorFilter(Application.get().getResources()
                .getDrawable(drawableRes), Application.get().getAccentColor());
    }

    public static Drawable applyAccentColorFilter(final Drawable drawable) {
        return applyColorFilter(drawable, Application.get().getAccentColor());
    }

}
