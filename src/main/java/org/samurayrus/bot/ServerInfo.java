package org.samurayrus.bot;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE)
public class ServerInfo {
    String name;

    int online;

    int maxPlayers;

    String ip;

    int port;

    public String getIdentifier() {
        return ip + ":" + port;
    }

    public String getOnlineStats(final String prefix) {
        return prefix + online + "/" + maxPlayers;
    }
}
