import sun.net.util.IPAddressUtil;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.BorderLayout;

import java.awt.event.*;


public class StreamReader {


    private static StreamReader thisApp;
    private final JFrame frame;
    private JPanel jPanel = new JPanel();
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;


    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.print("Bad number of arguments.\n");
            return;
        }

        // Check IP
        if (!IPAddressUtil.isIPv4LiteralAddress(args[0])) {
            System.err.print("Bad IP.\n");
            return;
        }

        // Get parameters
        String ip = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.print("Bad port.\n");
            return;
        }

        thisApp = new StreamReader(ip, port);
    }

    /**
     * Read from stream and show frame.
     */
    public StreamReader(String ip, int port) {
        System.err.print("Reading from \"" + ip + ":" + String.valueOf(port)
                + "\":\n");

        jPanel.setLayout(new BorderLayout());
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                System.err.print("playing.");
                super.playing(mediaPlayer);
            }
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                System.err.print("Stream over.\n");
                mediaPlayerComponent.release();
                System.exit(0);
            }
            @Override
            public void error(MediaPlayer mediaPlayer) {
                System.err.print("Stream over.\n");
                mediaPlayerComponent.release();
                System.exit(-1);
            }
        };
        jPanel.add(mediaPlayerComponent, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        JButton pauseButton = new JButton("Pause");
        controls.add(pauseButton);

        /* Set the pause/play button action and text. */
        pauseButton.addActionListener(e -> {
            mediaPlayerComponent.mediaPlayer().controls().pause();
            pauseButton.setText(pauseButton.getText().equals("Pause") ?
                    "Play":"Pause");
        });
        jPanel.add(controls, BorderLayout.SOUTH);

        // Set frame.
        frame = new JFrame("Stream Reader");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                System.exit(0);
            }
        });
        frame.setContentPane(jPanel);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
            }
        });

        // Set Media and show frame.
        String url_s = "rtp://" + ip + ":" + port;
        frame.setVisible(true);
        mediaPlayerComponent.mediaPlayer().media().play(url_s);

    }
}
