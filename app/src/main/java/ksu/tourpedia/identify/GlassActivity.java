package ksu.tourpedia.identify;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class GlassActivity extends AppCompatActivity {


    TextView message;
    Context context =this;
    BluetoothAdapter mBluetoothAdapter;
    Handler mHandler;
    File pictureFile;
    FileOutputStream fos;

    //========Constants=========
    final int REQUEST_ENABLE_BLUETOOTH = 1;
    final int MESSAGE_READ = 5;
    final int MEDIA_TYPE_IMAGE=1;//repeated constant
    final String NAME= "TOURPEDIA";
    final int DONE_READ =2;
    final UUID MY_UUID = UUID.fromString("36d36d58-63e5-4be8-840b-bcac5022149a");//Generated by a UUID generator

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass);


        message = (TextView)findViewById(R.id.connctionMessage);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if(mBluetoothAdapter!=null)
        requestBluetooth();

        //TODO: (else) and maybe create something for the toast.
         try{
             createFile();
        fos = new FileOutputStream(pictureFile);}catch (IOException e){
             Log.d("debug","fos creation: "+e.getMessage());
         }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {



                switch (msg.what) {
                    //case STATE_CONNECTION_STARTED:
                        //text.setText(msg.getData().getString("CONN"));
                    //    break;
                   // case STATE_CONNECTION_LOST:
                   //     break;
                   // case READY_TO_CONN:
                        //startListening();
                   //     break;

                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        int bytes = msg.arg1;
                        //String readMessage = (String) msg.obj;
                        //TODO: Here start showing the picture
                       // Log.d("debug", "Msg read please!! ");
                        // construct a string from the valid bytes in the buffer
                        //  String readMessage = new String(readBuf, 0, msg.arg1);
                        //TODO: create the file
                        try {
                            Log.d("debug","bytes after: "+ bytes);
                            fos.write(readBuf,0,bytes);

                        } catch (FileNotFoundException e) {
                            Log.d("debug", "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.d("debug", "Error accessing file: " + e.getMessage());
                        }
                        break;
                    case DONE_READ:
                        try{
                            fos.close();
                            Log.d("debug", "Msg read please!! ");
                            uploadImage();
                        }catch (IOException e){
                            Log.d("debug", "Error closing file: " + e.getMessage());

                            break;
                        }
                        break;
                    default:
                        break;
                }
            }
        };

    }


    public void requestBluetooth(){

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {

            //referance: http://android-er.blogspot.com/2011/05/turn-on-bluetooth-using-intent-of.html

            if (mBluetoothAdapter == null) {

               message.setText("Make sure of the blutoothAdabter!!");//TODO:This is for devices with no bluetooth or that what i think

            } else if (mBluetoothAdapter.isEnabled()) {

                if (mBluetoothAdapter.isDiscovering()) { //TODO:See the documentation for isDiscovering()

                    message.setText("Now discovering..");
                } else {

                    //Now everything is okay :)
                    message.setText("Waiting for the Glass.."); //TODO: I need to check for sudden bt close

                     new AcceptThread().start();


                }
            } else {

                //Someone ignored the request :(
                message.setText("Bluetooth is not enabled!!");
                Toast toast=Toast.makeText(this, "Can not work without Bluetooth enabled", Toast.LENGTH_SHORT);
                toast.show();
                finish();//No Bluetooth NO Glass
                           }

        }
    }

    public void createFile(){

        pictureFile = ImageHandler.saveImage(MEDIA_TYPE_IMAGE);
        if (pictureFile == null){
            Log.d("debug", "Error creating media file, check storage permissions! ");
            return;
        }

    }


    public void uploadImage(){
        //TODO: upload activity call
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    Log.d("debug", "before accept!");
                    socket = mmServerSocket.accept();
                    Log.d("debug", "after accept!");

                } catch (IOException e) {
                    Log.d("debug", "Error in socket accept: " + e.getMessage());

                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                   // mmServerSocket.close();
                    cancel();
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.d("debug", "Error socket close: " + e.getMessage());

            }
        }

       public void  manageConnectedSocket(BluetoothSocket socket){

           //TODO: Call the connectedThread.
           new ConnectedThread(socket).start();
       }



    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            try {
            // Keep listening to the InputStream until an exception occurs
            while ((bytes = mmInStream.read(buffer))!=-1) {// Read from the InputStream


                    // Send the obtained bytes to the UI activity
                    //TODO:Here is all the work start :)
                Log.d("debug","bytes before: "+ bytes);
                  //  mHandler.obtainMessage(MESSAGE_READ, bytes, 0, buffer)
                            //.sendToTarget();
                fos.write(buffer,0,bytes);



            }} catch (IOException e) {

                mHandler.obtainMessage(DONE_READ).sendToTarget();
                Log.d("debug","DOne in exeption");

            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }



}
