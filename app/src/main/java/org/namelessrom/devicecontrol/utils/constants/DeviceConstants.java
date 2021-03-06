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
package org.namelessrom.devicecontrol.utils.constants;

import org.namelessrom.devicecontrol.R;

import java.io.File;

public interface DeviceConstants {

    //==============================================================================================
    // Fields
    //==============================================================================================
    public static final String TAG = "DeviceControl";

    //==============================================================================================
    // IDs
    //==============================================================================================
    public static final int ID_PGREP = Integer.MAX_VALUE - 1000;

    //==============================================================================================
    // Fragments
    //==============================================================================================
    public static final int ID_HOME = -100;
    public static final int ID_DEVICE_INFORMATION = R.string.device_information;
    public static final int ID_FEATURES = R.string.features;
    public static final int ID_FAST_CHARGE = ID_FEATURES + 1000;
    public static final int ID_SOUND_CONTROL = ID_FEATURES + 1100;
    public static final int ID_PERFORMANCE_INFO = R.string.information;
    public static final int ID_PERFORMANCE_CPU_SETTINGS = R.string.cpusettings;
    public static final int ID_GOVERNOR_TUNABLE = ID_PERFORMANCE_CPU_SETTINGS + 1000;
    public static final int ID_PERFORMANCE_GPU_SETTINGS = R.string.gpusettings;
    public static final int ID_PERFORMANCE_EXTRA = R.string.extras;
    public static final int ID_HOTPLUGGING = ID_PERFORMANCE_EXTRA + 1000;
    public static final int ID_THERMAL = ID_PERFORMANCE_EXTRA + 1100;
    public static final int ID_KSM = ID_PERFORMANCE_EXTRA + 1200;
    public static final int ID_VOLTAGE = ID_PERFORMANCE_EXTRA + 1300;
    public static final int ID_ENTROPY = ID_PERFORMANCE_EXTRA + 1400;
    public static final int ID_FILESYSTEM = ID_PERFORMANCE_EXTRA + 1500;
    public static final int ID_LOWMEMORYKILLER = ID_PERFORMANCE_EXTRA + 1600;
    public static final int ID_TOOLS_TASKER = R.string.tasker;
    public static final int ID_TOOLS_FLASHER = R.string.flasher;
    public static final int ID_TOOLS_FLASHER_PREFS = ID_TOOLS_FLASHER + 1000;
    public static final int ID_TOOLS_MORE = R.string.more;
    public static final int ID_TOOLS_VM = ID_TOOLS_MORE + 1000;
    public static final int ID_TOOLS_EDITORS_VM = ID_TOOLS_MORE + 1020;
    public static final int ID_TOOLS_BUILD_PROP = ID_TOOLS_MORE + 1100;
    public static final int ID_TOOLS_EDITORS_BUILD_PROP = ID_TOOLS_MORE + 1120;
    public static final int ID_TOOLS_APP_MANAGER = ID_TOOLS_MORE + 1200;
    public static final int ID_TOOLS_WIRELESS_FM = ID_TOOLS_MORE + 1300;
    public static final int ID_PREFERENCES = R.string.preferences;
    public static final int ID_LICENSES = R.string.licenses;
    //----------------------------------------------------------------------------------------------

    //==============================================================================================
    // Actions
    //==============================================================================================
    public static final String ACTION_TASKER_FSTRIM = "action_tasker_fstrim";

    //==============================================================================================
    // Tasker
    //==============================================================================================
    public static final String FSTRIM = "fstrim";
    public static final String FSTRIM_INTERVAL = "fstrim_interval";

    //==============================================================================================
    // Etc
    //==============================================================================================
    public static final String USE_TASKER = "use_tasker";

    //----------------------------------------------------------------------------------------------
    public static final String EXTENSIVE_LOGGING = "extensive_logging";
    public static final String DC_FIRST_START = "dc_first_start";
    public static final String SHOW_LAUNCHER = "show_launcher";
    public static final String SKIP_CHECKS = "skip_checks";

    //----------------------------------------------------------------------------------------------
    public static final String PREF_RECOVERY_TYPE = "pref_recovery_type";
    public static final int RECOVERY_TYPE_BOTH = 0;
    public static final int RECOVERY_TYPE_CWM = 1;
    public static final int RECOVERY_TYPE_OPEN = 2;
    public static final String PREF_FLASHER_MULTI_USER = "flasher_multi_user";

    //==============================================================================================
    // Directories
    //==============================================================================================
    public static final String DC_LOG_DIR = File.separator + "Logs";
    //==============================================================================================
    public static final String DC_LOG_FILE_FSTRIM = DC_LOG_DIR + File.separator + "fstrim.log";
    //==============================================================================================
    public static final String DC_DOWNGRADE = File.separator + ".downgraded";
}
