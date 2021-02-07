package ru.miacomsoft.androidsignalclient.SocketClient;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ClientSpy {


    /***
     * Обработка входящих команд от сервера
     */
    class ThreadComandLine implements Runnable {
        private String cmd;

        public ThreadComandLine(String cmd) {
            this.cmd = cmd.replace("\n", "");
        }

        @Override
        public void run() {
            Log.d("LOG_TAG", "cmd: " + cmd);
            if (cmd.indexOf(":") != -1) {
                String nam = cmd.substring(0, cmd.indexOf(":"));
                String val = cmd.substring(cmd.indexOf(":") + 1, cmd.length());
                //----- устанавливаем устройства , на которое бедет прередоватся  сообщение от этого устройства ------------------------------------
                if (nam.indexOf("from") != -1) {
                    if (val.indexOf(":") != -1) {
                        fromDeviceName = val.substring(0, val.indexOf(":"));
                        fromDevicePass = val.substring(val.indexOf(":") + 1, val.length());
                    } else {
                        fromDeviceName = val;
                        fromDevicePass = "";
                    }
                    try {
                        outputStream.write(("device:" + fromDeviceName + "\r\npass:" + fromDevicePass + "\r\n\r\n").getBytes());
                    } catch (IOException e) {
                    }
                    return;
                }
                //------- Запустить приложение  на устройстве -------------------------------------------------------------------------------------
                if (nam.indexOf("run") != -1) {
                    try {
                        outputStream.write((val + "\r\n\r\n").getBytes());
                    } catch (IOException e) {
                    }
                }
                //---------------------------------------------------------------------------------------------------------------------------------

                ///--------------- запуск приложения -----
                if (nam.indexOf("run") != -1) {
                    String packName = val;
                    packageManager = context.getPackageManager();
                    List<ApplicationInfo> listApp = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
                    for (ApplicationInfo appInf : listApp) {
                        String packTxt = appInf.loadLabel(packageManager).toString();
                        if (packTxt.equals(packName)) {
                            packName = appInf.packageName.toString();
                            break;
                        }
                    }
                    try {
                        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packName);
                        if (launchIntent != null) {
                            outputStream.write(("{\"run\":\"" + packName + "\"}" + "\r\n\r\n").getBytes());
                            context.startActivity(launchIntent);//null pointer check in case package name was not found
                        } else {
                            outputStream.write(("{\"runerror\":\"" + packName + " not found\"}" + "\r\n\r\n").getBytes());
                        }
                    } catch (Exception e) {
                    }
                    return;
                }

                if (nam.indexOf("get") != -1) {
                    String cmd = "";
                    String param = "";
                    if (val.indexOf(":") != -1) {
                        cmd = val.substring(0, val.indexOf(":"));
                        param = val.substring(val.indexOf(":") + 1, val.length());
                    } else {
                        cmd = val;
                        param = "";
                    }
                    /// ----------- получить список приложений ------------------------
                    if (cmd.indexOf("applist") != -1) {
                        try {
                            packageManager = context.getPackageManager();
                            packageManager = context.getPackageManager();
                            List<ApplicationInfo> listApp = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
                            for (ApplicationInfo appInf : listApp) {
                                try {
                                    JSONObject jsonApp = new JSONObject();
                                    PackageInfo info = packageManager.getPackageInfo(appInf.packageName, 0);
                                    jsonApp.put("packageName", appInf.packageName.toString());
                                    jsonApp.put("loadLabel", appInf.loadLabel(packageManager));
                                    jsonApp.put("Last_update_time", info.lastUpdateTime);
                                    outputStream.write((jsonApp.toString() + "\r\n").getBytes());
                                } catch (PackageManager.NameNotFoundException | JSONException e) {
                                }
                            }
                            outputStream.write(("\r\n").getBytes());
                        } catch (IOException e) {
                        }
                        return;
                    }
                    ////----------------------------------------------------------------------


                    try {
                        outputStream.write((val + "\r\n\r\n").getBytes());
                    } catch (IOException e) {
                    }
                }


            }
        }
    }


    Thread Thread1 = null;
    Thread ThreadRunClient = null;
    public String SERVER_IP = "192.168.0.3";
    public int SERVER_PORT = 8266;

    private InputStream is;
    private OutputStream os;
    private PrintWriter output;
    private BufferedReader input;
    private Socket socket;
    private OutputStream outputStream;
    private Boolean process = false;
    private String fromDeviceName = "";
    private String fromDevicePass = "";
    private Context context;
    private String DeviceName = "";
    private String DevicePass = "";
    private PackageManager packageManager = null;

    public ClientSpy(Context context, String host, int port, String DeviceName, String DevicePass) {
        this.context = context;
        this.SERVER_IP = host;
        this.SERVER_PORT = port;
        this.DeviceName = DeviceName;
        this.DevicePass = DevicePass;
    }

    public void start() {
        Toast.makeText(context, "Start Client Spy service", Toast.LENGTH_SHORT).show();
        Thread1 = new Thread(new ThreadRun());
        Thread1.start();
    }

    public void stop() {
        process = false;
    }


    class ThreadTEstRead implements Runnable {
        @Override
        public void run() {
            int ind = 0;
            try {
                ind++;
                Log.d("LOG_TAG", socket.isConnected() + " ind = " + ind);
                InputStreamReader isr = new InputStreamReader(is);
                int charInt;
                StringBuffer sbTmp = new StringBuffer();
                ByteArrayOutputStream bufferRaw = new ByteArrayOutputStream();
                while ((charInt = isr.read()) > 0) {
                    if (socket.isConnected() == false) {
                        return;
                    }
                    if (((char) charInt) == '\r') {
                        new Thread(new ThreadComandLine(sbTmp.toString())).start();
                        sbTmp.setLength(0);
                    }
                    // sb.append((char) charInt);
                    sbTmp.append((char) charInt);
                    // bufferRaw.write((char) charInt);
                    // Log.d("LOG_TAG", sb.toString());
                }
                process = false;
                Log.d("LOG_TAG", "!!!!!!!!!!OK!!!!!!!!!!");
            } catch (Exception e) {
                process = false;
                //e.printStackTrace();
            }
        }
    }

    class ThreadExec implements Runnable {
        @Override
        public void run() {
            if (process == false) {
                process = true;
                String hostname = "192.168.0.3";
                int port = 8266;
                try {
                    socket = new Socket(hostname, port);
                    is = socket.getInputStream();
                    os = socket.getOutputStream();
                    outputStream = socket.getOutputStream();
                    outputStream.write((DeviceName + "\r\n" + DevicePass + "\r\n\r\n\r\n").getBytes());
                    pause(3000);
                    // String message = input.readLine();
                    new Thread(new ThreadTEstRead()).start();
                    Log.d("LOG_TAG", " connect:" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
                } catch (UnknownHostException ex) {
                    process = false;
                    // System.out.println("Server not found: " + ex.getMessage());
                    Log.d("LOG_TAG", "Server not found: " + ex.getMessage());
                } catch (IOException ex) {
                    process = false;
                    // System.out.println("I/O error: " + ex.getMessage());
                    Log.d("LOG_TAG", "I/O error: " + ex.getMessage());
                }
            }
        }
    }


    class ThreadRun implements Runnable {
        @Override
        public void run() {
            // ThreadExec client = new ThreadExec();
            // ThreadRunClient = new Thread(client);
            while (true) {
                if (process == false) {
                    new Thread(new ThreadExec()).start();
                }
                pause(10000);
            }
        }
    }


    public static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

}
