package com.meng.mac.opendownloadfile.activity;

import android.app.DownloadManager;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.mbms.DownloadRequest;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meng.mac.opendownloadfile.R;
import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    private Button downloadBtn;
    private Button openBtn;
    private DownloadManager mDownloadManager;
    private long mRequestId;
    private DownloadObserver mDownloadObserver;
    private String mFileUrl = "http://www.hrssgz.gov.cn/bgxz/sydwrybgxz/201101/P020110110748901718161.doc";
    private String mFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadToolBar();
        loadBtn();

        TextView textView = (TextView)findViewById(R.id.officeTV);
        textView.setText("下载页面");
    }

    private void loadToolBar() {

        Toolbar toolbar = (Toolbar)findViewById(R.id.firsttoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void loadBtn() {

        downloadBtn = (Button)findViewById(R.id.downloadBtn);
        downloadBtn.setText("下载");
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFileName = parseName(mFileUrl);
                //开始下载
                startDownload();
            }
        });

        openBtn = (Button)findViewById(R.id.openBtn);
        openBtn.setText("打开");
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //打开文件
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                Log.i("filepath", getLocalFile().getPath());
                intent.putExtra("filePath", getLocalFile().getPath());
                intent.putExtra("fileName", mFileName);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        mDownloadObserver = null;
        mDownloadManager.remove();
        mDownloadManager = null;
    }

    private void startDownload() {

        mDownloadObserver = new DownloadObserver(new Handler());
        getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, mDownloadObserver);

        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mFileUrl));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + "/testdownload/", mFileName);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        mRequestId = mDownloadManager.enqueue(request);
    }

    private class DownloadObserver extends ContentObserver {

        private DownloadObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("downloadUpdate: ", "onChange(boolean selfChange, Uri uri)");
            queryDownloadStatus();
        }
    }

    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(mRequestId);
        Cursor cursor = null;
        try {
            cursor = mDownloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                //已经下载的字节数
                int currentBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                //总需下载的字节数
                int totalBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                //状态所在的列索引
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                Log.i("downloadUpdate: ", currentBytes + " " + totalBytes + " " + status);
                downloadBtn.setText("正在下载：" + currentBytes + "/" + totalBytes);
                if (DownloadManager.STATUS_SUCCESSFUL == status && downloadBtn.getVisibility() == View.VISIBLE) {

                    downloadBtn.setVisibility(View.GONE);
                    openBtn.setVisibility(View.VISIBLE);
                }
            }
        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String parseFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String parseName(String url) {
        String fileName = null;
        try {
            fileName = url.substring(url.lastIndexOf("/") + 1);
        } finally {
            if (TextUtils.isEmpty(fileName)) {
                fileName = String.valueOf(System.currentTimeMillis());
            }
        }
        return fileName;
    }

    private boolean isLocalExist() {
        return getLocalFile().exists();
    }

    private File getLocalFile() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/testdownload/", mFileName);
    }
}
