import sun.net.util.IPAddressUtil;
import java.io.File;

public class Main {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.print("Bad number of arguments.\n");
            return;
        }


        // Get and check parameters:
        /// IP
        if (!IPAddressUtil.isIPv4LiteralAddress(args[0])) {
            System.err.print("Bad IP.\n");
            return;
        }
        String ip = args[0];

        ///PORT
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.print("Bad port.\n");
            return;
        }

        /// FILE
        String media = args[2];
        File f = new File(media);
        if(!f.exists() || f.isDirectory()) {
            System.err.print("Bad File.\n");
            return;
        }

        StreamServerRTP s = new StreamServerRTP(ip, port, media);
        s.StartStream();
        return;
    }
}