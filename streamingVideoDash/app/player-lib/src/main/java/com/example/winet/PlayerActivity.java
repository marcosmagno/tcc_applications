/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.winet;

import android.annotation.SuppressLint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.dash.manifest.RepresentationKey;
import com.google.android.exoplayer2.source.dash.offline.DashDownloader;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

/**
 * A fullscreen activity to play audio or video streams.
 */
public class PlayerActivity extends AppCompatActivity {

  private static final String TAG = "PlayerActivity";
  private SimpleExoPlayer player;
  private PlayerView playerView;
  private ComponentListener componentListener;
  private long playbackPosition;
  private int currentWindow;
  private boolean playWhenReady = true;
  public Thread threadDownload = null;

  // Create a default LoadControl
  public LoadControl loadControl;

  // Socket Client
  public ClientSocket clientSocket = new ClientSocket();
  public Thread threadClienteSocket = null;






  // bandwidth meter to measure and estimate bandwidth
  private final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter(new Handler(), new BandwidthMeter.EventListener() {
    @Override
    public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {
      Log.d("Bandwidth - Elapsed", String.valueOf(elapsedMs)+";"+String.valueOf(bytes)+";"+String.valueOf(bitrate)+";");
      Log.d("Buffer - Allocator", String.valueOf(loadControl.getAllocator().getTotalBytesAllocated()));
      Log.d("Buffer - ControlLength", String.valueOf(loadControl.getAllocator().getIndividualAllocationLength()));
      Log.d("Buffer - ControlBuffer", String.valueOf(loadControl.getBackBufferDurationUs()));
      Log.d("Buffer - ControlRetain", String.valueOf(loadControl.retainBackBufferFromKeyframe()));
      String logBandwidth = String.valueOf(elapsedMs+";"+bytes+";"+bitrate+";");
      salvarLog("Bandwith;" , logBandwidth);

    }
  });

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);
    componentListener = new ComponentListener();
    playerView = findViewById(R.id.video_view);
  }

  @Override
  public void onStart() {
      super.onStart();
      if (Util.SDK_INT > 23) {
          initializePlayer();
      }

      threadClienteSocket = new Thread(new Runnable() {
          @Override
          public void run() {
            clientSocket.startConnection();
            Log.d("clienteSocket","connection");
            clientSocket.sendDataCellPhone();
          }
      });
      Log.d("clienteSocket","start thread");
      threadClienteSocket.start();
  }

  @Override
  public void onResume() {
    super.onResume();
    hideSystemUi();
    if ((Util.SDK_INT <= 23 || player == null)) {
      initializePlayer();
    }

  }

  @Override
  public void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      releasePlayer();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (Util.SDK_INT > 23) {
      releasePlayer();
    }
  }

  private void initializePlayer() {
    if (player == null) {
      // a factory to create an AdaptiveVideoTrackSelection
      TrackSelection.Factory adaptiveTrackSelectionFactory =
              new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);

      // a controller
      loadControl = new DefaultLoadControl();


      // using a DefaultTrackSelector with an adaptive video selection factory
      player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this),
              new DefaultTrackSelector(adaptiveTrackSelectionFactory), loadControl);

      player.addListener(componentListener);
      player.addVideoDebugListener(componentListener);
      player.addAudioDebugListener(componentListener);
      playerView.setPlayer(player);
      player.setPlayWhenReady(playWhenReady);

      player.seekTo(currentWindow, playbackPosition);
    }

    //TODO teste: 4G constroi o media source - D2D cria um novo socket (Definir uma variavel de controle)
    MediaSource mediaSource = buildMediaSource(Uri.parse(getString(R.string.media_url_dash)));
    threadDownload.start();

    player.prepare(mediaSource, true, false);
    //player.prepare(concatenatingMediaSource, true, false);



  }

  private void releasePlayer() {
    if (player != null) {

      playbackPosition = player.getCurrentPosition();
      currentWindow = player.getCurrentWindowIndex();
      playWhenReady = player.getPlayWhenReady();
      player.removeListener(componentListener);
      player.removeVideoDebugListener(componentListener);
      player.removeAudioDebugListener(componentListener);
      player.release();
      player = null;
    }

  }

  private MediaSource buildMediaSource(Uri uri) {
      final Uri uriRecv = uri;
      final DataSource.Factory manifestDataSourceFactory = new DefaultHttpDataSourceFactory("ua");

      DashChunkSource.Factory dashChunkSourceFactory = new DefaultDashChunkSource.Factory(
              new DefaultHttpDataSourceFactory("ua", BANDWIDTH_METER));


      threadDownload = new Thread(new Runnable() {
      @Override
      public void run() {
        File cacheDirectory = new File(getApplicationContext().getExternalCacheDir(), "downloads");
        LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024);
        SimpleCache cache = new SimpleCache(cacheDirectory, evictor);
        //DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory("ExoPlayer", null);
        DownloaderConstructorHelper constructorHelper =
                new DownloaderConstructorHelper(cache, manifestDataSourceFactory);
        // Create a downloader for the first representation of the first adaptation set of the first
        // period.
        Log.d("File", String.valueOf(getApplicationContext().getExternalCacheDir()));
        DashDownloader dashDownloader = new DashDownloader(uriRecv, Collections.singletonList(new RepresentationKey(0, 0, 0)), constructorHelper);
        try {
          dashDownloader.download();
          dashDownloader.getDownloadPercentage();
          Log.d("Download", String.valueOf(dashDownloader.getDownloadPercentage()));

          Log.d("Download", "Download");
        }
        catch (IOException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    return new DashMediaSource.Factory(dashChunkSourceFactory,manifestDataSourceFactory).createMediaSource(uri);
  }


  @SuppressLint("InlinedApi")
  private void hideSystemUi() {
    playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
  }

  private class ComponentListener extends Player.DefaultEventListener implements
          VideoRendererEventListener, AudioRendererEventListener {

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

      String stateString;
      switch (playbackState) {
        case Player.STATE_IDLE:
          stateString = "ExoPlayer.STATE_IDLE      -";
          break;
        case Player.STATE_BUFFERING:
          stateString = "ExoPlayer.STATE_BUFFERING -";
          break;
        case Player.STATE_READY:
          stateString = "ExoPlayer.STATE_READY     -";
          break;
        case Player.STATE_ENDED:
          stateString = "ExoPlayer.STATE_ENDED     -";
          break;
        default:
          stateString = "UNKNOWN_STATE             -";
          break;
      }
      Log.d(TAG, "Bandwidth - changed state to " + stateString + " playWhenReady: " + playWhenReady + playbackState);
    }


    // Implementing VideoRendererEventListener.

    @Override
    public void onVideoEnabled(DecoderCounters counters) {

      // Do nothing.
    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
      // Do nothing.
    }

    @Override
    public void onVideoInputFormatChanged(Format format) {
      // Do nothing.
    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {
      // Do nothing.
      Log.d("Qualidade Video;", "Dropped Frames: " + " " + count);
      salvarLog("Dropped Frames;", String.valueOf(count));
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
      // Do nothing.
      Log.d("Video", "Video Size Changed: " + " " + width);
      salvarLog("Qualidade Video;", String.valueOf(width));
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {
      // Do nothing.
    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {
      // Do nothing.
    }

    // Implementing AudioRendererEventListener.

    @Override
    public void onAudioEnabled(DecoderCounters counters) {
      // Do nothing.
    }

    @Override
    public void onAudioSessionId(int audioSessionId) {
      // Do nothing.
    }

    @Override
    public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
      // Do nothing.
    }

    @Override
    public void onAudioInputFormatChanged(Format format) {
      // Do nothing.
    }

    @Override
    public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
      // Do nothing.
    }

    @Override
    public void onAudioDisabled(DecoderCounters counters) {
      // Do nothing.
    }

  }


  public void salvarLog(String type, String data) {
    try {
      // Creates a file in the primary external storage space of the
      // current application.
      // If the file does not exists, it is created.
      File testFile = new File(getApplicationContext().getExternalFilesDir(null), "Logs.txt");
      if (!testFile.exists())
        testFile.createNewFile();

      // Adds a line to the file
      BufferedWriter writer = new BufferedWriter(new FileWriter(testFile, true /*append*/));
      writer.write(type + data + "\n");
      writer.close();
      // Refresh the data so it can seen when the device is plugged in a
      // computer. You may have to unplug and replug the device to see the
      // latest changes. This is not necessary if the user should not modify
      // the files.
      MediaScannerConnection.scanFile(getApplicationContext(),
              new String[]{testFile.toString()},
              null,
              null);
    } catch (IOException e) {
      Log.e("ReadWriteFile", "Unable to write to the TestFile.txt file.");
    }

  }

}