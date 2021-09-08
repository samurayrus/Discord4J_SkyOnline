package org.samurayrus.bot;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
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
