package com.example.bonus.sd;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity {

    Thread currentThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int n = (int)Math.round((100.0*(int)(msg.obj))/msg.arg1);
                ProgressBar pb = findViewById(R.id.progressBar);
                pb.setProgress(n);
            }
        };
    }

    public void onClick_start(View view) {
        currentThread = new ThreadDownloader();
        currentThread.start();
    }

    public void onClick_stop(View view) {
        currentThread.interrupt();
    }

    public class ThreadDownloader extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                URL url = new URL("https://speed.hetzner.de/100MB.bin");
                HttpsURLConnection huc = (HttpsURLConnection) url.openConnection();

                InputStream inputStream = huc.getInputStream();

                File SDCardRoot = Environment.getExternalStorageDirectory();

                File sdPath = new File(SDCardRoot.getAbsolutePath() + "/" + "Downloaded");
                sdPath.mkdirs();
                File file = new File(sdPath, System.currentTimeMillis() + "file");
                Log.d("PATH", file.getAbsolutePath());

                FileOutputStream fileOutput = new FileOutputStream(file);

                huc.setRequestMethod("GET");

                byte[] buffer = new byte[1024];
                int bufferLength;
                int fSize = huc.getContentLength();
                int dSize = 0;

                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer);
                    dSize += bufferLength;

                    Message msg = new Message();
                    msg.obj = dSize;
                    msg.arg1 = fSize;
                 //   handler.sendMessage(msg);

                    if (isInterrupted()) {
                        file.delete();
                        break;
                    }

                }
                fileOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IO", "Exception");
            }
        }
    }

}