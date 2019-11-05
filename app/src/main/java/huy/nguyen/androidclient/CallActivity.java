package huy.nguyen.androidclient;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class CallActivity extends AppCompatActivity {
    ImageView btnEndCall;
    int MY_PORT;
    int FRIEND_PORT;
    String friendIp;
    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 20; // Milliseconds
    private static final int SAMPLE_SIZE = 2; // Bytes
    private static final int BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2; //Bytes
    static String LOG_TAG = "1234";
    boolean call = true;
    boolean speakers = true;
    DatagramSocket callSocket;
    DatagramSocket receiverSocket;
    AudioRecord audioRecorder;
    AudioTrack track;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        addControl();
        addEvent();
    }

    private void addControl() {
        btnEndCall = findViewById(R.id.btnEndCall);
        Intent intent = getIntent();
        MY_PORT = intent.getIntExtra("MyPort",10000);
        FRIEND_PORT = intent.getIntExtra("FriendPort",10001);
        friendIp = intent.getStringExtra("PeerIp");
        initCall();
        initReceiver();
    }


    private void addEvent() {
        btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call = false;
                speakers = false;
                finish();
            }
        });
    }

    private void initCall() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                audioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 10);
                int bytes_read;
                byte[] buf = new byte[BUF_SIZE];
                try {
                    // Create a socket and start recording
                    callSocket = new DatagramSocket();
                    audioRecorder.startRecording();
                    InetAddress address = InetAddress.getByName(friendIp);
                    while (call) {
                        // Capture audio from the mic and transmit it
                        bytes_read = audioRecorder.read(buf, 0, BUF_SIZE);
                        DatagramPacket packet = new DatagramPacket(buf, bytes_read, address, MY_PORT);
                        callSocket.send(packet);
                        Thread.sleep(SAMPLE_INTERVAL, 10);
                    }
                    // Stop recording and release resources
                    audioRecorder.stop();
                    audioRecorder.release();
                    callSocket.disconnect();
                    callSocket.close();
                } catch (InterruptedException e) {
                    call = false;
                    speakers = false;
                    Log.e(LOG_TAG, "InterruptedException: " + e.toString());
                } catch (SocketException e) {
                    call = false;
                    speakers = false;
                    Log.e(LOG_TAG, "SocketException: " + e.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                } catch (UnknownHostException e) {
                    call = false;
                    speakers = false;
                    Log.e(LOG_TAG, "UnknownHostException: " + e.toString());
                } catch (IOException e) {
                    call = false;
                    speakers = false;
                    Log.e(LOG_TAG, "IOException: " + e.toString());
                }
            }
        }).start();
    }


    private void initReceiver() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
                track.play();
                try {
                    // Define a socket to receive the audio
                    receiverSocket = new DatagramSocket(FRIEND_PORT);
                    byte[] buf = new byte[BUF_SIZE];
                    while (speakers) {
                        Log.e("1234", "get there 2");
                        // Play back the audio received from packets
                        DatagramPacket packet = new DatagramPacket(buf, BUF_SIZE);
                        receiverSocket.receive(packet);
                        Log.i(LOG_TAG, "Packet received: " + packet.getLength());
                        track.write(packet.getData(), 0, BUF_SIZE);
                    }
                    // Stop playing back and release resources
                    receiverSocket.disconnect();
                    receiverSocket.close();
                    track.stop();
                    track.flush();
                    track.release();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                    speakers = false;
                } catch (SocketException e) {
                    call = false;
                    speakers = false;
                    Log.e(LOG_TAG, "SocketException: " + e.toString());
                } catch (IOException e) {
                    speakers = false;
                    Log.e(LOG_TAG, "IOException: " + e.toString());
                    speakers = false;
                }
            }
        }).start();
    }


    @Override
    public void onBackPressed() {
        speakers = false;
        call = false;
        super.onBackPressed();
    }
}
