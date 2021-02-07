package ru.miacomsoft.androidsignalclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import ru.miacomsoft.androidsignalclient.services.ServiceExample;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         /*
        TextView text = (TextView) findViewById(R.id.textView);
        // Разблокировать экран
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();

        // ---------------------
        boolean onConnect = false;
        String ipAddress = "";

        // Подключение к WIFI точке деоступа
        WifiConfiguration wifiConfig = new WifiConfiguration();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        // wifiConfig.SSID = String.format("\"%s\"", "a616mm");
        // wifiConfig.SSID = String.format("\"%s\"", "ELTEX-87A2"); // Имя WIFI точки доступа
        // wifiConfig.preSharedKey = String.format("\"%s\"", "XXXXXXX"); // Пароль для полдключения к точки доступа

        wifiConfig.SSID = String.format("\"%s\"", "RT-GPON-3AD2"); // Имя WIFI точки доступа
        wifiConfig.preSharedKey = String.format("\"%s\"", "XXXXXXX"); // Пароль для полдключения к точки доступа

        wifiManager.disconnect();
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        // ------------------------------------------
        while (onConnect == false) {
            WifiInfo info = wifiManager.getConnectionInfo();
            int ip = info.getIpAddress();
            if (ip != 0) {
                ipAddress = Formatter.formatIpAddress(ip);
                text.setText(ipAddress);
                text.append("\nSSID WIFI: ");
                text.append(info.getSSID());
                text.append("\nMAC DEVICE: ");
                text.append(info.getMacAddress());
                onConnect = true;
            } else {
                text.append("\n.");
            }
            pause(3000); // пауза 3 секунды
        }
        */
        startService(new Intent(this, ServiceExample.class));
        pause(10000); // пауза 3 секунды
        this.finishAffinity();
        // getActivity().finish();
        // System.exit(0);
        // moveTaskToBack(true);
        // android.os.Process.killProcess(android.os.Process.myPid());
        // System.exit(1);

    }

    public static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.format("IOException: %s%n", e);
        }
    }
}