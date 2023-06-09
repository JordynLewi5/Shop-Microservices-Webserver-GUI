import com.google.gson.Gson;
import com.hp.gagawa.java.elements.S;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class UsernamePasswordService {
    public static void main(String[] args) throws Exception {

        // server is listening on port 5050

        int port = 5052;

        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        ServerSocket ss = new ServerSocket(port);

        // running infinite loop for getting
        // client request

        System.out.println("Starting ProductInfo service at port = " + port);

        boolean result = register("localhost", 5000, "localhost", port); // Register with the Registry, so the client know how to find!

        if (!result) {
            System.out.println("Register unsuccessfully!");
            ss.close();
            return;
        }

        int nClients = 0;

        while (true)
        {
            Socket s = null;
            // socket object to receive incoming client requests
            s = ss.accept();
            nClients++;
            System.out.println("A new client is connected : " + s + " client number: " + nClients);
            serve(s, nClients);

        }
    }

    private static void serve(Socket socket, int clientID) throws Exception {

        DataInputStream reader = new DataInputStream(socket.getInputStream());
        String msg = reader.readUTF();
        // int id = reader.readInt();
        UsernamePasswordModel usernamePassword = new Gson().fromJson(msg, UsernamePasswordModel.class);

        Class.forName("org.sqlite.JDBC");
        DataAccess adapter = new SQLiteDataAdapter();
        adapter.connect("jdbc:sqlite:shop.db");
        UserModel model = adapter.loadUser(usernamePassword.username);

        Boolean ans = false;
        if (model != null) {
            if (model.password.equals(usernamePassword.password))
                ans = true;
            else
                ans = false;
        }
        DataOutputStream printer = new DataOutputStream(socket.getOutputStream());
        printer.writeUTF(new Gson().toJson(ans));
        printer.flush();
        printer.close();
        reader.close();
        socket.close();
    }
/*
    Register this service to the Registry!
 */
    private static boolean register(String regHost, int regPort, String myHost, int myPort) throws IOException {

        ServiceInfoModel info = new ServiceInfoModel();
        info.serviceCode = ServiceInfoModel.USERNAME_PASSWORD_VALID_SERVICE;
        info.serviceHostAddress = myHost;
        info.serviceHostPort = myPort;

        Gson gson = new Gson();

        ServiceMessageModel req = new ServiceMessageModel();
        req.code = ServiceMessageModel.SERVICE_PUBLISH_REQUEST;
        req.data = gson.toJson(info);

        Socket socket = new Socket(regHost, regPort);

        DataOutputStream printer = new DataOutputStream(socket.getOutputStream());
        printer.writeUTF(gson.toJson(req));
        printer.flush();

        DataInputStream reader = new DataInputStream(socket.getInputStream());
        String msg = reader.readUTF();
        printer.close();
        reader.close();
        socket.close();

        System.out.println("Message from server: " + msg);
        ServiceMessageModel res = gson.fromJson(msg, ServiceMessageModel.class);




        return res.code == ServiceMessageModel.SERVICE_PUBLISH_OK;
    }

    private static void deregister() {

    }
}