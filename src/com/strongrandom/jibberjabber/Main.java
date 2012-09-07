package com.strongrandom.jibberjabber;

import com.strongrandom.jibberjabber.thirdparty.SystemCommandExecutor;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: strongrandom
 * Date: 3/4/12
 * Time: 3:04 PM
 * <p/>
 * This is the main Jabber loop and entry point.
 */
public class Main {
    private static Logger logger = Logger.getLogger(Main.class.getName());
    private static XMPPConnection connection;


    public static void main(String[] args) {
        int offlineDelay = 0;

        while (true) {
            try {
                if (offlineDelay > 0) {
                    logger.info(String.format("Pausing %s ms", offlineDelay));
                    Thread.sleep(offlineDelay);
                }

                ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(
                        Config.getInstance().getString("xmpp.server"),
                        Config.getInstance().getInteger("xmpp.port"),
                        Config.getInstance().getString("xmpp.domain"));

                connectionConfiguration.setSASLAuthenticationEnabled(true);
                connection = new XMPPConnection(connectionConfiguration);
                connection.connect();
                connection.login(Config.getInstance().getString("xmpp.username"),
                        Config.getInstance().getString("xmpp.password"));

                Recv();

                Presence presence = new Presence(Presence.Type.available);
                connection.sendPacket(presence);

                int seconds = 0;
                while (true) {
                    if (seconds % Config.getInstance().getInteger("status.poll_seconds", 30) == 0) {
                        if (new File(Config.getInstance().getString("status.file")).exists()) {
                            presence.setStatus(readText(Config.getInstance().getString("status.file")));
                        }

                        offlineDelay = 600;

                        connection.sendPacket(presence);
                    }

                    Thread.sleep(1000);
                    seconds++;
                }
            } catch (InterruptedException e) {
                try {
                    connection.disconnect();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }

                return;
            } catch (Exception e) {
                try {
                    connection.disconnect();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }

                e.printStackTrace();

                if (offlineDelay < 600000) offlineDelay += 30000;
            }
        }
    }

    private static String paranoiaRegex(String s) {
        String regex = Config.getInstance().getString("regex.paranoia", null);

        if (regex != null)
            return s.replaceAll(regex, "");
        else
            return s;
    }

    private static String scriptRegex(String s) {
        String regex = Config.getInstance().getString("regex.script", null);

        if (regex != null)
            return s.replaceAll(regex, "");
        else
            return s;
    }

    private static void Recv() {
        PacketFilter filter = new PacketTypeFilter(Message.class);

        PacketListener myListener = new PacketListener() {
            public void processPacket(Packet packet) {
                String body = ((Message) packet).getBody();

                if (body != null) {
                    // Strip all non-ASCII
                    body = body.replaceAll("[^\u0020-\u007F]", "");
                    String paranoid = paranoiaRegex(body);
                    String[] split = paranoid.split(" ");

                    String script = String.format("%s%s%s%s",
                            Config.getInstance().getString("script.path"),
                            File.separator,
                            scriptRegex(split[0].toLowerCase()),
                            Config.getInstance().getString("script.extension", ""));

                    logger.info(String.format("RECV (%s): %s [%s] [%s]", packet.getFrom(), body, paranoid, script));

                    File f = new File(script);

                    if (f.exists()) {
                        try {
                            List<String> commands = new ArrayList<String>();
                            commands.add(script);
                            commands.addAll(Arrays.asList(split).subList(1, split.length));

                            logger.info("EXEC: " + script);

                            SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
                            int result = commandExecutor.executeCommand();

                            StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
                            StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

                            logger.info("RESULT: " + Integer.toString(result));
                            logger.info("STDOUT: " + stdout);
                            logger.info("STDERR: " + stderr);

                            String response = stdout.toString().trim() + stderr.toString().trim();

                            if (response.length() == 0)
                                response = "[OK]";

                            Message message = new Message(packet.getFrom(), Message.Type.chat);
                            message.setBody(response);
                            connection.sendPacket(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {


                        Message message = new Message(packet.getFrom(), Message.Type.chat);
                        message.setBody("[OK] " + body);
                        connection.sendPacket(message);
                    }
                }
            }
        };

        // Register the listener.
        connection.addPacketListener(myListener, filter);
    }

    private static String readText(String filename) {

        StringBuilder text = new StringBuilder();
        Scanner scanner = null;

        try {
            scanner = new Scanner(new FileInputStream(filename));
            while (scanner.hasNextLine()) {
                text.append(scanner.nextLine());
            }
        } catch (Exception ignored) {
            logger.warning(ignored.toString());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return text.toString();
    }
}
