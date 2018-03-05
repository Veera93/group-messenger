package edu.buffalo.cse.cse486586.groupmessenger2;

/**
 * Created by veera on 3/4/18.
 */

public class MessageData {
    Integer seqNo;
    String message;
    Proposal proposals;
    boolean isDeliverable;
    Integer proposalCount;

    public MessageData(Integer seqNo, String message, Proposal proposals, boolean isDeliverable, Integer proposalCount) {
        this.seqNo = seqNo;
        this.message = message;
        this.proposals = proposals;
        this.isDeliverable = isDeliverable;
        this.proposalCount = proposalCount;
    }

    class Proposal {
        Integer id;
        Integer SeqNo;
        boolean isAlive;

        public Proposal(Integer id, Integer seqNo, boolean isAlive) {
            this.id = id;
            SeqNo = seqNo;
            this.isAlive = isAlive;
        }
    }
}
