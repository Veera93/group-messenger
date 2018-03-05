package edu.buffalo.cse.cse486586.groupmessenger2;

/**
 * Created by veera on 3/4/18.
 */

public class Message {
    Integer seqNo;
    String message;
    Integer proposer;
    String type;

    public Message(Integer seqNo, String message, Integer proposer, String type) {
        this.seqNo = seqNo;
        this.message = message;
        this.proposer = proposer;
        this.type = type;
    }
    public String concatMessage() {
        return this.seqNo+":"+this.message+":"+this.proposer+this.type;
    }
}
