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

import java.util.LinkedList;

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
            System.out.println("Non Ordered Multicasting Complete");
        } catch (IOException ex) {
            System.out.println("" + ex);
        }
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
        System.out.println("Recievied Message from Process " + m.getProcessID() + " having counter :" + m.getCounter());

        return CurrentMsg;
    }

}
