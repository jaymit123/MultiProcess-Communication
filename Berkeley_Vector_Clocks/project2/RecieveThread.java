/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import project2.Message.MessageType;

/**
 *
 * @author Jaymit Desai
 */
public class RecieveThread implements Runnable {

    private int vectorClock[];
    private InetAddress multicastAddr;
    private MulticastSocket multiSocket;
    private LinkedList<Message> vectorStorage;
    private ProcessClass ownerProcess;
    private int processID;
    private int portNo;

    public RecieveThread(ProcessClass ownerP, int[] vc, String multiCastAddress, int portNo) {
        try {
            this.portNo = portNo;
            ownerProcess = ownerP;
            vectorClock = vc;
            multicastAddr = InetAddress.getByName(multiCastAddress);
            multiSocket = new MulticastSocket(portNo);
            multiSocket.joinGroup(multicastAddr);
            vectorStorage = new LinkedList<>();
            processID = ownerProcess.getProcessID();
        } catch (IOException ex) {
            System.out.println("Sorry, could not create multicast connection: " + ex);
        }
    }

    public void run() {
        try {
            startRecieving();
        } catch (IOException ex) {
            System.out.println("" + ex);
        }
        System.out.println("Ordered Multicasting using Vector Clocks - Complete");
    }

    public void startRecieving() throws IOException {

        MessageType CurrentMsg = MessageType.TIMESTAMP;
        for (int i = 0; i < vectorClock.length * 2; i++) {
            CurrentMsg = processRecieve();

        }

    }
// recieve multiple
    //vc msgs last one not recieved

    public MessageType processRecieve() throws IOException {
        MessageType CurrentMsg;
        Object[] msg = ownerProcess.recieveInformation(multiSocket);

        DatagramPacket pkt = (DatagramPacket) msg[0];

        Message m = (Message) msg[1];
        CurrentMsg = m.getType();
        if (m.getProcessID() == this.processID) {
            return CurrentMsg;
        }
        if (handleVC(m)) {
            if (vectorStorage.size() > 0) {
                Iterator<Message> i = vectorStorage.iterator();
                while (i.hasNext()) {
                    Message currMsg = i.next();
                    if (handleVC(currMsg)) {
                        System.out.println("Msg removed from linkedlist");
                        vectorStorage.remove(currMsg);
                    }
                }
            }
        }

        return CurrentMsg;
    }

    public boolean handleVC(Message m) {
        int[] tempVector = (int[]) ownerProcess.readObject(m.getMessage());
        boolean isSatisfied = true;
        int i = 0;
        while (i < vectorClock.length && isSatisfied) {
            if (i == m.getProcessID()) {
                if (tempVector[i] != (this.vectorClock[i] + 1)) {
                    isSatisfied = false;
                    vectorStorage.add(m);
                }

            } else if (!(tempVector[i] <= vectorClock[i])) {
                isSatisfied = false;
                vectorStorage.add(m);
            }
            ++i;
        }

        if (isSatisfied) {
            System.out.println("P" + m.getProcessID() + " --(sends)--> P" + processID + ". Vector Clock  P" + processID + ":  " + updateVector(tempVector));
        }
        return isSatisfied;
    }

    public String updateVector(int[] v) {
        String vectorForm = "(";
        for (int i = 0; i < v.length - 1; i++) {
            vectorClock[i] = Math.max(vectorClock[i], v[i]);
            vectorForm += vectorClock[i] + ", ";
        }
        vectorClock[v.length - 1] = Math.max(vectorClock[v.length - 1], v[v.length - 1]);
        vectorForm += vectorClock[v.length - 1] + ")";
        return vectorForm;
    }

}
