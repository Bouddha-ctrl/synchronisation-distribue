package org.example;

import io.jbotsim.core.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RobotWithFrameAndMessages extends Node {

    int myID;
    Node owner = null;
    List<Node> neighborList;
    List<Node> reader;

    boolean init = false;

    boolean JETON = false;
    int criticalTime = 0;
    Random rand;

    boolean onWrite = false;
    boolean onRead = false;
    boolean waitForWrite = false;
    boolean next = false;
    boolean myTurn =  false;
    int nb_reader = 0;

    int nb_click = 0;
    Date date = null;
    DisplayFrame df = null;
    Topology tp;
    Point pos;

    // Display state
    void displayState(String info) {

        String state = new String();

        state = state + "--------------------------------------\n";
        state = state + "** Process " + getID() + " **\n";
        state = state + "Owner " + ((owner!=null)?owner.getID():-1) + " **\n";
        state = state + "Jeton " + JETON + " **\n";
        state = state + "wait for write " + waitForWrite + " **\n";
        state = state + "onWrite " + onWrite + " **\n";
        state = state + "onRead " + onRead + " **\n";
        state = state + "nb Reader " + nb_reader + " **\n";
        state = state + "next " + next + " **\n";
        state = state + "Info " + info + " rcvd";
        df.display( state );
    }

    @Override
    public void onStart() { //creation d'objet
        setColor(null);
        this.setIcon("src/main/resources/img.png");
        setIconSize(40);
        tp = this.getTopology();
        pos = this.getLocation();

        myID = this.getID();

        rand = new Random(myID);
        init();
        if ( df != null ) df.deleteFrame();
        df = new DisplayFrame( this, (int)this.getLocation().getX(), (int)this.getLocation().getY() );
        displayState("rien");
    }

    @Override
    public void onClock() {
        neighborList = getNeighbors();
        if (!init) {
            if (this.getID() != 0) {

                owner = neighborList.get(0);
            }else{
                JETON = true;
                owner = null;
            }
            init = true;
        }

        // Manage access to critical
        if ( onWrite ) {
            criticalTime--;
            if ( criticalTime == 0 ) endWrite();
        }
        if ( onRead ) {
            criticalTime--;
            if ( criticalTime == 0 ) endRead();
        }
        if(nb_click == 1 && date.before(new Date())){
            onSelectionRead();
            nb_click=0;
            date = null;
        }
        displayState("onclock");
    }

    @Override
    public void onSelection() { // click souris
        nb_click++;
        if (nb_click==2){
            onSelectionWrite();
            nb_click = 0;
            date = null;
        }else if (nb_click == 1){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 1);
            date = calendar.getTime();
        }

    }

    public void onSelectionRead(){
        System.out.println("Robot " + this.getID() + " asks for read");
        AskForRead();
        setColor(Color.YELLOW);
    }
    public void onSelectionWrite(){
        System.out.println("Robot " + this.getID() + " asks for write");
        AskForWrite();
        setColor(Color.ORANGE);
    }

    public void deliver(MovingMessage msg) { // reception

        Message rcvMsg =  msg.getMessage();
        SyncMessage mm = (SyncMessage) rcvMsg.getContent();
        System.out.println("Node " + this.getID() + " received " + mm.getMsgType() + " from " + mm.getRobotId());
        switch ( mm.getMsgType()) {
            case REQ_READ:
                receiveReqRead( msg.getEmet(), mm.getRobotId());
                break;
            case REQ_WRITE:
                receiveReqWrite( msg.getEmet(), mm.getRobotId());
                break;
            case ACK_READ:
                receiveAckRead( msg.getEmet() );
                break;
            case ACK_WRITE:
                receiveAckWrite( msg.getEmet() );
                break;
            case REL :
                receiveRel( msg.getEmet() );
                break;
            default:
                System.out.println("Error message type");
        }
        displayState( "Message recu" );

        try {
            Thread.sleep( 1000 );
        } catch ( InterruptedException ie ) { ie.printStackTrace(); }
    }



    public void init(){
        nb_reader = 0;
        onWrite = false;
        waitForWrite = false;
        next = false;
        JETON = false;
        myTurn = false;
        reader = new ArrayList<Node>();
    }

    public void receiveReqRead(Node robot, int id){
        Node sender = getRobotById(id);

        if (owner != null) {
            sendReqRead(owner, id);
            return;
        }

        if (JETON && !onWrite && !waitForWrite){
            nb_reader++;
            sendAckRead(sender, myID);
            return;
        }

        reader.add(robot);
    }

    public void receiveReqWrite(Node robot, int id){
        Node sender = getRobotById(id);
        if(owner == null){
            if (JETON){
                if(nb_reader == 0 && !onWrite && !waitForWrite){
                    init();
                    sendAckWrite(sender, myID);
                }
            }else
                next = true;
        }else
            sendReqWrite(owner, id);

        owner = sender;
    }


    public void receiveAckRead(Node robot){
        owner = robot;
        startRead();

    }
    public void receiveAckWrite(Node robot){
        JETON = true;
        startWrite();
        setColor(Color.BLUE);
    }

    public void receiveRel(Node robot ){
        nb_reader--;
        if (nb_reader == 0)
            if (myTurn)
                startWrite();
            else if (next){
                sendAckWrite(owner,myID);
                init();
            }
    }


    public void AskForWrite(){
        setColor(Color.RED);
        waitForWrite = true;
        if (!next && JETON){
            if (nb_reader == 0)
                startWrite();
            else
                myTurn = true;
        }else{
            sendReqWrite(owner,myID);
            owner = null;
        }
    }
    public void startWrite(){
        System.out.println("Robot " + this.getID() + " starts writing");
        randomlyTime();
        onWrite = true;
        waitForWrite = false;
    }

    public void endWrite(){
        System.out.println("Robot " + this.getID() + " ends writing");
        onWrite = false;
        if (reader.size() > 0){
            reader.forEach(node -> {
                nb_reader++;
                sendAckRead(node, myID);
            });
            reader = new ArrayList<Node>();
        }
        if (nb_reader == 0 && next){
            sendAckWrite(owner, myID);
            init();
        }

        setColor(null);
    }


    public void AskForRead(){
        if (JETON && !next){
            startRead();
        }else{
            sendReqRead(owner,myID);
        }
    }

    public void startRead(){
        System.out.println("Robot " + this.getID() + " starts reading");
        setColor(Color.GREEN);
        randomlyTime();
        onRead = true;
        if (JETON){
            nb_reader++;
        }
    }

    public void endRead(){
        System.out.println("Robot " + this.getID() + " ends reading");
        onRead = false;
        if (JETON)
            receiveRel(null);
        else
            sendRel(owner, myID);

        setColor(null);
    }

    public void sendReqRead(Node dest, int id){
        SyncMessage sm = new SyncMessage(MsgType.REQ_READ, id);
        Message m = new Message(sm);
        MovingMessage mm = new MovingMessage( this, dest, m );
        tp.addNode( pos.getX(), pos.getY(), mm );
    }

    public void sendReqWrite(Node dest, int id){
        SyncMessage sm = new SyncMessage(MsgType.REQ_WRITE, id);
        Message m = new Message(sm);
        MovingMessage mm = new MovingMessage( this, dest, m );
        tp.addNode( pos.getX(), pos.getY(), mm );
    }

    public void sendAckRead(Node dest, int id){

        SyncMessage sm = new SyncMessage(MsgType.ACK_READ, id);
        Message m = new Message(sm);
        MovingMessage mm = new MovingMessage( this, dest, m );
        tp.addNode( pos.getX(), pos.getY(), mm );
    }

    public void sendAckWrite(Node dest, int id){
        SyncMessage sm = new SyncMessage(MsgType.ACK_WRITE, id);
        Message m = new Message(sm);
        MovingMessage mm = new MovingMessage( this, dest, m );
        tp.addNode( pos.getX(), pos.getY(), mm );
    }

    public void sendRel(Node dest, int id){
        SyncMessage sm = new SyncMessage(MsgType.REL, id);
        Message m = new Message(sm);
        MovingMessage mm = new MovingMessage( this, dest, m );
        tp.addNode( pos.getX(), pos.getY(), mm );
    }

    public Node getRobotById(int id){
        return neighborList.stream().filter(node -> node.getID()==id).collect(Collectors.toList()).get(0);
    }

    public void randomlyTime(){
        criticalTime = 2 + rand.nextInt(5);
        System.out.println("random time :"+criticalTime);
    }
}
