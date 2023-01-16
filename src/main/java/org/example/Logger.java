package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {

    static boolean  init = false;
    public static void log(String message) {
        System.out.println(message);
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter("output.txt", init), true);

            out.write(message+"\n");
            out.close();
        } catch (IOException e) {
            System.out.println("logger exception");
        }
        init = true;

    }
}
