package net.pistonmaster.encryptedchat.server;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectionState {
    private State state;
    private Object bus;

    public enum State {
        LOGIN,
        CONNECTED
    }
}
