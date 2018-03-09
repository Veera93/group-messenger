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

    public String concatMessage() {
        String temp = this.seqNo + this.owner + this.message;
        this.id = temp.hashCode();
        return this.failedPort+"::"+this.seqNo+"::"+this.owner+"::"+this.type+"::"+this.id+"::"+this.message;
    }
}
