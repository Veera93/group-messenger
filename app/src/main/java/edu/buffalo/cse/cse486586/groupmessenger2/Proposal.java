package edu.buffalo.cse.cse486586.groupmessenger2;

/**
 * Created by veera on 3/6/18.
 */

public class Proposal implements Comparable<Proposal>{
    Integer messageId;
    Integer proposedSequenceNo;
    Integer proposer;


    public Proposal(Integer messageId, Integer proposedSequenceNo,Integer proposer) {
        this.messageId = messageId;
        this.proposer = proposer;
        this.proposedSequenceNo = proposedSequenceNo;
    }

    public Proposal() {

    }

    @Override
    public int compareTo(Proposal another) {
        if(this.proposedSequenceNo == another.proposedSequenceNo) {
            //Same sequence number then lowest proposer
            return this.proposer - another.proposer;
        } else {
            //Highest sequence number
            return -(this.proposedSequenceNo - another.proposedSequenceNo);
        }
    }
}
