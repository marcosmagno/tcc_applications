package com.example.winet;

import android.app.Service;
import android.content.Intent;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReadFile extends Service {

    public static FileObserver observer;
    public List listOfTheFiles = new ArrayList();
    public ReadFile() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void startWatching(String directory) {

        final String pathToWatch = directory;

        Log.d("File", pathToWatch);
        observer = new FileObserver(pathToWatch, FileObserver.ALL_EVENTS) {
            @Override
            public void onEvent(int event, final String file) {
                if (event == FileObserver.CREATE && file != "cached_content_index.exi") {
                    Log.d("MediaListenerService", "File created ["+ file + "]");
                    listOfTheFiles.add(file.toString());

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            }
        };
        observer.startWatching();
        //getLastFile();
    }


    public void getLastFile() {
        Thread d = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("MediaListener", String.valueOf(listOfTheFiles.get(listOfTheFiles.size() - 1)));
            }
        });
        d.start();
    }
}