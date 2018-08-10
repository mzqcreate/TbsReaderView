package com.meng.mac.opendownloadfile.activity;

import android.app.DownloadManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meng.mac.opendownloadfile.R;
import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

public class SecondActivity extends AppCompatActivity implements TbsReaderView.ReaderCallback {

    private TbsReaderView mtbsReaderView;
    private String fileName;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        loadToolBar();

        fileName = getIntent().getStringExtra("fileName");
        filePath = getIntent().getStringExtra("filePath");

        mtbsReaderView = new TbsReaderView(this, this);
        FrameLayout rootLayout = (FrameLayout)findViewById(R.id.rl_layout);
        rootLayout.addView(mtbsReaderView, new LinearLayout.LayoutParams(-1, -1));


        TextView textView = (TextView)findViewById(R.id.officeTV);
        if (fileName.contains(".xlsx")) {

            String name = fileName.substring(0, fileName.indexOf("."));
            textView.setText(name);
        } else {
            textView.setText(fileName);
        }
        displayOfficeFile();
    }

    private void loadToolBar() {

        Toolbar toolbar = (Toolbar)findViewById(R.id.officetoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }

    private void displayOfficeFile() {

        //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
        String bsReaderTemp = "/storage/emulated/0/TbsReaderTemp";
        File bsReaderTempFile = new File(bsReaderTemp);

        if (!bsReaderTempFile.exists()) {
            Log.d("TAG", "准备创建/storage/emulated/0/TbsReaderTemp！！");
            boolean mkdir = bsReaderTempFile.mkdir();
            if(!mkdir){
                Log.e("TAG", "创建/storage/emulated/0/TbsReaderTemp失败！！！！！");
            }
        }

        Bundle bundle = new Bundle();
        String path = filePath + fileName;
        bundle.putString("filePath", new File(filePath).toString());
        bundle.putString("tempPath", Environment.getExternalStorageDirectory() + "/" + "TbsReaderTemp");
        boolean result = mtbsReaderView.preOpen(getFileType(new File(path).toString()),false);
        if (result) {

            mtbsReaderView.openFile(bundle);
        }
    }

    private String parseFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String getFileType(String paramString) {
        String str = "";

        if (TextUtils.isEmpty(paramString)) {
            return str;
        }
        int i = paramString.lastIndexOf('.');
        if (i <= -1) {
            return str;
        }

        str = paramString.substring(i + 1);
        return str;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        mtbsReaderView.onStop();
    }
}
