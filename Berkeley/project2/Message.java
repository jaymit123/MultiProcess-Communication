/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project2;

import java.io.Serializable;

/**
 *
 * @author Jaymit Desai
 */
public class Message implements Serializable {

    private int counter;
    private byte[] message;
    private MessageType type;

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
    private int processID;

    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    public Message(int pid, int counter, byte[] message, MessageType type) {
        this.processID = pid;
        this.counter = counter;
        this.message = message;
        this.type = type;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public byte[] getMessage() {
        return message;
    }

    public String getMessageString() {
        String msg = null;

        msg = new String(message);
        return msg;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public enum MessageType implements Serializable {
        TEXT, FILE, TIME_DIFF, CHANGE_TIME, DAEMON_COUNTER,SEND_PROCESS_LIST,TIMESTAMP,TIMESTAMP_FINAL;

    }

}
