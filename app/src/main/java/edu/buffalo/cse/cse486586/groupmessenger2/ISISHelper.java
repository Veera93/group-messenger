package edu.buffalo.cse.cse486586.groupmessenger2;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Created by veera on 3/4/18.
 */

public class ISISHelper {

    PriorityQueue<MessageQueue> holdbackQueue = new PriorityQueue<MessageQueue>();
    static final String TAG = ISISHelper.class.getSimpleName();

    public ISISResponse newMessage(Integer proposedSeq, Integer proposer, String msg) {
        String[] parsedMsg = msg.split(GroupMessengerConfiguration.DELIMITER,6);
        MessageQueue messageData = new MessageQueue(Integer.parseInt(parsedMsg[4]), parsedMsg[5], Integer.parseInt(parsedMsg[1]), Integer.parseInt(parsedMsg[2]), proposedSeq, proposer, 0);
        holdbackQueue.add(messageData);
        String deliverableMessage[] = checkDelivery();
        return new ISISResponse(true, deliverableMessage);
    }

    public String[] agreedOnProposal(String msg) {
        String[] parsedMsg = msg.split(GroupMessengerConfiguration.DELIMITER,6);
        Integer msgId = Integer.parseInt(parsedMsg[1]);
        Integer agreedProposedSeqNo = Integer.parseInt(parsedMsg[4]);
        Integer agreedProposedSeqNoProposer = Integer.parseInt(parsedMsg[5]);
        for (MessageQueue element : holdbackQueue) {
            if(element.id.intValue() == msgId.intValue()) {
                MessageQueue temp = element;
                holdbackQueue.remove(element);
                if(temp.proposedseqNo < agreedProposedSeqNo) {
                    temp.proposedseqNo = agreedProposedSeqNo;
                    temp.proposer = agreedProposedSeqNoProposer;
                }
                temp.isDeliverable = 1;
                holdbackQueue.add(temp);
            }
        }
        return checkDelivery();
    }

    private String[] checkDelivery() {
        ArrayList<String> deliverableMessages = new ArrayList<String>();
        while(holdbackQueue.peek() != null && holdbackQueue.peek().isDeliverable == 1) {
            //Deliver the message to the application
            MessageQueue data = holdbackQueue.poll();
            if(data != null)
                deliverableMessages.add(data.message);
        }
        if(deliverableMessages !=null)
            return deliverableMessages.toArray(new String[deliverableMessages.size()]);
        else
            return null;
    }

    public boolean removeMessages(Integer owner) {
        for(MessageQueue element : holdbackQueue) {
            if(element.owner.intValue() == owner.intValue()) {
                holdbackQueue.remove(element);
            }
        }
        return true;
    }

    static class ISISResponse {
        boolean status;
        String[] deliverableMessage;

        public ISISResponse(boolean status, String[] deliverableMessage) {
            this.status = status;
            this.deliverableMessage = deliverableMessage;
        }
    }

}
