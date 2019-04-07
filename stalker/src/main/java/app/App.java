/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package app;

import app.LeaderUtils.CRUDQueue;
import app.LeaderUtils.RequestAdministrator;
import app.chunk_utils.Indexer;
import app.chunk_utils.IndexFile;
import org.apache.commons.io.FilenameUtils;
import java.io.*;
import java.util.List;

public class App {

    private static int leaderUuid = -1;

    public int getLeaderUuid()
    {
        return leaderUuid;
    }

    public static void main(String[] args) {

        int discoveryinterval = 15;
        //starting listener thread for health check and leader election
        Thread listenerForHealth = new Thread( new ListenerThread());
        listenerForHealth.start();

        //First thing to do is locate all other stalkers and print the stalkers to file
        //check the netDiscovery class to see where the file is being created
        Thread discManager = new Thread(new DiscoveryManager(Module.STALKER, discoveryinterval, false));
       // DiscoveryManager DM = new DiscoveryManager(Module.STALKER);
        discManager.start();
        //we will wait for network discovery to do its thing
        System.out.println(NetworkUtils.timeStamp(1) + "Waiting for system discovery...");
        try{
            Thread.sleep((long)((discoveryinterval * 1000) + 5000));
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println(NetworkUtils.timeStamp(1) + "System discovery complete!");

        System.out.println("This Stalker's macID" + NetworkUtils.getMacID());
        int test = 0;
        initStalker();
        Indexer.loadFromFile();
        //ind.summary();
        System.out.println(NetworkUtils.timeStamp(1) + "Stalker Online");

        //starting task for health checks on STALKERS and HARM targets
        Thread healthChecker = new Thread(new HealthChecker(Module.STALKER, null, false));
        healthChecker.start();
        String stalkerList = NetworkUtils.fileToString("config/stalkers.list");
        String harmlist = NetworkUtils.fileToString("config/harm.list");

        // initiaze ids


        //election based on networkDiscovery
        while (true){
            // Leader election by asking for a leader
            List<Integer> ids = NetworkUtils.mapToSList(NetworkUtils.mapFromJson(stalkerList));
            LeaderCheck leaderchecker = new LeaderCheck();
            leaderchecker.election();
            leaderUuid = LeaderCheck.getLeaderUuid();

            int role = ElectionUtils.identifyRole(ids,leaderUuid);
            switch (role){
                case 0:
                    //This means that this STK is the leader
                    //create a priority comparator for the Priority queue
                    CRUDQueue syncQueue = new CRUDQueue();
                    Thread t1 = new Thread(new StalkerRequestHandler(syncQueue));
                    Thread t2 =  new Thread(new RequestAdministrator(syncQueue));
                    t1.start();
                    t2.start();

                    try{
                        t1.join();
                        t2.join();
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    Thread jcpReq = new Thread(new JcpRequestHandler());
//                JcpRequestHandler jcpRequestHandler = new JcpRequestHandler(ind);
//                jcpRequestHandler.run();
                    jcpReq.start();
                    try {
                        jcpReq.join();
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    Thread vice = new Thread(new JcpRequestHandler());
//                JcpRequestHandler jcpRequestHandler = new JcpRequestHandler(ind);
//                jcpRequestHandler.run();
                    vice.start();
                    try {
                        vice.join();
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    break;
            }
        }

    }



    //cleans chunk folders on startup
    public static void initStalker(){
        //clear chunk folder
        File chunk_folder = new File("temp/chunks/");

        File[] chunk_folder_contents = chunk_folder.listFiles();
        File temp_folder = new File("temp/toChunk/");
        File[] temp_folder_contents = temp_folder.listFiles();

        if(chunk_folder_contents != null) {
            for (File f : chunk_folder_contents) {
                if (!FilenameUtils.getExtension(f.getName()).equals("empty")) {
                    f.delete();
                }
            }
        }
        if(temp_folder_contents != null) {
            for (File f : temp_folder_contents) {
                if (!FilenameUtils.getExtension(f.getName()).equals("empty")) {
                    f.delete();
                }
            }
        }

    }


}

