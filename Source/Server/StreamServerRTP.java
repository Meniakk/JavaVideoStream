import sun.awt.Mutex;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.MediaEventAdapter;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class StreamServerRTP {

    public static final Object monitor = new Object();
    public static boolean finished = false;
    private String ip;
    private int port;
    private String media;

    public StreamServerRTP(String ip, int port, String media) {
        this.ip = ip;
        this.port = port;
        this.media = media;
    }

    public void StartStream() {

        // Create server and player.
        String options = formatRtpStream(ip, port);
        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(media);
        MediaPlayer mediaPlayer = mediaPlayerFactory.mediaPlayers().newMediaPlayer();

        // Set handlers for stream events.
        mediaPlayer.events().addMediaPlayerEventListener(
                new MediaPlayerEventAdapter() {
                    @Override
                    public void playing(MediaPlayer mediaPlayer) {
                        System.err.print("playing.\n");
                        super.playing(mediaPlayer);
                    }

                    @Override
                    public void finished(MediaPlayer mediaPlayer) {
                        System.err.print("Finished stream.\n");
                        finished = true;
                        synchronized (monitor) {
                            monitor.notify();
                        }
                        super.finished(mediaPlayer);
                    }

                    @Override
                    public void error(MediaPlayer mediaPlayer) {
                        System.err.print("Error in stream.\n");
                        finished = true;
                        synchronized (monitor) {
                            monitor.notify();
                        }
                        super.error(mediaPlayer);
                    }
                });
        // Stream media.
        System.err.print("Streaming \"" + media + "\" To: \"" +
                ip + ":" + String.valueOf(port) + "\".\n");
        System.err.print("Stream starting.\n");
        mediaPlayer.media().play(media,
                options,
                ":no-sout-rtp-sap",
                ":no-sout-standard-sap",
                ":sout-all",
                ":sout-keep"
        );

        // Wait for stream to finish and close server.
        synchronized(monitor) {
            while (!finished) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        mediaPlayer.release();
        System.err.print("Stream over.\n");
    }

    /**
     * Get server properties and format to RTP input.
     * @param serverAddress The ip.
     * @param serverPort The port.
     * @return String input for RTP server.
     */
    private static String formatRtpStream(String serverAddress, int serverPort) {
        StringBuilder sb = new StringBuilder(60);
        sb.append(":sout=#rtp{dst=");
        sb.append(serverAddress);
        sb.append(",port=");
        sb.append(serverPort);
        sb.append(",mux=ts}");
        return sb.toString();

    }
}
