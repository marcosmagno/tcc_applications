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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

  private static final String TAG = "PlayerActivity";
  private SimpleExoPlayer player;
  private PlayerView playerView;
  private ComponentListener componentListener;
  private long playbackPosition;
  private int currentWindow;
  private boolean playWhenReady = true;
  public Thread threadDownload = null;
  public Thread threadSendIPtoGO = null;

  // Create a default LoadControl
  public LoadControl loadControl;

  // Socket Client
  public ClientSocket clientSocket = new ClientSocket();
  public Thread threadClienteSocket = null;
  public Thread threadWifiDirect = null;
  public Thread threadServerD2D = null;
    public Thread threadClientD2D = null;
  // Wifi
    Button btnOnOff, btnDiscover, btnSend, btnGo;
    ListView listView;
    TextView connectionStatus;
    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;
    public String macGO = null;
    public boolean connectionD2D = false;
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


  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);
    componentListener = new ComponentListener();
    playerView = findViewById(R.id.video_view);

    // Thread to create manager to WIFI and D2D
    threadWifiDirect = new Thread(new Runnable() {

      public void run() {
          initialWork();
          exqListener();

      }

    });
    threadWifiDirect.start();
  }


  public void onStart() {
      super.onStart();
      if (Util.SDK_INT > 23) {
          initializePlayer();
      }

      threadClienteSocket = new Thread(new Runnable() {
          @Override
          public void run() {
              int count = 0;
            clientSocket.startConnection();
            Log.d("clienteSocket","connection");


            clientSocket.sendDataCellPhone("1", getMacAddr());
            //Log.d("Responsiline", clientSocket.getResponseLine());

            macGO = clientSocket.getMacGo();
            Log.d("GetGO", macGO);
            if (getMacAddr().equals(macGO)){
                createGroup();
                // Criar socket Server
              } else {
                discover();
                //Log.d("Peers", String.valueOf(peers.size()));
                //connectD2D();

            }

          }
      });
      Log.d("clienteSocket","start thread");
      threadClienteSocket.start();
      threadClienteSocket.interrupt();


  } // onStart() end

  public void onResume() {
    super.onResume();
    hideSystemUi();
    if ((Util.SDK_INT <= 23 || player == null)) {

      initializePlayer();
    }
    registerReceiver(mReceiver, mIntentFilter);
  } // onResume end


  public void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      releasePlayer();

    }
    unregisterReceiver(mReceiver);
  }


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


      Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
              ReadFile readFile = new ReadFile();
              String directory = getApplicationContext().getExternalCacheDir()+"/" + "downloads";
              Log.d("FileDirectory", directory);
              readFile.startWatching(directory);
          }
      });
      t.start();


  } // initializePlayer() end

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
              null, null);
    } catch (IOException e) {
      Log.e("ReadWriteFile", "Unable to write to the TestFile.txt file.");
    }

  }


  // WIFI
    private void exqListener() {
      /*
      btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    btnOnOff.setText("ON");
                } else {
                    wifiManager.setWifiEnabled(true);
                    btnOnOff.setText("OFF");
                }
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Discovery Started");

                    }

                    @Override
                    public void onFailure(int reason) {
                        connectionStatus.setText("Discovery Starting Failed");
                    }
                });
            }
        });
        */


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];

                final WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                Log.d("D2D", device.deviceAddress);

                // Connect
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("D2D", String.valueOf(config));
                        Toast.makeText(getApplicationContext(),"Connected to" + device.deviceName,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(),"Not Connected",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        /*
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
                Toast.makeText(getApplicationContext(),"GO",Toast.LENGTH_LONG).show();
            }
        });
        */
    }


    public void createGroup() {
        /* Cancela conexões */
        mManager.cancelConnect(mChannel, null);

        /* Remove grupo - desconexão */
        mManager.removeGroup(mChannel, null);

        /* Limpa os serviços locais */
        mManager.clearLocalServices(mChannel, null);

        /* Limpa as requisições de serviços */
        mManager.clearServiceRequests(mChannel, null);

        /* Cria grupo */
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(),"GO",Toast.LENGTH_LONG).show();
                /* D2DReceiver irá tratar */
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(),"Not Group Created ",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initialWork() {
        // Btns
        //btnOnOff = (Button) findViewById(R.id.onOff);
        //btnDiscover = (Button) findViewById(R.id.discover);
        //btnSend = (Button) findViewById(R.id.sendButton);
        listView = (ListView) findViewById(R.id.peerListView);
        //btnGo = (Button) findViewById(R.id.go);
        //read_msg_box = (TextView) findViewById(R.id.readMsg);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        //writeMsg = (EditText) findViewById(R.id.writeMsg);

        // Wifi manager object
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Get IP



        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


    }


    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index = 0;

                for(WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }
                Log.d("Index", String.valueOf(index));
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);


            }

            if(peers.size() == 0) {
                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_LONG).show();
                return;
            }
        }
    };


    WifiP2pManager.ConnectionInfoListener connctionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddres = wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                Log.d("InetAddres", String.valueOf(wifiManager.getDhcpInfo()));
                Log.d("InetAddres", String.valueOf(groupOwnerAddres));
                connectionStatus.setText("Host");
                //TODO Criar Servidor Socket
                threadClienteSocket.interrupt();
                Thread servidor1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ServidorSocketD2D socketServerD2D1 = new ServidorSocketD2D(1403,"192.168.49.1");
                            socketServerD2D1.startSocket();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                servidor1.start();


                Thread servidor2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ServidorSocketD2D socketServerD2D2 = new ServidorSocketD2D(1404,"192.168.49.1");
                            socketServerD2D2.startSocket();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
                servidor2.start();

                Thread servidor3 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ServidorSocketD2D socketServerD2D3 = new ServidorSocketD2D(1405,"192.168.49.1");
                            socketServerD2D3.startSocket();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
                servidor3.start();
                // Class Arquivo




            } else if(wifiP2pInfo.groupFormed){
                //wifiManager.getDhcpInfo();
                connectionStatus.setText("Client");
                String ip = getDottedDecimalIP(getLocalIPAddress());
                Log.d("IPGO", String.valueOf(wifiP2pInfo.groupOwnerAddress));

                Log.d("ClienteGO", clientSocket.getPortConnetionGo());

                Thread client1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ClientSocketD2D clientSocketD2D = new ClientSocketD2D();
                        //Integer.parseInt(clientSocket.getPortConnetionGo()
                        clientSocketD2D.startConnection(String.valueOf(wifiP2pInfo.groupOwnerAddress), Integer.parseInt(clientSocket.getPortConnetionGo()));
                        connectionD2D = true;


                    }

                });

                client1.start();
                onPause();
            }
        }
    };



    public void discover() {

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                connectionStatus.setText("Discovery Started");
            }

            @Override
            public void onFailure(int reason) {
                connectionStatus.setText("Discovery Starting Failed");
            }
        });

    }


    public void connectD2D(String mac) {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

}


    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }


    public void SocketServerD2D(int port) {
        String responseLine = null;
        DataOutputStream os_send = null;
        int sending = 0;
        Log.d("SocketServerD2D", "porta aberta" + port);
        try {
            ServerSocket server = new ServerSocket(port);
            Log.d("SocketServerD2D", "Criando o socket");
            while (true) {
                Socket cliente = server.accept();
                Log.d("SocketServerD2D", "Cliente conectado do IP " + cliente.getInetAddress().getHostAddress());
                os_send = new DataOutputStream(cliente.getOutputStream());

                while (sending <= 10) {
                    Log.d("SocketServerD2D", String.valueOf(sending));
                    os_send.writeBytes("Enviando para o cliente" + port + "\n");
                }
                //server.close();
            }
        } catch (IOException ex) {
            Log.d("SocketServerD2D", "Erro socket" + ex);
        }
    }




    public byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }


    public String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }
}