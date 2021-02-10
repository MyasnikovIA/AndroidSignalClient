package ru.miacomsoft.androidsignalclient.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import ru.miacomsoft.androidsignalclient.SocketClient.ClientSpy;


public class ServiceExample extends Service {

    ClientSpy sp = null;
    @Override
    public void onCreate() {
        super.onCreate();
        sp = new  ClientSpy(getBaseContext(),"192.168.0.3",8266,"HUAWEI_ATI-l","123","");
        sp.start();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        sp.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
