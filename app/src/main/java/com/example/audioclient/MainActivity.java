package com.example.audioclient;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.clipserver.ClipServerAIDL;

public class MainActivity extends AppCompatActivity {

    private ClipServerAIDL clipServerAIDL;
    RadioGroup radioGroup;
    int selectedTrack;
    int bind = 0;
    ReceiverClient receiver;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            clipServerAIDL = ClipServerAIDL.Stub.asInterface(service);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            clipServerAIDL = null;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receiver = new ReceiverClient();
        registerReceiver(receiver, new IntentFilter("IS_SONG_FINISHED"));


        final ImageView play_pause_button = findViewById(R.id.play_pause);
        final ImageView stop_button = findViewById(R.id.stop);
        final Button service_button = findViewById(R.id.service);

        play_pause_button.setImageResource(R.drawable.play);
        stop_button.setImageResource(R.drawable.stop);

        final String[] play_pause_button_tag = {String.valueOf(play_pause_button.getTag())};


        play_pause_button.setEnabled(false);
        stop_button.setEnabled(false);

        radioGroup = findViewById(R.id.radioGroup);


        //Start/Stop Service Button
        service_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent();
                i.setClassName( "com.example.clipserver", "com.example.clipserver.ClipServerService" );

                if(service_button.getText().equals("Start Service"))
                {
                    startForegroundService(i);
                    bindService(i,serviceConnection,BIND_AUTO_CREATE);
                    bind = 1;
                    service_button.setText("Stop Service");
                    play_pause_button.setEnabled(true);
                }
                else
                {
                    stopService(i);
                    if(bind != 0)
                        unbindService(serviceConnection);
                    bind = 0;

                    play_pause_button.setImageResource(R.drawable.play);
                    play_pause_button.setTag("play");
                    play_pause_button_tag[0] = String.valueOf(play_pause_button.getTag());
                    service_button.setText("Start Service");

                    play_pause_button.setEnabled(false);
                    stop_button.setEnabled(false);

                    Toast.makeText(getApplicationContext(), "Service Stopped", +  Toast.LENGTH_LONG).show();
                }

            }
        });

        //Play/Pause Button
        play_pause_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stop_button.setEnabled(true);

                //Check if button is play
                if(play_pause_button_tag[0].equals("play"))
                {
                    play_pause_button.setImageResource(R.drawable.pause);
                    play_pause_button.setTag("pause");
                    play_pause_button_tag[0] = String.valueOf(play_pause_button.getTag());

                    Intent i = new Intent();
                    i.setClassName( "com.example.clipserver", "com.example.clipserver.ClipServerService" );
                    bindService(i,serviceConnection,BIND_AUTO_CREATE);
                    bind = 1;

                    //Get selected track
                    int radioButtonId = radioGroup.getCheckedRadioButtonId();
                    View selectedRadioButton = radioGroup.findViewById(radioButtonId);
                    selectedTrack = radioGroup.indexOfChild(selectedRadioButton)+1;
                    //Toast.makeText(getApplicationContext(), "TRACK: " + selectedTrack, +  Toast.LENGTH_LONG).show();


                    try {
                        if(clipServerAIDL != null)
                            clipServerAIDL.playClip(selectedTrack);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                //Do pause button
                else
                {
                    play_pause_button.setImageResource(R.drawable.play);
                    play_pause_button.setTag("play");
                    play_pause_button_tag[0] = String.valueOf(play_pause_button.getTag());

                    try {
                        if(clipServerAIDL != null)
                            clipServerAIDL.pauseClip();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }


            }
        });

        //Stop playback button
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if(clipServerAIDL != null)
                        clipServerAIDL.stopClip();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if(bind != 0)
                    unbindService(serviceConnection);
                bind = 0;

                play_pause_button.setImageResource(R.drawable.play);
                play_pause_button.setTag("play");
                play_pause_button_tag[0] = String.valueOf(play_pause_button.getTag());

                Toast.makeText(getApplicationContext(), "Playback Stopped", +  Toast.LENGTH_LONG).show();

            }
        });



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(serviceConnection);
        bind = 0;
    }


    public class ReceiverClient extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("IS_SONG_FINISHED"))
            {
                unbindService(serviceConnection);
                //Toast.makeText(getApplicationContext(), "UNBOUND", +  Toast.LENGTH_LONG).show();

            }

        }
    }
}
