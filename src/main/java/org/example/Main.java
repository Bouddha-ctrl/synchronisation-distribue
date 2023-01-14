package org.example;

import io.jbotsim.core.DelayMessageEngine;
import io.jbotsim.core.Topology;
import io.jbotsim.ui.JViewer;

public class Main {
    public static void main(String[] args){

        Topology tp = new Topology(1000, 1000);
        tp.setCommunicationRange(1000.0);
        tp.setDefaultNodeModel(RobotWithFrameAndMessages.class);
        tp.setTimeUnit(100);
        new JViewer(tp);
        tp.start();
    }
}
