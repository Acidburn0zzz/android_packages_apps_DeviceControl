package org.namelessrom.devicecontrol.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.libcore.RequestHeaders;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.objects.Device;
import org.namelessrom.devicecontrol.services.WebServerService;
import org.namelessrom.devicecontrol.utils.ContentTypes;
import org.namelessrom.devicecontrol.utils.HtmlHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.SortHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A wrapper for the AsyncHttpServer
 */
public class ServerWrapper {
    public static final String ACTION_CONNECTED = "---CONNECTED---";
    public static final String ACTION_TERMINATING = "---TERMINATING---";

    public boolean isStopped = false;

    private static final ArrayList<WebSocket> _sockets = new ArrayList<WebSocket>();

    private AsyncHttpServer mServer;
    private AsyncServerSocket mServerSocket;

    private final WebServerService mService;

    public ServerWrapper(final WebServerService service) {
        mService = service;
    }

    public void stopServer() {
        unregisterReceivers();

        if (mServer != null) {
            mServer.stop();
            mServer = null;
        }
        if (mServerSocket != null) {
            mServerSocket.stop();
            mServerSocket = null;
        }
        for (final WebSocket socket : _sockets) {
            if (socket == null) continue;
            socket.send(ACTION_TERMINATING);
            socket.close();
        }
        _sockets.clear();

        isStopped = true;
    }

    public void createServer() {
        if (mServer != null) return;
        registerReceivers();

        mServer = new AsyncHttpServer();
        Logger.v(this, "[!] Server created");

        setupFonts();
        Logger.v(this, "[!] Setup fonts");

        setupWebSockets();
        Logger.v(this, "[!] Setup websockets");

        setupApi();
        Logger.v(this, "[!] Setup api");

        mServer.directory(Application.get(), "/license", "license.html");
        Logger.v(this, "[!] Setup route: /license");

        mServer.get("/files", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest req,
                    final AsyncHttpServerResponse res) { res.redirect("/files/"); }
        });
        mServer.get("/files/(?s).*", filesCallback);
        Logger.v(this, "[!] Setup route: /files/(?s).*");

        mServer.get("/information", informationCallback);
        Logger.v(this, "[!] Setup route: /information");

        // should be always the last, matches anything that the stuff above did not
        mServer.get("/(?s).*", mainCallback);
        Logger.v(this, "[!] Setup route: /");

        final String portString = PreferenceHelper.getString("wfm_port", "8080");
        int port;
        try {
            port = Utils.parseInt(portString);
        } catch (Exception e) {
            port = 8080;
        }
        mServerSocket = mServer.listen(port);

        mService.setNotification(null);
    }

    private final HttpServerRequestCallback mainCallback = new HttpServerRequestCallback() {
        @Override public void onRequest(final AsyncHttpServerRequest req,
                final AsyncHttpServerResponse res) {
            if (isStopped) {
                res.responseCode(404);
                res.end();
            }
            if (!isAuthenticated(req)) {
                res.getHeaders().getHeaders()
                        .add("WWW-Authenticate", "Basic realm=\"DeviceControl\"");
                res.responseCode(401);
                res.end();
                return;
            }
            Logger.v(this, "[+] Received connection from: %s", req.getHeaders().getUserAgent());
            final String path = remapPath(req.getPath());
            res.getHeaders().getHeaders()
                    .set("Content-Type", ContentTypes.getInstance().getContentType(path));

            res.send(HtmlHelper.loadPath(path));
        }
    };

    private final HttpServerRequestCallback filesCallback = new HttpServerRequestCallback() {
        @Override public void onRequest(final AsyncHttpServerRequest req,
                final AsyncHttpServerResponse res) {
            if (isStopped) {
                res.responseCode(404);
                res.end();
                return;
            }
            if (!isAuthenticated(req)) {
                res.getHeaders().getHeaders().add("WWW-Authenticate",
                        "Basic realm=\"DeviceControl\"");
                res.responseCode(401);
                res.end();
                return;
            }
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                res.send("SDCARD not mounted!");
                return;
            }
            boolean isDirectory = true;
            final String filePath = HtmlHelper.urlDecode(req.getPath()).replace("/files/", "");
            Logger.v(this, "req.getPath(): " + req.getPath());
            Logger.v(this, "filePath: " + filePath);
            File file;
            String sdRoot;
            if (PreferenceHelper.getBoolean("wfm_root", false)) {
                file = new File("/");
                sdRoot = "";
            } else {
                file = Environment.getExternalStorageDirectory();
                sdRoot = file.getAbsolutePath();
            }
            if (filePath != null && !filePath.isEmpty()) {
                file = new File(file, filePath);
                if (file.exists()) {
                    isDirectory = file.isDirectory();
                } else {
                    res.send("File or directory does not exist!");
                    return;
                }
            }
            if (isDirectory) {
                final File[] fs = file.listFiles();
                if (fs == null) {
                    res.send("An error occured!");
                    return;
                }
                final List<File> directories = new ArrayList<File>();
                final List<File> files = new ArrayList<File>();
                for (final File f : fs) {
                    if (f.exists()) {
                        if (f.isDirectory()) {
                            directories.add(f);
                        } else {
                            files.add(f);
                        }
                    }
                }

                final ArrayList<FileEntry> fileEntries = new ArrayList<FileEntry>();
                if (directories.size() > 0) {
                    Collections.sort(directories, SortHelper.sFileComparator);
                    for (final File f : directories) {
                        fileEntries.add(new FileEntry(f.getName(),
                                f.getAbsolutePath().replace(sdRoot, ""), true));
                    }
                }
                if (files.size() > 0) {
                    Collections.sort(files, SortHelper.sFileComparator);
                    for (final File f : files) {
                        fileEntries.add(new FileEntry(f.getName(),
                                f.getAbsolutePath().replace(sdRoot, ""), false));
                    }
                }

                res.send(new Gson().toJson(fileEntries));
            } else {
                final String contentType = ContentTypes.getInstance()
                        .getContentType(file.getAbsolutePath());
                Logger.v(this, "Requested file: %s", file.getName());
                Logger.v(this, "Content-Type: %s", contentType);
                res.setContentType(contentType);
                res.sendFile(file);
            }
        }
    };

    private final HttpServerRequestCallback informationCallback = new HttpServerRequestCallback() {
        @Override public void onRequest(final AsyncHttpServerRequest req,
                final AsyncHttpServerResponse res) {

        }
    };

    private String remapPath(final String path) {
        if (TextUtils.equals("/", path)) {
            return "index.html";
        }
        return path;
    }

    private void setupFonts() {
        // Bootstrap glyphicons
        mServer.directory(Application.get(), "/fonts/glyphicons-halflings-regular.eot",
                "fonts/glyphicons-halflings-regular.eot");
        mServer.directory(Application.get(), "/fonts/glyphicons-halflings-regular.svg",
                "fonts/glyphicons-halflings-regular.svg");
        mServer.directory(Application.get(), "/fonts/glyphicons-halflings-regular.ttf",
                "fonts/glyphicons-halflings-regular.ttf");
        mServer.directory(Application.get(), "/fonts/glyphicons-halflings-regular.woff",
                "fonts/glyphicons-halflings-regular.woff");
        // FontAwesome
        mServer.directory(Application.get(), "/fonts/FontAwesome.otf",
                "fonts/FontAwesome.otf");
        mServer.directory(Application.get(), "/fonts/fontawesome-webfont.eot",
                "fonts/fontawesome-webfont.eot");
        mServer.directory(Application.get(), "/fonts/fontawesome-webfont.svg",
                "fonts/fontawesome-webfont.svg");
        mServer.directory(Application.get(), "/fonts/fontawesome-webfont.ttf",
                "fonts/fontawesome-webfont.ttf");
        mServer.directory(Application.get(), "/fonts/fontawesome-webfont.woff",
                "fonts/fontawesome-webfont.woff");
    }

    private void setupWebSockets() {
        mServer.websocket("/live", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override public void onConnected(final WebSocket webSocket, RequestHeaders headers) {
                _sockets.add(webSocket);
                if (_sockets.size() == 1) {
                    // first client connected, register receivers
                    registerReceivers();
                }

                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override public void onCompleted(final Exception ex) {
                        _sockets.remove(webSocket);
                        if (_sockets.size() == 0) {
                            // No client left, unregister to save battery
                            unregisterReceivers();
                        }
                    }
                });

                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override public void onStringAvailable(final String s) {
                        Logger.v(this, s);
                        //noinspection StatementWithEmptyBody
                        if (ACTION_CONNECTED.equals(s)) {
                            //TODO: initializing
                        }
                    }
                });

                webSocket.send(ACTION_CONNECTED);
            }
        });
    }

    private void setupApi() {
        mServer.get("/api", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest req,
                    final AsyncHttpServerResponse res) {
                res.redirect("/api/device");
            }
        });

        mServer.get("/api/device", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest req,
                    final AsyncHttpServerResponse res) {
                final String result = new Gson().toJson(Device.get());
                res.send(result);
            }
        });
    }

    private boolean isAuthenticated(final AsyncHttpServerRequest req) {
        final boolean isAuth = !PreferenceHelper.getBoolean("wfm_auth", true);
        if (req.getHeaders().hasAuthorization() && !isAuth) {
            final String auth = req.getHeaders().getHeaders().get("Authorization");
            if (auth != null && !auth.isEmpty()) {
                final String[] parts = new String(Base64.decode(auth.replace("Basic", "").trim(),
                        Base64.DEFAULT)).split(":");
                return parts[0] != null
                        && parts[0].equals(PreferenceHelper.getString("wfm_username", "root"))
                        && parts[1] != null
                        && parts[1].equals(PreferenceHelper.getString("wfm_password", "toor"));
            }
        }
        return isAuth;
    }

    private void registerReceivers() {
        if (mService == null) {
            Logger.wtf(this, "mService is null!");
            return;
        }
        final Intent sticky = mService.registerReceiver(mBatteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // try to preload battery level
        if (sticky != null) {
            mBatteryReceiver.onReceive(mService, sticky);
        }
    }

    private void unregisterReceivers() {
        if (mService == null) {
            Logger.wtf(this, "mService is null!");
            return;
        }
        try {
            mService.unregisterReceiver(mBatteryReceiver);
        } catch (Exception ignored) { }
    }

    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            final String batteryLevel = String.format("batteryLevel|%s",
                    intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 1));
            final String batteryCharging = String.format("batteryCharging|%s",
                    intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0 ? "1" : "0");
            for (final WebSocket socket : _sockets) {
                socket.send(batteryLevel);
                socket.send(batteryCharging);
            }
        }
    };

    public AsyncHttpServer getServer() { return mServer; }

    public AsyncServerSocket getServerSocket() { return mServerSocket; }

    private class FileEntry {
        public final String name;
        public final String path;
        public final boolean isDirectory;

        public FileEntry(final String name, final String path, final boolean isDirectory) {
            this.name = name;
            this.path = path;
            this.isDirectory = isDirectory;
        }
    }

}
