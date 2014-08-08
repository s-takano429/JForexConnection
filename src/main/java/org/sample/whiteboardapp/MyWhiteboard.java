/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sample.whiteboardapp;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author nb
 */
@ServerEndpoint(value = "/whiteboardendpoint",
        encoders = {FigureEncoder.class},
        decoders = {FigureDecoder.class})
public class MyWhiteboard {

    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());

    @OnOpen
    public void onOpen(Session peer) {
        peers.add(peer);
//                Timer timer1 = new Timer();
//        timer1.schedule(new Task1(), 1000, 1000);

    }

    @OnClose
    public void onClose(Session peer) {
        peers.remove(peer);
    }

    @OnMessage
    public void broadcastFigure(Figure figure, Session session) throws IOException, EncodeException {
        System.out.println("broadcastFigure: " + figure);
        for (Session peer : peers) {
            if (!peer.equals(session)) {
                peer.getBasicRemote().sendObject(figure);
            }
        }

    }

    @OnMessage
    public void broadcastSnapshot(ByteBuffer data, Session session) throws IOException {
        System.out.println("broadcastBinary: " + data);
        for (Session peer : peers) {
            if (!peer.equals(session)) {
                peer.getBasicRemote().sendBinary(data);
            }
        }
    }

    public void sendFigure() throws IOException, EncodeException {
        System.out.println("sendFigure: ");
        Random rnd = new Random();
        int ranX = rnd.nextInt(200);
        int ranY = rnd.nextInt(200);

        String str = "{\"shape\":\"circle\",\"color\":\"#FF0000\",\"coords\":{\"x\":" + ranX + ",\"y\":" + ranY + ".000}}";
        JsonObject jsonObject = Json.createReader(new StringReader(str)).readObject();
        Figure f = new Figure(jsonObject);

        for (Session peer : peers) {
            peer.getAsyncRemote().sendObject(f);
        }
    }

    class Task1 extends TimerTask {

        @Override
        public void run() {
            try {
                System.out.println("test timer");
                sendFigure();
            } catch (IOException ex) {
                Logger.getLogger(MyWhiteboard.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EncodeException ex) {
                Logger.getLogger(MyWhiteboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
