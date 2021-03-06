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
package org.namelessrom.devicecontrol.actions;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.cpu.CpuFreqMaxAction;
import org.namelessrom.devicecontrol.actions.cpu.CpuFreqMinAction;
import org.namelessrom.devicecontrol.actions.cpu.CpuGovAction;
import org.namelessrom.devicecontrol.actions.extras.MpDecisionAction;
import org.namelessrom.devicecontrol.actions.extras.ksm.KsmDeferredAction;
import org.namelessrom.devicecontrol.actions.extras.ksm.KsmEnableAction;
import org.namelessrom.devicecontrol.actions.extras.ksm.KsmPagesAction;
import org.namelessrom.devicecontrol.actions.extras.ksm.KsmSleepAction;
import org.namelessrom.devicecontrol.actions.fs.IoSchedulerAction;
import org.namelessrom.devicecontrol.actions.fs.ReadAheadAction;
import org.namelessrom.devicecontrol.actions.gpu.Gpu3dScalingAction;
import org.namelessrom.devicecontrol.actions.gpu.GpuFreqMaxAction;
import org.namelessrom.devicecontrol.actions.gpu.GpuFreqMinAction;
import org.namelessrom.devicecontrol.actions.gpu.GpuGovAction;
import org.namelessrom.devicecontrol.hardware.CpuUtils;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.hardware.GpuUtils;
import org.namelessrom.devicecontrol.hardware.IoUtils;
import org.namelessrom.devicecontrol.hardware.KsmUtils;
import org.namelessrom.devicecontrol.utils.DrawableHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;

public class ActionProcessor {

    public static final String CATEGORY_CPU = "cpu";
    public static final String CATEGORY_GPU = "gpu";
    public static final String CATEGORY_EXTRAS = "extras";
    public static final String CATEGORY_FS = "fs";

    public static final String TRIGGER_SCREEN_ON = "screen_on";
    public static final String TRIGGER_SCREEN_OFF = "screen_off";

    //----------------------------------------------------------------------------------------------
    // CPU
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_CPU_FREQUENCY_MAX = CpuFreqMaxAction.NAME;
    public static final String ACTION_CPU_FREQUENCY_MIN = CpuFreqMinAction.NAME;
    public static final String ACTION_CPU_GOVERNOR = CpuGovAction.NAME;

    //----------------------------------------------------------------------------------------------
    // GPU
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_GPU_FREQUENCY_MAX = GpuFreqMaxAction.NAME;
    public static final String ACTION_GPU_FREQUENCY_MIN = GpuFreqMinAction.NAME;
    public static final String ACTION_GPU_GOVERNOR = GpuGovAction.NAME;

    //----------------------------------------------------------------------------------------------
    public static final String ACTION_3D_SCALING = Gpu3dScalingAction.NAME;

    //----------------------------------------------------------------------------------------------
    // Extras
    //----------------------------------------------------------------------------------------------
    // KSM
    public static final String ACTION_KSM_ENABLED = KsmEnableAction.NAME;
    public static final String ACTION_KSM_DEFERRED = KsmDeferredAction.NAME;
    public static final String ACTION_KSM_PAGES = KsmPagesAction.NAME;
    public static final String ACTION_KSM_SLEEP = KsmSleepAction.NAME;
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_MPDECISION = MpDecisionAction.NAME;

    //----------------------------------------------------------------------------------------------
    // Filesystem
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_IO_SCHEDULER = IoSchedulerAction.NAME;
    public static final String ACTION_READ_AHEAD = ReadAheadAction.NAME;

    public static Drawable getImageForCategory(final String category) {
        if (TextUtils.equals(CATEGORY_CPU, category)) {
            return DrawableHelper.applyAccentColorFilter(R.drawable.ic_menu_perf_cpu);
        }
        if (TextUtils.equals(CATEGORY_GPU, category)) {
            return DrawableHelper.applyAccentColorFilter(R.drawable.ic_menu_perf_gpu);
        }
        if (TextUtils.equals(CATEGORY_EXTRAS, category)) {
            return DrawableHelper.applyAccentColorFilter(R.drawable.ic_menu_perf_extras);
        }
        if (TextUtils.equals(CATEGORY_FS, category)) {
            return DrawableHelper.applyAccentColorFilter(R.drawable.ic_general_storage);
        }

        // return the extras one by default
        return DrawableHelper.applyAccentColorFilter(R.drawable.ic_menu_perf_extras);
    }

    public static class Entry {
        public final String name;
        public final String value;

        public Entry(final String name, final String value) {
            this.name = name;
            this.value = value;
        }
    }

    public static ArrayList<Entry> getTriggers() {
        final ArrayList<Entry> triggers = new ArrayList<Entry>();

        Entry e = new Entry(Application.get().getString(R.string.screen_off), TRIGGER_SCREEN_OFF);
        triggers.add(e);
        e = new Entry(Application.get().getString(R.string.screen_on), TRIGGER_SCREEN_ON);
        triggers.add(e);

        return triggers;
    }

    public static ArrayList<Entry> getCategories() {
        final ArrayList<Entry> categories = new ArrayList<Entry>();

        // CPU
        categories.add(new Entry(Application.get().getString(R.string.cpu), CATEGORY_CPU));
        // GPU
        categories.add(new Entry(Application.get().getString(R.string.gpu), CATEGORY_GPU));
        // Extras
        categories.add(new Entry(Application.get().getString(R.string.extras), CATEGORY_EXTRAS));
        // Filesystem
        categories.add(new Entry(Application.get().getString(R.string.filesystem), CATEGORY_FS));

        return categories;
    }

    public static ArrayList<Entry> getActions(final String category) {
        final ArrayList<Entry> actions = new ArrayList<Entry>();
        if (TextUtils.isEmpty(category)) return actions;

        // CPU
        if (TextUtils.equals(CATEGORY_CPU, category)) {
            actions.add(new Entry(Application.get().getString(R.string.cpu_freq_max),
                    ACTION_CPU_FREQUENCY_MAX));
            actions.add(new Entry(Application.get().getString(R.string.cpu_freq_min),
                    ACTION_CPU_FREQUENCY_MIN));
            actions.add(new Entry(Application.get().getString(R.string.cpu_governor),
                    ACTION_CPU_GOVERNOR));
        }
        // GPU
        if (TextUtils.equals(CATEGORY_GPU, category)) {
            if (Utils.fileExists(GpuUtils.get().getGpuFreqMaxPath())) {
                actions.add(new Entry(Application.get().getString(R.string.gpu_freq_max),
                        ACTION_GPU_FREQUENCY_MAX));
            }
            if (Utils.fileExists(GpuUtils.get().getGpuFreqMinPath())) {
                actions.add(new Entry(Application.get().getString(R.string.gpu_freq_min),
                        ACTION_GPU_FREQUENCY_MIN));
            }
            if (Utils.fileExists(GpuUtils.get().getGpuGovPath())) {
                actions.add(new Entry(Application.get().getString(R.string.gpu_governor),
                        ACTION_GPU_GOVERNOR));
            }
            if (Utils.fileExists(GpuUtils.FILE_3D_SCALING)) {
                actions.add(new Entry(Application.get().getString(R.string.gpu_3d_scaling),
                        ACTION_3D_SCALING));
            }
        }
        // Extras
        if (TextUtils.equals(CATEGORY_EXTRAS, category)) {
            if (Utils.fileExists(KsmUtils.KSM_PATH)) {
                if (Utils.fileExists(Application.get().getString(R.string.file_ksm_run))) {
                    actions.add(new Entry(Application.get().getString(R.string.enable_ksm),
                            ACTION_KSM_ENABLED));
                }
                if (Utils.fileExists(Application.get().getString(R.string.file_ksm_deferred))) {
                    actions.add(new Entry(Application.get().getString(R.string.deferred_timer),
                            ACTION_KSM_DEFERRED));
                }
                if (Utils.fileExists(KsmUtils.KSM_PAGES_TO_SCAN)) {
                    actions.add(new Entry(Application.get().getString(R.string.pages_to_scan),
                            ACTION_KSM_PAGES));
                }
                if (Utils.fileExists(KsmUtils.KSM_SLEEP)) {
                    actions.add(new Entry(Application.get().getString(R.string.ksm_sleep),
                            ACTION_KSM_SLEEP));
                }
            }
            if (Utils.fileExists(MpDecisionAction.MPDECISION_PATH)) {
                actions.add(new Entry(Application.get().getString(R.string.mpdecision),
                        ACTION_MPDECISION));
            }
        }
        // Filesystem
        if (TextUtils.equals(CATEGORY_FS, category)) {
            actions.add(new Entry(Application.get().getString(R.string.io), ACTION_IO_SCHEDULER));
            actions.add(new Entry(Application.get().getString(R.string.read_ahead),
                    ACTION_READ_AHEAD));
        }

        return actions;
    }

    public static ArrayList<Entry> getValues(final String action) {
        final ArrayList<Entry> values = new ArrayList<Entry>();
        if (TextUtils.isEmpty(action)) return values;

        // CPU frequencies
        if (TextUtils.equals(ACTION_CPU_FREQUENCY_MAX, action)
                || TextUtils.equals(ACTION_CPU_FREQUENCY_MIN, action)) {
            final String[] freqs = CpuUtils.get().getAvailableFrequencies(true);
            if (freqs == null) return values;

            for (final String s : freqs) {
                values.add(new Entry(CpuUtils.toMhz(s), s));
            }
        }

        // CPU governor
        if (TextUtils.equals(ACTION_CPU_GOVERNOR, action)) {
            final String[] governors = GovernorUtils.get().getAvailableCpuGovernors();
            if (governors == null) return values;

            for (final String s : governors) {
                values.add(new Entry(s, s));
            }
        }

        // GPU frequencies
        if (TextUtils.equals(ACTION_GPU_FREQUENCY_MAX, action)
                || TextUtils.equals(ACTION_GPU_FREQUENCY_MIN, action)) {
            final String[] freqs = GpuUtils.get().getAvailableFrequencies(true);
            if (freqs == null) return values;

            for (final String s : freqs) {
                values.add(new Entry(GpuUtils.toMhz(s), s));
            }
        }

        // GPU governor
        if (TextUtils.equals(ACTION_GPU_GOVERNOR, action)) {
            final String[] governors = GovernorUtils.get().getAvailableGpuGovernors();
            if (governors == null) return values;

            for (final String s : governors) {
                values.add(new Entry(s, s));
            }
        }

        // GPU 3D scaling
        if (TextUtils.equals(ACTION_3D_SCALING, action)) {
            addValuesOnOff(values);
        }

        // Filesystem
        if (TextUtils.equals(ACTION_IO_SCHEDULER, action)) {
            final String[] scheds = IoUtils.get().getAvailableIoSchedulers();
            if (scheds == null) return values;

            for (final String s : scheds) {
                values.add(new Entry(s, s));
            }
        }
        if (TextUtils.equals(ACTION_READ_AHEAD, action)) {
            final String[] entries = Application.get().getStringArray(R.array.read_ahead_entries);
            final String[] vals = Application.get().getStringArray(R.array.read_ahead_values);
            for (int i = 0; i < entries.length; i++) {
                values.add(new Entry(entries[i], vals[i]));
            }
        }
        // Extras
        if (TextUtils.equals(ACTION_KSM_ENABLED, action)
                || TextUtils.equals(ACTION_KSM_DEFERRED, action)) {
            addValuesOnOff(values);
        }
        if (TextUtils.equals(ACTION_KSM_PAGES, action)) {
            final String[] vals = { "32", "64", "128", "256", "512", "1024" };
            for (final String s : vals) {
                values.add(new Entry(s, s));
            }
        }
        if (TextUtils.equals(ACTION_KSM_SLEEP, action)) {
            final String[] vals = { "100", "250", "500", "1000", "2000", "3000", "4000", "5000" };
            for (final String s : vals) {
                values.add(new Entry(s, s));
            }
        }
        if (TextUtils.equals(ACTION_MPDECISION, action)) {
            addValuesOnOff(values);
        }

        return values;
    }

    public static void getProcessAction(final String action, final String value,
            final boolean boot) {
        if (action == null || action.isEmpty() || value == null || value.isEmpty()) {
            return;
        }

        //------------------------------------------------------------------------------------------
        // CPU
        //------------------------------------------------------------------------------------------
        if (ACTION_CPU_FREQUENCY_MAX.equals(action)) {
            new CpuFreqMaxAction(value, boot).triggerAction();
        } else if (ACTION_CPU_FREQUENCY_MIN.equals(action)) {
            new CpuFreqMinAction(value, boot).triggerAction();
        } else if (ACTION_CPU_GOVERNOR.equals(action)) {
            new CpuGovAction(value, boot).triggerAction();
        }
        //------------------------------------------------------------------------------------------
        // GPU
        //------------------------------------------------------------------------------------------
        else if (ACTION_GPU_FREQUENCY_MAX.equals(action)) {
            new GpuFreqMaxAction(value, boot).triggerAction();
        } else if (ACTION_GPU_GOVERNOR.equals(action)) {
            new GpuGovAction(value, boot).triggerAction();
        } else if (ACTION_3D_SCALING.equals(action)) {
            new Gpu3dScalingAction(value, boot).triggerAction();
        }
        //------------------------------------------------------------------------------------------
        // Filesystem
        //------------------------------------------------------------------------------------------
        // IO sched --------------------------------------------------------------------------------
        else if (ACTION_IO_SCHEDULER.equals(action)) {
            new IoSchedulerAction(value, boot).triggerAction();
        }
        // Read Ahead ------------------------------------------------------------------------------
        else if (ACTION_READ_AHEAD.equals(action)) {
            new ReadAheadAction(value, boot).triggerAction();
        }
        //------------------------------------------------------------------------------------------
        // Extras
        //------------------------------------------------------------------------------------------
        // KSM -------------------------------------------------------------------------------------
        else if (ACTION_KSM_ENABLED.equals(action)) {
            new KsmEnableAction(value, boot).triggerAction();
        } else if (ACTION_KSM_DEFERRED.equals(action)) {
            new KsmDeferredAction(value, boot).triggerAction();
        } else if (ACTION_KSM_PAGES.equals(action)) {
            new KsmPagesAction(value, boot).triggerAction();
        } else if (ACTION_KSM_SLEEP.equals(action)) {
            new KsmSleepAction(value, boot).triggerAction();
        }
    }

    private static void addValuesOnOff(final ArrayList<Entry> values) {
        values.add(new Entry(Application.get().getString(R.string.on), "1"));
        values.add(new Entry(Application.get().getString(R.string.off), "0"));
    }

    public static void processAction(final String action, final String value) {
        processAction(action, value, false);
    }

    public static void processAction(final String action, final String value, final boolean boot) {
        getProcessAction(action, value, boot);
    }

}
