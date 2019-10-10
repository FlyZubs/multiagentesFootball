package team.legacy.A1;

import java.io.IOException;
import java.net.InetAddress;

public class MainA1 {

	public static void main(String[] args) {
		int numPlayers = 2; 
		
		try {			
			launchServer();
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for (int i = 0; i < numPlayers; i++) {
				A1 a1 = new A1(InetAddress.getByName("localhost"), 6000, "A1");
				a1.run();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void launchServer() {
		try {
			System.out.println(" >> Iniciando servidor...");
			
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("cmd /c tools\\startServer.cmd");
			p.waitFor();

        } catch(Exception e) {
        	e.printStackTrace();
        	System.err.println("N�o pode iniciar o servidor!");
        	return;
        }
	}

}
