package test1;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by noodles on 2018/3/30 22:19.
 */
public class TestDelayLogSocketServer {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(9000);

        while (true) {
            final Socket sock = serverSocket.accept();

            System.out.println("accept one client");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("run thread...");
                    try {
                        final OutputStream outputStream = sock.getOutputStream();

                        String[] data = new String[]{"aa,1,0","ee,1,39", "bb,1,41", "ccc,1,44"};

                        int i = 0;
                        while (true) {

                            int index = i++ % data.length;


                            final String[] split = data[index].split(",");
                            final long deley = Long.parseLong(split[2]) * 1000;


                            final String line = split[0] + " " + split[1] + " " + (System.currentTimeMillis() - deley);

                            System.out.println("real:" +
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                                    + " delay:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis() - deley)));

                            outputStream.write((line + "\n").getBytes());

                            Thread.sleep(500);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


    }
}
