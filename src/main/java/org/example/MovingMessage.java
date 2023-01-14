package org.example;

import io.jbotsim.core.*;

public class MovingMessage extends Node {

    Node emet;
    Node dest;
    Message msg;
    Point destPos;

    Topology tp;
    Point pos;

    public MovingMessage(Node e, Node d, Message m) {

        this.emet = e;
        this.dest = d;
        this.msg = m;
    }

    @Override
    public void onStart() {

        destPos = dest.getLocation();

        this.setCommunicationRange(0);
        this.setSensingRange(10);
        this.setIconSize(20);
        SyncMessage mm = (SyncMessage) msg.getContent();

        switch ( mm.getMsgType()) {
            case REQ_READ:
                this.setIcon("src/main/resources/read.png");
                break;
            case REQ_WRITE:
                this.setIcon("src/main/resources/edit.png");
                break;
            case ACK_READ:
                this.setIcon("src/main/resources/approuve.png");
                break;
            case ACK_WRITE:
                this.setIcon("src/main/resources/jeton.png");
                break;
        }
    }

    @Override
    public void onClock() {

        setDirection(destPos);
        move(10);
    }

    @Override
    public void onSensingIn(Node node) {

        if (dest.compareTo(node) == 0) {

            ((RobotWithFrameAndMessages) node).deliver(this);
            //BroadcastNode bn = (BroadcastNode) node;
            //bn.deliver(this);
            this.die();
        }
    }

    public Node getDest() {
        return dest;
    }

    public Node getEmet() {
        return emet;
    }

    public Message getMessage() {
        return msg;
    }

    public void setColer(Color c) {
        this.setColor(c);
    }
}
