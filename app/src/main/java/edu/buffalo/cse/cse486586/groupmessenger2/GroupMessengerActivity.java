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

import edu.buffalo.cse.cse486586.groupmessenger2.ISISHelper.ISISRespone;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final int SERVER_PORT = 10000;
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String[] REMOTE_PORTS = {"11108", "11112", "11116", "11120", "11124"};
    static Set<String> failedPorts;
    boolean running = true;
    public static final String PREFS_NAME = "CounterPrefsFile";
    static Integer counter = 0;
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
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        key = settings.getInt("key", 0);

        Log.d(TAG,key+" start key");
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
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
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

    /***
     * ServerTask is an AsyncTask that should handle incoming messages. It is created by
     * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
     *
     * Please make sure you understand how AsyncTask works by reading
     * http://developer.android.com/reference/android/os/AsyncTask.html
     *
     *
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
                        String[] parsedMsg = msg.split("::",6);
                        Integer failedPort = Integer.parseInt(parsedMsg[0]);
                        if(failedPort.intValue() != 0) {
                            failedPorts.add(failedPort.toString());
                            helper.removeMessages(failedPort);
                        }
                        if(parsedMsg[3].equals("message")) {
                            Log.d(TAG+"SERVER: ", "B-delivered: message= "+ parsedMsg[5]+" id= "+parsedMsg[4]+" at "+myPort);
                            s = s + 1;
                            //Need to send message id and sequence no to Pj
                            String proposal = parsedMsg[4] + "::" + s.toString()+ "::" + myPort;
                            Log.d(TAG+"SERVER: ", "Sending: Proposal= "+ s+" for id= "+parsedMsg[4]+" at "+myPort);
                            out.writeUTF(proposal);
                            //put item in hold back queue
                            ISISRespone response = helper.newMessage(s,Integer.parseInt(myPort),msg);
                            if(response.deliverableMessage != null) {
                                deliverMessage(response.deliverableMessage);
                            }
                        } else if(parsedMsg[3].equals("agree")) {
                            Integer sSeqNo = Integer.parseInt(parsedMsg[4]);
                            Log.d(TAG+"SERVER: ", "Final Got Agree "+ sSeqNo+" for id= "+parsedMsg[1]+" at "+myPort);
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
                Log.d(TAG,"Final Inserting "+str+" at "+k);
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
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     *
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
                for(int i=0; i < REMOTE_PORTS.length; i++) {
                    try{
                        if(!failedPorts.contains(REMOTE_PORTS[i])) {
                            remotePort = REMOTE_PORTS[i];
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}) ,Integer.parseInt(remotePort));
                            socket.setSoTimeout(300);
                            socket.setTcpNoDelay(false);
                            Integer owner = Integer.parseInt(msgs[1]);
                            Message msg = new Message(initialSeqNo, owner, "message", msgs[0], failerProcess);
                            //Integer seqNo,Integer owner, String type,  String message

                            String msgToSend = msg.concatMessage();
                            Log.d(TAG+" CLIENT: ", "Sending msg: "+ msgs[0]+" at "+ msgs[1]+" to "+REMOTE_PORTS[i]+ " with Id: "+msg.id);
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                            //Send data to server
                            out.writeUTF(msgToSend);

                            //Receive data from server
                            DataInputStream in = new DataInputStream(socket.getInputStream());
                            String proposal = in.readUTF();
                    /*
                           Proposal Structure
                           0 Message Id
                           1 Proposed Sequence No
                           2 Proposer
                     */
                            String[] parsedProposal = proposal.split("::", 3);
                            Log.d(TAG+" CLIENT: ", "Got proposal from: "+ parsedProposal[2]+" at "+ myPort+" as "+parsedProposal[1]+ " for Id: "+parsedProposal[0]);
                            proposalQueue.add(new Proposal(Integer.parseInt(parsedProposal[0]), Integer.parseInt(parsedProposal[1]),Integer.parseInt(parsedProposal[2])));
                            socket.close();
                        }
                    } catch (SocketException e) {
                        Log.e(TAG, "ClientTask Socket Exception");
                        //Todo Remove from list
                        failedPorts.add(remotePort);
                        helper.removeMessages(Integer.parseInt(remotePort));
                    } catch (IOException e) {
                        Log.e(TAG, "ClientTask socket IOException ");
                        Log.d(TAG, "Remote port failed "+remotePort);
                        //Todo Remove from list
                        failedPorts.add(remotePort);
                        failerProcess = Integer.parseInt(remotePort);
                        helper.removeMessages(failerProcess);
                    }
                }

                //Got all the proposals, now need to decide on the max sequence number and send it to all
                Proposal winner = proposalQueue.peek();

                // 0 Id 1 Owner 2 type 3 seq 4 proposer
                String agreed = failerProcess+"::"+winner.messageId+"::"+myPort+"::"+"agree"+"::"+winner.proposedSequenceNo+"::"+winner.proposer;
                for(int i=0; i < REMOTE_PORTS.length; i++) {
                    try{
                        if(!failedPorts.contains(REMOTE_PORTS[i])) {
                            remotePort = REMOTE_PORTS[i];
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(remotePort));
                            socket.setTcpNoDelay(false);
                            socket.setSoTimeout(500);
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                            Log.d(TAG+" CLIENT: ", "Sending agreed Seq: "+ winner.proposedSequenceNo+" at "+ myPort+" to "+REMOTE_PORTS[i]+ " for Id: "+winner.messageId);
                            //Send data to server
                            out.writeUTF(agreed);
                            socket.close();
                        }
                    } catch (SocketException e) {
                        Log.e(TAG, "ClientTask Socket Exception");
                        //Todo Remove from list
                        failedPorts.add(remotePort);
                        helper.removeMessages(Integer.parseInt(remotePort));
                    } catch (IOException e) {
                        Log.e(TAG, "ClientTask socket IOException ");
                        Log.d(TAG, "Remote port failed "+remotePort);
                        //Todo Remove from list
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
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("key", key);
        Log.d(TAG,key+" end key");
        // Commit the edits!
        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        running = true;
    }
}
