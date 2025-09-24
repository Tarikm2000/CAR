import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {
    private static final String LOGIN = "miage";
    private static final String PASSWORD = "car";

    private static ServerSocket dataserver;

    public static void main(String[] args) {
        try {
            ServerSocket serv = new ServerSocket(1025);
            Socket s2 = serv.accept();

            OutputStream out = s2.getOutputStream();
            String str = "220 Service ready\r\n";
            out.write(str.getBytes());

            InputStream in = s2.getInputStream();
            Scanner scan = new Scanner(in);

            boolean isAuthenticated = false;
            String receivedLogin = null;
            boolean run = true;
            File rootDirectory = new File(".").getCanonicalFile();
            File currentDirectory = rootDirectory;


            while (run) {
                String command;
                try {
                    command = scan.nextLine();
                } catch (Exception e) {
                    break;
                }
                System.out.println("Commande reçue: " + command);

                if (!isAuthenticated) {
                    if (command.startsWith("USER ")) {
                        receivedLogin = command.substring(5).trim();
                        out.write("331 Password required\r\n".getBytes());
                    } else if (command.startsWith("PASS ")) {
                        String receivedPassword = command.substring(5).trim();
                        if (LOGIN.equals(receivedLogin) && PASSWORD.equals(receivedPassword)) {
                            isAuthenticated = true;
                            out.write("230 User logged in, proceed\r\n".getBytes());
                        } else {
                            out.write("530 Login incorrect\r\n".getBytes());
                        }
                    } else {
                        out.write("530 Please login with USER and PASS\r\n".getBytes());
                    }
                } else {
                    if (command.equalsIgnoreCase("QUIT")) {
                        out.write("221 Service closing control connection\r\n".getBytes());
                        s2.close();
                        serv.close();
                        run = false;
                    }
                    else if (command.equalsIgnoreCase("EPSV")) {
                        try {
                            dataserver = new ServerSocket(0);
                            int dataPort = dataserver.getLocalPort();
                            String epsvResponse = "229 Entering Extended Passive Mode (|||" 
                                                  + dataPort + "|)\r\n";
                            out.write(epsvResponse.getBytes());
                        } catch (IOException e) {
                            out.write("425 Can't open data connection\r\n".getBytes());
                        }
                    }
                    else if (command.toUpperCase().startsWith("CWD ")) {
                        String requestedPath = command.substring(4).trim();
                        File newDir = new File(currentDirectory, requestedPath);
                        try {
                            newDir = newDir.getCanonicalFile();
                            if (!newDir.exists() || !newDir.isDirectory()) {
                                out.write("550 Failed to change directory.\r\n".getBytes());
                            } else if (!newDir.getPath().startsWith(rootDirectory.getPath())) {
                                out.write("550 Access denied.\r\n".getBytes());
                            } else {
                                currentDirectory = newDir;
                                out.write(("250 Directory changed to "
                                    + currentDirectory.getAbsolutePath() + "\r\n").getBytes());
                            }
                        } catch (IOException e) {
                            out.write("550 Failed to change directory.\r\n".getBytes());
                        }
                    }


                    else if (command.startsWith("RETR ")) { 
                        String fileName = command.substring(5).trim();
                        File file = new File(fileName);

                        if (!file.exists()) {
                            out.write("550 File not found\r\n".getBytes());
                            continue;
                        }
                        if (dataserver == null || dataserver.isClosed()) {
                            out.write("425 Use EPSV first\r\n".getBytes());
                            continue;
                        }

                        out.write("150 Opening data connection\r\n".getBytes());

                        try (
                            Socket dataSocket = dataserver.accept();
                            FileInputStream fileInput = new FileInputStream(file);
                            BufferedInputStream bis = new BufferedInputStream(fileInput);
                            OutputStream dataOut = dataSocket.getOutputStream()
                        ) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = bis.read(buffer)) != -1) {
                                dataOut.write(buffer, 0, bytesRead);
                            }
                            dataOut.flush();

                            out.write("226 Transfer complete\r\n".getBytes());
                        } catch (IOException e) {
                            out.write("426 Connection closed; transfer aborted\r\n".getBytes());
                        } finally {
                            dataserver.close();
                            dataserver = null;
                        }
                    }
                    else if (command.toUpperCase().startsWith("LIST")) {                   
                            if (dataserver == null || dataserver.isClosed()) {
                            out.write("425 Use EPSV first\r\n".getBytes());
                            continue;
                        }

                        String path = ".";
                        if (command.trim().length() > 4) {
                            path = command.substring(4).trim();
                        }
                        File dir = new File(path);
                        if (!dir.exists() || !dir.isDirectory()) {
                            out.write("550 Directory not found\r\n".getBytes());
                            continue;
                        }
                        
                        out.write("150 Opening data connection for file list\r\n".getBytes());

                        try (
                            Socket dataSocket = dataserver.accept();
                            OutputStream dataOut = dataSocket.getOutputStream();
                            PrintWriter writer = new PrintWriter(new OutputStreamWriter(dataOut, "UTF-8"), true)
                        ) {
   
                            File[] files = dir.listFiles();
                            if (files != null) {
                                for (File file : files) {
                                    writer.println(file.getName());
                                }
                            }
                            writer.flush();
                            out.write("226 List transfer done\r\n".getBytes());
                        } catch (IOException e) {   
                            out.write("426 Connection closed; transfer aborted\r\n".getBytes());
                        } finally {
                            dataserver.close();
                            dataserver = null;
                        }
                    }
                    else {
                        out.write("502 Command not implemented\r\n".getBytes());
                    }
                }
            }

            scan.close();
        }
        catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
