package edu.buffalo.cse.cse486586.groupmessenger2;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Created by veera on 3/4/18.
 */

public class ISISHelper {

    PriorityQueue<MessageData> message = new PriorityQueue<MessageData>();
    static final String TAG = ISISHelper.class.getSimpleName();

    public ISISRespone newMessage(Integer proposedSeq, Integer proposer, String msg) {
        /*
            Parsed Message Data structure
            0 Failed port
            1 Initial Sequence No
            2 Owner
            3 Type
            4 Id
            5 Message
         */

        String[] parsedMsg = msg.split("::",6);

        /* Message data structure
             1 id
             2 message
             3 initialSeqNo
             4 owner
             5 proposedseqNo
             6 proposer
             7 isDeliverable
         */
        MessageData messageData = new MessageData(Integer.parseInt(parsedMsg[4]), parsedMsg[5], Integer.parseInt(parsedMsg[1]), Integer.parseInt(parsedMsg[2]), proposedSeq, proposer, 0);
        message.add(messageData);

        String deliverableMessage[] = checkDelivery();

        return new ISISRespone(true, deliverableMessage);
    }

    public String[] agreedOnProposal(String msg) {
        /*
            Parsed Message Data structure
            0 Failure
            1 Message Id
            2 Owner
            3 Type
            4 Agreed Seq
            5 Proposer
         */
        String[] parsedMsg = msg.split("::",6);
        Integer msgId = Integer.parseInt(parsedMsg[1]);
        Integer agreedProposedSeqNo = Integer.parseInt(parsedMsg[4]);
        Integer agreedProposedSeqNoProposer = Integer.parseInt(parsedMsg[5]);
        for (MessageData element : message) {
            if(element.id.intValue() == msgId.intValue()) {
                MessageData temp = element;
                message.remove(element);
                if(temp.proposedseqNo < agreedProposedSeqNo) {
                    temp.proposedseqNo = agreedProposedSeqNo;
                    temp.proposer = agreedProposedSeqNoProposer;
                }
                temp.isDeliverable = 1;
                message.add(temp);
            }
        }
        return checkDelivery();
    }

    private String[] checkDelivery() {
        ArrayList<String> deliverableMessages = new ArrayList<String>();
        while(message.peek() != null && message.peek().isDeliverable == 1) {
            //Deliver the message to the application
            MessageData data = message.poll();
            if(data != null)
                deliverableMessages.add(data.message);
        }
        if(deliverableMessages !=null)
            return deliverableMessages.toArray(new String[deliverableMessages.size()]);
        else
            return null;
    }

    public boolean removeMessages(Integer owner) {
        for(MessageData element : message) {
            if(element.owner.intValue() == owner.intValue()) {
                message.remove(element);
            }
        }
        return true;
    }


    static class ISISRespone {
        boolean status;
        String[] deliverableMessage;

        public ISISRespone(boolean status, String[] deliverableMessage) {
            this.status = status;
            this.deliverableMessage = deliverableMessage;
        }
    }

}
