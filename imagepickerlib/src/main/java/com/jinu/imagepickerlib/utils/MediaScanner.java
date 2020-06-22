package com.jinu.imagepickerlib.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;

public class MediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    private MediaScannerConnection _msc;
    private File _file;

    public MediaScanner(Context context, File f) {
        _file = f;
        _msc = new MediaScannerConnection(context, this);
        _msc.connect();

    }

    @Override
    public void onMediaScannerConnected() {
        _msc.scanFile(_file.getAbsolutePath(), null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        _msc.disconnect();
    }
}