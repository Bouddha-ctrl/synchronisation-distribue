package org.example;

public class SyncMessage {

    MsgType type;
    int robot;

    SyncMessage( MsgType t, int r ) {

        type = t;
        robot = r;
    }

    public MsgType getMsgType() { return type; }
    public int getRobotId() { return robot; }
}