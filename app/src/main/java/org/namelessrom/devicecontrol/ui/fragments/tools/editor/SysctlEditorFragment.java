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
package org.namelessrom.devicecontrol.ui.fragments.tools.editor;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.negusoft.holoaccent.dialog.AccentAlertDialog;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.objects.Prop;
import org.namelessrom.devicecontrol.objects.ShellOutput;
import org.namelessrom.devicecontrol.ui.adapters.PropAdapter;
import org.namelessrom.devicecontrol.ui.views.AttachFragment;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SysctlEditorFragment extends AttachFragment implements DeviceConstants,
        AdapterView.OnItemClickListener, ShellOutput.OnShellOutputListener {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private static final int HANDLER_DELAY = 200;

    private static final int APPLY = 200;
    private static final int SAVE  = 201;

    private ListView       mListView;
    private LinearLayout   mLoadingView;
    private LinearLayout   mEmptyView;
    private RelativeLayout mTools;
    private View           mShadowTop, mShadowBottom;

    private       PropAdapter mAdapter = null;
    private       EditText    mFilter  = null;
    private final List<Prop>  mProps   = new ArrayList<Prop>();

    private boolean mLoadFull = false;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override protected int getFragmentId() { return ID_TOOLS_EDITORS_VM; }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new GetPropOperation().execute();
            }
        }, HANDLER_DELAY);
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.tools_prop_list, container, false);

        mListView = (ListView) view.findViewById(R.id.proplist);
        mListView.setOnItemClickListener(this);
        mListView.setFastScrollEnabled(true);
        mListView.setFastScrollAlwaysVisible(true);

        mLoadingView = (LinearLayout) view.findViewById(R.id.loading);
        mEmptyView = (LinearLayout) view.findViewById(R.id.nofiles);
        mTools = (RelativeLayout) view.findViewById(R.id.tools);
        mFilter = (EditText) view.findViewById(R.id.filter);
        mFilter.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(final Editable s) { }

            @Override public void beforeTextChanged(final CharSequence s, final int start,
                    final int count, final int after) { }

            @Override public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) {
                if (mAdapter != null) {
                    final Editable filter = mFilter.getText();
                    mAdapter.getFilter().filter(filter != null ? filter.toString() : "");
                }
            }
        });

        mTools.setVisibility(View.GONE);

        mShadowTop = view.findViewById(R.id.tools_editor_shadow_top);
        mShadowBottom = view.findViewById(R.id.tools_editor_shadow_bottom);

        return view;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_editor, menu);

        menu.removeItem(R.id.menu_action_add);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.menu_action_apply: {
                makeApplyDialog();
                return true;
            }
            case R.id.menu_action_toggle: {
                mLoadFull = !mLoadFull;
                new GetPropOperation().execute();
                return true;
            }
        }

        return false;
    }

    @Override public boolean showBurger() { return false; }

    @Override public void onItemClick(final AdapterView<?> parent, final View view,
            final int position, final long row) {
        final Prop p = mAdapter.getItem(position);
        if (p != null) {
            editPropDialog(p);
        }
    }

    private void makeApplyDialog() {
        final Activity activity = getActivity();
        if (activity == null) return;

        new AccentAlertDialog.Builder(activity)
                .setTitle(getString(R.string.dialog_warning))
                .setMessage(getString(R.string.dialog_warning_apply))
                .setNegativeButton(getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }
                )
                .setPositiveButton(getString(android.R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Utils.remount("/system", "rw");
                                Utils.getCommandResult(SysctlEditorFragment.this, APPLY,
                                        Scripts.copyFile(Application.get().getFilesDirectory() +
                                                "/sysctl.conf", Scripts.SYSCTL)
                                );
                                dialogInterface.dismiss();
                                Toast.makeText(activity,
                                        getString(R.string.toast_settings_applied),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                )
                .show();
    }

    //==============================================================================================
    // Async Tasks
    //==============================================================================================

    // TODO: animation
    private class GetPropOperation extends AsyncTask<String, Void, Void> {
        @Override protected void onPreExecute() {
            mLoadingView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            mTools.setVisibility(View.GONE);
        }

        @Override protected Void doInBackground(final String... params) {
            final StringBuilder sb = new StringBuilder();
            final String dn = Application.get().getFilesDirectory();

            if (new File("/system/etc/sysctl.conf").exists()) {
                sb.append(Scripts.copyFile("/system/etc/sysctl.conf", dn + "/sysctl.conf"));
            } else {
                sb.append("busybox echo \"# created by Device Control\n\" > ")
                        .append(dn).append("/sysctl.conf;\n");
            }

            if (mLoadFull) {
                sb.append("busybox echo `busybox find /proc/sys/* -type f -perm -644 |")
                        .append(" grep -v \"vm.\"`;\n");
            } else {
                sb.append("busybox echo `busybox find /proc/sys/vm/* -type f ")
                        .append("-prune -perm -644`;\n");
            }

            Utils.getCommandResult(SysctlEditorFragment.this, -1, sb.toString());

            return null;
        }
    }

    public void onShellOutput(final ShellOutput shellOutput) {
        switch (shellOutput.id) {
            case SAVE:
                Utils.remount("/system", "ro"); // slip through to APPLY
            case APPLY:
                Utils.runRootCommand("busybox chmod 644 /system/etc/sysctl.conf;"
                        + "busybox sysctl -p /system/etc/sysctl.conf;");
                break;
            default:
                Logger.v(this, "onReadPropsCompleted: " + shellOutput.output);
                if (isAdded()) {
                    loadProp(shellOutput.output);
                } else {
                    Logger.w(this, "Not attached!");
                }
                break;
        }
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    private void loadProp(final String result) {
        final Activity activity = getActivity();
        if ((activity != null) && (result != null) && (!result.isEmpty())) {
            mProps.clear();
            final String[] p = result.split(" ");
            for (String aP : p) {
                if (aP != null && !aP.isEmpty()) {
                    aP = aP.trim();
                    final int length = aP.length();
                    if (length > 0) {
                        String pv = Utils.readOneLine(aP);
                        if (pv != null && !pv.isEmpty()) {
                            pv = pv.trim();
                        }
                        String pn = aP.replace("/", ".");
                        if (pn.length() > 10) {
                            pn = pn.substring(10, length);
                        }
                        mProps.add(new Prop(pn, pv));
                    }
                }
            }
            Collections.sort(mProps);
            mLoadingView.setVisibility(View.GONE);
            if (mProps.isEmpty()) {
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mEmptyView.setVisibility(View.GONE);
                mTools.setVisibility(View.VISIBLE);
                mShadowTop.setVisibility(View.VISIBLE);
                mShadowBottom.setVisibility(View.VISIBLE);
                mAdapter = new PropAdapter(activity, mProps);
                mListView.setAdapter(mAdapter);
            }
        }
    }

    //==============================================================================================
    // Dialogs
    //==============================================================================================

    private void editPropDialog(final Prop p) {
        final Activity activity = getActivity();
        if (activity == null) return;

        final String dn = Application.get().getFilesDirectory();
        String title;

        final View editDialog = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_prop, null);
        final EditText tv = (EditText) editDialog.findViewById(R.id.prop_value);
        final TextView tn = (TextView) editDialog.findViewById(R.id.prop_name_tv);

        if (p != null) {
            tv.setText(p.getVal());
            tn.setText(p.getName());
            title = getString(R.string.edit_property);
        } else {
            title = getString(R.string.add_property);
        }

        new AccentAlertDialog.Builder(activity)
                .setTitle(title)
                .setView(editDialog)
                .setNegativeButton(getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) { }
                        }
                )
                .setPositiveButton(getString(R.string.save)
                        , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (p != null) {
                            if (tv.getText() != null) {
                                final String name = p.getName();
                                final String value = tv.getText().toString().trim();
                                p.setVal(value);
                                Utils.getCommandResult(SysctlEditorFragment.this, SAVE,
                                        Scripts.addOrUpdate(name, value, dn + "/sysctl.conf"));
                            }
                        } else {
                            if (tv.getText() != null && tn.getText() != null) {
                                final String name = tn.getText().toString().trim();
                                final String value = tv.getText().toString().trim();
                                if (name.length() > 0) {
                                    mProps.add(new Prop(name, value));
                                    Utils.getCommandResult(SysctlEditorFragment.this, SAVE,
                                            Scripts.addOrUpdate(name, value, dn + "/sysctl.conf"));
                                }
                            }
                        }
                        Collections.sort(mProps);
                        mAdapter.notifyDataSetChanged();
                    }
                }).show();
    }

}
