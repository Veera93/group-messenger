package edu.buffalo.cse.cse486586.groupmessenger2;

/**
 * Created by veera on 3/4/18.
 */

public class Message {
    Integer failedPort;
    Integer seqNo;
    Integer owner;
    String type;
    String message;
    Integer id;

    public Message(Integer seqNo,Integer owner, String type,  String message, Integer failedPort) {
        this.seqNo = seqNo;
        this.message = message;
        this.owner = owner;
        this.type = type;
        this.failedPort = failedPort;
    }

    @Override
    public String toString() {
        String temp = this.seqNo + this.owner + this.message;
        this.id = temp.hashCode();
        return this.failedPort+"::"+this.seqNo+"::"+this.owner+"::"+this.type+"::"+this.id+"::"+this.message;
    }

    public static class Proposal implements Comparable<Proposal>{
        Integer messageId;
        Integer proposedSequenceNo;
        Integer proposer;


        public Proposal(Integer messageId, Integer proposedSequenceNo,Integer proposer) {
            this.messageId = messageId;
            this.proposedSequenceNo = proposedSequenceNo;
            this.proposer = proposer;
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

        @Override
        public String toString() {
            return messageId.toString() + "::" + proposedSequenceNo.toString()+ "::" + proposer.toString();
        }
    }

    public static class Agreement {

        Integer failerProcess;
        Integer messageId;
        Integer originator;
        String type;
        Integer sequenceNo;
        Integer proposer;

        public Agreement(Integer failerProcess, Integer messageId, Integer originator, String type, Integer sequenceNo, Integer proposer) {
            this.failerProcess = failerProcess;
            this.messageId = messageId;
            this.originator = originator;
            this.type = type;
            this.sequenceNo = sequenceNo;
            this.proposer = proposer;
        }

        @Override
        public String toString() {
            return failerProcess+"::"+messageId+"::"+originator+"::"+type+"::"+sequenceNo+"::"+proposer;
        }
    }

    public static class Status {
        public final static String MESSAGE = "message";
        public final static String AGREEMENT = "agreement";
        public final static String PROPOSAL = "proposal";
    }
}
