package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import edu.buffalo.cse.cse486586.groupmessenger2.ISISHelper.ISISResponse;
import edu.buffalo.cse.cse486586.groupmessenger2.Message.Proposal;
import edu.buffalo.cse.cse486586.groupmessenger2.Message.Agreement;

import static edu.buffalo.cse.cse486586.groupmessenger2.Message.Status.*;


/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author veera
 *
 */
public class GroupMessengerActivity extends Activity {


    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static Set<String> failedPorts;
    boolean running = true;
    //Local counter
    static Integer counter = 0;
    //State maintainer
    static Integer s = 0;
    static Integer key;
    ISISHelper helper;
    static String myPort = new String();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        helper = new ISISHelper();
        failedPorts = new HashSet<String>();
        SharedPreferences settings = getSharedPreferences(GroupMessengerConfiguration.PREFS_NAME, 0);
        // KEY: For storing the key so that we could retrieve the latest value
        key = settings.getInt(GroupMessengerConfiguration.KEY, 0);
        /*
         * For displaying the previous messages
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        GroupMessengeDbHandler handler = new GroupMessengeDbHandler(getContentResolver());
        String [] prevMessages = handler.queryAllMessages();
        if(prevMessages != null) {
            for(String s : prevMessages) {
                StringBuilder builder = new StringBuilder(s);
                builder.append("\n");
                tv.append(builder.toString());
            }
        }

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides.
             */
            ServerSocket serverSocket = new ServerSocket(GroupMessengerConfiguration.SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        /*
         * OnClickListener for the "Send" button.
         * In the implementation we get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        /*
         * Retrieve a pointer to the input box (EditText) defined in the layout
         * XML file (res/layout/main.xml).
         *
         * This is another example of R class variables. R.id.edit_text refers to the EditText UI
         * element declared in res/layout/main.xml. The id of "edit_text" is given in that file by
         * the use of "android:id="@+id/edit_text""
         */
        final EditText editText = (EditText) findViewById(R.id.editText1);


        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String msg = editText.getText().toString();
                editText.setText(""); // This is one way to reset the input box.
                    /*
                     * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
                     * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
                     * the difference, please take a look at
                     * http://developer.android.com/reference/android/os/AsyncTask.html
                     */
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });
    }

    /*
     * ServerTask is an AsyncTask that should handle incoming messages. It is created by
     * ServerTask.executeOnExecutor() call.
     */
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            Socket server = null;
            while(running) {
                try {
                    server = serverSocket.accept();
                    DataInputStream in = new DataInputStream(server.getInputStream());
                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
                    String msg = in.readUTF();

                    if(msg != null) {
                        //Split the message
                        String[] parsedMsg = msg.split(GroupMessengerConfiguration.DELIMITER,6);
                        Integer failedPort = Integer.parseInt(parsedMsg[0]);
                        if(failedPort.intValue() != 0) {
                            failedPorts.add(failedPort.toString());
                            helper.removeMessages(failedPort);
                        }

                        if(parsedMsg[3].equals(MESSAGE)) {
                            s = s + 1;
                            //Need to send message id and sequence no to Pj
                            Proposal proposal = new Proposal(Integer.parseInt(parsedMsg[4]), s, Integer.parseInt(myPort));
                            String proposalMsg = proposal.toString();
                            out.writeUTF(proposalMsg);
                            //put item in hold back queue
                            ISISResponse response = helper.newMessage(s,Integer.parseInt(myPort),msg);
                            if(response.deliverableMessage != null) {
                                deliverMessage(response.deliverableMessage);
                            }
                        } else if(parsedMsg[3].equals(AGREEMENT)) {
                            Integer sSeqNo = Integer.parseInt(parsedMsg[4]);
                            s = Math.max(s.intValue(), sSeqNo.intValue());
                            String[] msgs = helper.agreedOnProposal(msg);
                            if(msgs != null) {
                                deliverMessage(msgs);
                            }
                        }
                    }
                    server.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception in server");
                }
            }
            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground() and handles db.
             */
            String strReceived = strings[0].trim();
            TextView textView = (TextView) findViewById(R.id.textView1);
            textView.append(strReceived);
            textView.append("\n");
            return;
        }

        protected boolean deliverMessage(String[] msgs) {
            for(String str: msgs) {
                GroupMessengeDbHandler handler = new GroupMessengeDbHandler(getContentResolver());
                String k= key.toString();
                String value= str;
                boolean status = handler.insertMessage(k, value);
                if(status) {
                    key++;
                    publishProgress(str);
                }
            }
            return true;
        }
    }
    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever setOnClickListener detects
     * an click event.
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            String remotePort = null;
            Integer failerProcess = 0;
            try {
                counter++;
                Integer initialSeqNo = counter;
                PriorityQueue<Proposal> proposalQueue = new PriorityQueue<Proposal>();
                //Sending Message
                for(int i=0; i < GroupMessengerConfiguration.REMOTE_PORTS.length; i++) {
                    try{
                        if(!failedPorts.contains(GroupMessengerConfiguration.REMOTE_PORTS[i])) {
                            remotePort = GroupMessengerConfiguration.REMOTE_PORTS[i];
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}) ,Integer.parseInt(remotePort));
                            socket.setSoTimeout(GroupMessengerConfiguration.TIMEOUT_TIME);
                            socket.setTcpNoDelay(false);
                            Integer owner = Integer.parseInt(msgs[1]);
                            Message msg = new Message(initialSeqNo, owner, MESSAGE, msgs[0], failerProcess);
                            String msgToSend = msg.toString();
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                            //Send data to server
                            out.writeUTF(msgToSend);

                            //Receive data from server
                            DataInputStream in = new DataInputStream(socket.getInputStream());
                            String proposal = in.readUTF();
                            String[] parsedProposal = proposal.split(GroupMessengerConfiguration.DELIMITER, 3);
                            proposalQueue.add(new Proposal(Integer.parseInt(parsedProposal[0]), Integer.parseInt(parsedProposal[1]),Integer.parseInt(parsedProposal[2])));
                            socket.close();
                        }
                    } catch (SocketException e) {
                        Log.e(TAG, "ClientTask Socket Exception");
                        failedPorts.add(remotePort);
                        helper.removeMessages(Integer.parseInt(remotePort));
                    } catch (IOException e) {
                        Log.e(TAG, "ClientTask socket IOException ");
                        Log.d(TAG, "Remote port failed "+remotePort);
                        failedPorts.add(remotePort);
                        failerProcess = Integer.parseInt(remotePort);
                        helper.removeMessages(failerProcess);
                    }
                }

                //Got all the proposals, now need to decide on the max sequence number and send it to all
                Proposal winner = proposalQueue.peek();
                Agreement agreement = new Agreement(failerProcess, winner.messageId, Integer.parseInt(myPort), AGREEMENT, winner.proposedSequenceNo, winner.proposer);
                String agreementMessage = agreement.toString();

                for(int i=0; i < GroupMessengerConfiguration.REMOTE_PORTS.length; i++) {
                    try{
                        if(!failedPorts.contains(GroupMessengerConfiguration.REMOTE_PORTS[i])) {
                            remotePort = GroupMessengerConfiguration.REMOTE_PORTS[i];
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(remotePort));
                            socket.setTcpNoDelay(false);
                            socket.setSoTimeout(GroupMessengerConfiguration.TIMEOUT_TIME);
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                            //Send data to server
                            out.writeUTF(agreementMessage);
                            socket.close();
                        }
                    } catch (SocketException e) {
                        Log.e(TAG, "ClientTask Socket Exception");
                        failedPorts.add(remotePort);
                        helper.removeMessages(Integer.parseInt(remotePort));
                    } catch (IOException e) {
                        Log.e(TAG, "ClientTask socket IOException ");
                        Log.d(TAG, "Remote port failed "+remotePort);
                        failedPorts.add(remotePort);
                        failerProcess = Integer.parseInt(remotePort);
                        helper.removeMessages(failerProcess);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "ClientTask socket Thread");
            }

            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        running = false;
        SharedPreferences settings = getSharedPreferences(GroupMessengerConfiguration.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(GroupMessengerConfiguration.KEY, key);
        // Commit the edits!
        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        running = true;
    }

}
