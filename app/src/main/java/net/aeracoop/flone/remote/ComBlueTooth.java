package net.aeracoop.flone.remote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import processing.test.floneremote.R;

public class ComBlueTooth extends Activity {
    static TBlue tBlue;
    EditText blueName;
    TextView textView;
    long lastPress=0;
    static String bluetoothId = "HB02";
    byte[] LED_COLOR = new byte[5];
    int idx = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com_blue_tooth);

        LED_COLOR[0] = 0x01;    // blue
        LED_COLOR[1] = 0x02;    // Green
        LED_COLOR[2] = 0x04;    // RED
        LED_COLOR[3] = 0x08;    // YELLOW
        LED_COLOR[4] = 0x10;    // WHITE

        blueName = (EditText) findViewById(R.id.blue_name);
        textView = (TextView) findViewById(R.id.text);

        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(BTReceiver, filter1);
        this.registerReceiver(BTReceiver, filter2);
        this.registerReceiver(BTReceiver, filter3);
        this.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver BTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Do something if connected
                Toast.makeText(getApplicationContext(), "flone connected", Toast.LENGTH_SHORT).show();
                // floneConnected = true;
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Do something if disconnected
                Toast.makeText(getApplicationContext(), "flone disconnected", Toast.LENGTH_SHORT).show();
                //floneConnected = false;
                try {
                    tBlue.close();

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "flone disconnected error", Toast.LENGTH_SHORT).show();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
                Toast.makeText(getApplicationContext(), "Bluetooth discovery finished.", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
                Toast.makeText(getApplicationContext(), "Found Bluetooth device.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void onDestroy() {
        this.unregisterReceiver(BTReceiver);
        super.onDestroy();
    }

    public void onStart() {
        super.onStart();

        tBlue = new TBlue(this);
    }

    public void onStop() {
        if (tBlue!= null)
            tBlue.close();
        super.onStop();
    }

    public void onConnect(View view) {
        Toast.makeText(this, "onConnect", Toast.LENGTH_SHORT).show();
        if (!blueName.getText().toString().equals(""))
            bluetoothId = blueName.getText().toString();

        try {
            tBlue.connect();
        }
        catch (NullPointerException ex) {
            Toast.makeText(this, "Warning: Flone not connected", Toast.LENGTH_SHORT).show();
        }
    }


    public void onSend(View view) {
        if (btConnected()) {
            Toast.makeText(this, "isConnect == true", Toast.LENGTH_SHORT).show();
            byte[] command = new byte[5];
            command[0] = 0x08;  // LED Color Command
            command[1] = 0x01; //LED_COLOR[idx++];
            command[2] = 0x04; // 0x02 = celcius, 0x04 = feran
            command[3] = 0x01; // second_digit
            command[4] = 0x09; // first_digit

//            if (idx % 5 == 0) {
//                idx = 0;
//            }
            tBlue.write(command);

//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            Log.d("park", "idx: " + idx);
//            command[0] = 0x17; // Temperature Command
//            command[1] = (byte) idx++;
//            tBlue.write(command);
//
//            if (idx > 11) {
//                idx = 0;
//            }
        } else {
            Toast.makeText(this, "isConnect == false", Toast.LENGTH_SHORT).show();
        }
    }

    public void onRead(View view) {
        if (btConnected()) {
            Toast.makeText(this, "isConnect == true", Toast.LENGTH_SHORT).show();
            textView.setText(tBlue.read());
        } else {
            Toast.makeText(this, "isConnect == false", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean btConnected() {
        boolean btConnected = false;
        try {
            btConnected = tBlue.streaming();
        }
        catch(Exception ex) {
            btConnected = false;
        }
        return btConnected;
    }

    public void bluetoothLoop() {
        try {
            if (!tBlue.streaming()) {
                tBlue.connect();
            }
        }
        catch (NullPointerException ex) {
            Toast.makeText(this, "Bluetooth Warning: Can not connect", Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            tBlue.connect();
        }
        else {
            Toast.makeText(this, "No se ha activado el bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        System.out.println("back");
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastPress > 5000){
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_LONG).show();
            lastPress = currentTime;
        }else{
            super.onBackPressed();
        }
    }
}
