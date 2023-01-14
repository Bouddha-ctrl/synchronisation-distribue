package org.example;

import javax.swing.*;
import java.awt.*;

public class DisplayFrame {

    RobotWithFrameAndMessages node;
    
    JFrame frame;
    JTextArea textArea;

    int size;
    int proc;
    int speed =5;
    String displayString = "     ";
    
    public DisplayFrame(RobotWithFrameAndMessages a, int x, int y ) {

		node = a;
		proc = node.getID();

		frame = new JFrame("Process " + proc );
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation( x + 10, y + 10);
		frame.setAlwaysOnTop (true);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		frame.getContentPane().add(panel);

		size = displayString.length();

		// The size of the text area must be adapted here
		textArea = new JTextArea(displayString, 10, 15);
		textArea.setEditable(false);

		panel.add(textArea, BorderLayout.PAGE_END);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
    }


    public void display(String s) {

		displayString = s;
		int nextSize = displayString.length();
		textArea.replaceRange(displayString, 0, size);
		size = nextSize;
    }

    public void deleteFrame() {

		frame.dispose();
		return;
    }
}
