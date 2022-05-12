package org.samurayrus.bot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.VoiceChannel;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ServerInfoLoaderService {
    private final long voiceChannelId;
    private final String targetIp;
    private final String targetUrl;
    private final String prefix;
    private final String reasonEditName;

    private final GatewayDiscordClient gateway;

    @Autowired
    public ServerInfoLoaderService(@Value("${app.voiceChannelId}") final long voiceChannelId,
                                   @Value("${app.token}") final String token,
                                   @Value("${app.targetIpPort}") final String targetIp,
                                   @Value("${app.targetUrl}") final String targetUrl,
                                   @Value("${app.prefix}") final String prefix,
                                   @Value("${app.reasonEditName}") final String reasonEditName) {
        this.voiceChannelId = voiceChannelId;
        this.targetIp = targetIp;
        this.targetUrl = targetUrl;
        this.prefix = prefix;
        this.reasonEditName = reasonEditName;
        this.gateway = DiscordClient.create(token).login().block();
    }

    @Scheduled(fixedDelayString = "${app.schedulerFixedDelay}")
    void scheduling() throws Exception {
        final String serverStatsInfo = loadServerStatsInfo();
        System.out.println("new: " + serverStatsInfo);

        final VoiceChannel voiceChannel =
                (VoiceChannel) gateway.getChannelById(Snowflake.of(voiceChannelId)).block();

        if (voiceChannel != null && !voiceChannel.getName().equals(serverStatsInfo))
            voiceChannel.edit(voiceChannelEditSpec -> voiceChannelEditSpec.setName(serverStatsInfo).setReason(reasonEditName)).block();
    }

    private String loadServerStatsInfo() {
        try {
            return loadServerInfosMapper().stream().filter(x -> x.getIdentifier().equals(targetIp)).findFirst().get().getOnlineStats(prefix);
        } catch (Exception e) {
            return prefix + "-/-";
        }
    }

    private List<ServerInfo> loadServerInfosMapper() throws Exception {
        return new ObjectMapper().readValue(loadResponseServerInfosJson(), new TypeReference<List<ServerInfo>>() {
        });
    }

    private String loadResponseServerInfosJson() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        final HttpUriRequest httpGet = new HttpGet(targetUrl);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        String responseServerInfoJson = EntityUtils.toString(response.getEntity());

        response.close();
        httpGet.abort();
        httpclient.close();
        return responseServerInfoJson;
    }
}
