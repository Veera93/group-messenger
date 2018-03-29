package edu.buffalo.cse.cse486586.groupmessenger2;

/**
 * Created by veera on 3/4/18.
 */

public class MessageQueue implements Comparable<MessageQueue> {

    Integer id;
    String message;
    Integer initialSeqNo;
    Integer owner;
    Integer proposedseqNo;
    Integer proposer;
    int isDeliverable;


    public MessageQueue() {

    }

    public MessageQueue(Integer id, String message, Integer initialSeqNo, Integer owner, Integer proposedseqNo, Integer proposer, int isDeliverable) {
        this.id = id;
        this.message = message;
        this.initialSeqNo = initialSeqNo;
        this.owner = owner;
        this.proposedseqNo = proposedseqNo;
        this.proposer = proposer;
        this.isDeliverable = isDeliverable;
    }

    //ToDo: Need to check if this proirity is okay
    @Override
    public int compareTo(MessageQueue another) {
        if (this.proposedseqNo == another.proposedseqNo) {
            return this.proposer - another.proposer;
        } else {
            //Smallest Sequence No at the head
            return this.proposedseqNo - another.proposedseqNo;
        }

        /*
        if (this.proposedseqNo == another.proposedseqNo) {
            if(this.isDeliverable == another.isDeliverable) {
                //Placing messages from lower process at the head
                return this.proposer - another.proposer;
            } else {
                //Placing undeliverable messages at the head
                return this.isDeliverable - another.isDeliverable;
            }

        } else {
            //Smallest Sequence No at the head
            return this.proposedseqNo - another.proposedseqNo;
        }
         */
    }

}
