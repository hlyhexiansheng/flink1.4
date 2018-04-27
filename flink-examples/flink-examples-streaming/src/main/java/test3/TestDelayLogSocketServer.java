package test3;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

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
					System.out.println("run thread....");
					try {
						final OutputStream outputStream = sock.getOutputStream();

						String[] data = new String[]{"aa,1,0"};

						int i = 0;
						while (true) {

							Scanner scanner = new Scanner(System.in);
							final String nextLine = scanner.nextLine();

							final long delay = toInt(nextLine) * 1000;

							final String line = "aa " + i++ + " " + (System.currentTimeMillis() - delay);

							System.out.println(line + "  ,real:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " delay:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis() - delay)));

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

	private static int toInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Throwable e) {
			return 0;
		}
	}
}
