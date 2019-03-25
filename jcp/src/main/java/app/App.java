/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class App {
    //jcp main
    public static void main(String[] args) {
        int test  = 0;


        System.out.println(NetworkUtils.timeStamp(1) + "JCP online");
        //make a discoverymanager and start it, prints results to file
        DiscoveryManager DM = new DiscoveryManager(Module.JCP);
        DM.start();

        //get the stalkers from file
        HashMap<Integer, String> m =  NetworkUtils.mapFromJson(NetworkUtils.fileToString("config/stalkers.list"));
        //get sorted list from targets
        List<Integer> s_list = NetworkUtils.mapToSList(m);

        System.out.println(" Ip ids" + (s_list));
//        if (test == 0){
//            return;
//        }
        for (Integer key : m.keySet()){

        }
        RequestSender requestSender = RequestSender.getInstance();
        //ip of stalker we'll just use the one at index 0 for now
        String i =  m.get(s_list.get(0));
        System.out.println(" dwdwdwdwddwwd" + i.toString());
        String stalkerip =  m.get(s_list.get(1));

        //port to connect to
        int port = 11111;
        Socket socket = requestSender.connect(stalkerip, port);
        String req = "upload";
        switch (req){
            case("upload"):
                requestSender.sendFile("temp\\003_txt_test.txt");
                break;
            case("download"):
                requestSender.getFile("temp\\003_txt_test.txt");
                break;
        }
        // should close socket from main calling method, otherwise threads giving null pointer exception
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//
//    //load a config (stalker ip) from file while we get network discovery working

}
