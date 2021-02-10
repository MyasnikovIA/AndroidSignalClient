package ru.miacomsoft.androidsignalclient.SocketClient;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
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
import java.util.Locale;

public class ClientSpy  implements TextToSpeech.OnInitListener {
    // --------------------------- генерация голосового сообщения -------------------------
    private TextToSpeech tts = null
            ;
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locale = new Locale("ru");
            int result = tts.setLanguage(locale);
            //int result = mTTS.setLanguage(Locale.getDefault());

            //int result = tts.setLanguage(Locale.US);
                       // tts.setLanguage(Locale.CHINESE);
                       // tts.setPitch(0.6);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                //btnSpeak.setEnabled(true);
                // speakOut();
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
    //----------------------------------------------------------------------------------------




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
                    tts.speak("К устройству подключился клиент "+fromDeviceName, TextToSpeech.QUEUE_FLUSH, null);
                    return;
                }
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
                            tts.speak("Запуск приложения "+packName, TextToSpeech.QUEUE_FLUSH, null);
                            outputStream.write(("{\"run\":\"" + packName + "\"}" + "\r\n\r\n").getBytes());
                            context.startActivity(launchIntent);//null pointer check in case package name was not found
                        } else {
                            tts.speak("Приложение "+packName+" не найдено", TextToSpeech.QUEUE_FLUSH, null);
                            outputStream.write(("{\"runerror\":\"" + packName + " not found\"}" + "\r\n\r\n").getBytes());
                        }

                    } catch (Exception e) {
                    }
                    return;
                }

                if (nam.indexOf("sey") != -1) {
                    tts.speak(val, TextToSpeech.QUEUE_FLUSH, null);
                    Log.e("TTS", "- sey val - "+val);
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
                            tts.speak("Запрос списка установленных приложений для клиента "+fromDeviceName, TextToSpeech.QUEUE_FLUSH, null);
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


            } else {

                if (cmd.indexOf("info") != -1){
                    try {
                        outputStream.write(("connect:" + DeviceName+"\r\n").getBytes());
                        outputStream.write(("===========\r\n").getBytes());
                        outputStream.write(("get:applist\r\n").getBytes());
                        outputStream.write(("run:com.android.chrome\r\n").getBytes());
                        outputStream.write(("run:Chrome\r\n").getBytes());
                        outputStream.write(("sey: сообщение которое будет озвучено уч\r\n").getBytes());
                        outputStream.write(("===========\r\n").getBytes());
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
    private String DeviceName = ""; // имя этого устройства
    private String DevicePass = ""; // пароль этого устройства
    private String RouterPass = ""; // пороль коммутационного сервера
    private PackageManager packageManager = null;

    public ClientSpy(Context context, String host, int port, String DeviceName, String DevicePass,String RouterPass) {
        this.context = context;
        this.SERVER_IP = host;
        this.SERVER_PORT = port;
        this.DeviceName = DeviceName;
        this.DevicePass = DevicePass;
        this.RouterPass = RouterPass;
        if(tts == null) {
            tts = new TextToSpeech(context, this);
        }
    }

    public void start() {
        Toast.makeText(context, "Start Signal Client", Toast.LENGTH_SHORT).show();
        Thread1 = new Thread(new ThreadRun());
        Thread1.start();
    }

    public void stop() {
        process = false;
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    /**
     * отправка сообщения сигнальному серверу
     */
    public void send(String cmd) {
        try {
            outputStream.write((cmd + "\r\n\r\n").getBytes());
        } catch (IOException e) {
        }
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
                tts.speak("Разрав соединения с сигнальным сервером", TextToSpeech.QUEUE_FLUSH, null);
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
                    outputStream.write((DeviceName + "\r\n" + DevicePass + "\r\n"+RouterPass+"\r\n\r\n").getBytes());
                    pause(3000);
                    // String message = input.readLine();
                    new Thread(new ThreadTEstRead()).start();
                    Log.d("LOG_TAG", " connect:" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
                    tts.speak("Установлена связь с сигнальным сервером", TextToSpeech.QUEUE_FLUSH, null);
                } catch (UnknownHostException ex) {
                    process = false;
                    // System.out.println("Server not found: " + ex.getMessage());
                    Log.d("LOG_TAG", "Server not found: " + ex.getMessage());
                } catch (IOException ex) {
                    if (process == true) {
                        tts.speak("нет связи с сигнальным сервером", TextToSpeech.QUEUE_FLUSH, null);
                    }
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
