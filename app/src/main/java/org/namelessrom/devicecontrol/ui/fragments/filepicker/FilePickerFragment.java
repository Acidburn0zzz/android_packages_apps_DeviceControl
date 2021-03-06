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
package org.namelessrom.devicecontrol.ui.fragments.filepicker;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.activities.FilePickerActivity;
import org.namelessrom.devicecontrol.listeners.OnBackPressedListener;
import org.namelessrom.devicecontrol.objects.FlashItem;
import org.namelessrom.devicecontrol.objects.ShellOutput;
import org.namelessrom.devicecontrol.utils.ContentTypes;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;
import java.util.ArrayList;

/**
 * A class for picking a file
 */
public class FilePickerFragment extends ListFragment implements OnBackPressedListener,
        ShellOutput.OnShellOutputListener, FilePickerListener {

    public static final String ARG_FILE_TYPE = "arg_file_type";

    private static final int ID_GET_FILES = 100;

    private String root        = "/";
    private String currentPath = "/";

    private ArrayList<String> breadcrumbs = new ArrayList<String>();

    private String fileType = "";

    private FileAdapter mFileAdapter;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        if (bundle != null) {
            fileType = bundle.getString(ARG_FILE_TYPE, fileType);
        }
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: restore instance state
        mFileAdapter = new FileAdapter(this);
        mFileAdapter.setFileType(fileType);
        loadFiles(root, true);
    }

    private void loadFiles(final String path, final boolean isBreadcrumb) {
        currentPath = path;
        if (isBreadcrumb) {
            breadcrumbs.add(path);
        }
        Utils.getCommandResult(this, ID_GET_FILES, String.format("ls %s", path), true);
    }

    @Override public void onFilePicked(final File f) {
        currentPath = f.getAbsolutePath() + File.separator;
        if (currentPath.endsWith("../")) {
            onBackPressed();
            return;
        }

        Logger.v(this, String.format("onFile(%s)", currentPath));
        loadFiles(currentPath, true);
    }

    @Override public void onFlashItemPicked(final FlashItem item) {
        if (!ContentTypes.isFiletypeMatching(item.getName(), fileType)) return;

        Logger.v(this, String.format("filePicked(%s)", item.getPath()));
        if (getActivity() instanceof FilePickerActivity) {
            ((FilePickerActivity) getActivity()).onFlashItemPicked(item);
        }
    }

    @Override public void onShellOutput(final ShellOutput shellOutput) {
        if (shellOutput == null) return;
        switch (shellOutput.id) {
            case ID_GET_FILES:
                final String[] output = shellOutput.output.split("\n");
                final ArrayList<File> fileList = new ArrayList<File>(output.length);
                if (!currentPath.equals(root)) {
                    fileList.add(new File(currentPath + File.separator + "../"));
                }
                for (final String s : output) {
                    if (s.isEmpty()) continue;
                    fileList.add(new File(currentPath + File.separator + s));
                }
                mFileAdapter.setFiles(fileList);
                if (getListAdapter() == null) {
                    setListAdapter(mFileAdapter);
                } else {
                    mFileAdapter.notifyDataSetChanged();
                    getListView().setSelectionAfterHeaderView();
                }
                break;
        }
    }

    @Override public boolean onBackPressed() {
        if (!currentPath.equals(root)) {
            if (!breadcrumbs.isEmpty() && breadcrumbs.get(breadcrumbs.size() - 1) != null) {
                breadcrumbs.remove(breadcrumbs.size() - 1);
            }
            loadFiles(breadcrumbs.get(breadcrumbs.size() - 1), false);
            return true;
        }
        return false;
    }

    @Override public boolean showBurger() { return false; }
}
