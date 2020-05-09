package io.oscript.hub.api.storage;

import io.oscript.hub.api.config.HubConfiguration;
import io.oscript.hub.api.exceptions.EntityNotFoundException;
import io.oscript.hub.api.exceptions.OperationFailedException;
import io.oscript.hub.api.utils.Constants;
import io.oscript.hub.api.utils.Naming;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class Storage {
    @Autowired
    IStoreProvider storeProvider;

    @Autowired
    HubConfiguration appConfiguration;

    private final Map<String, Channel> channels = new LinkedHashMap<>();

    @PostConstruct
    void initialize() throws IOException, OperationFailedException {
        initializeChannels();
    }

    // region Channels

    void initializeChannels() throws IOException, OperationFailedException {
        storeProvider.getChannels().forEach(channelInfo -> {
            Naming.checkChannelName(channelInfo.name);
            Channel channel = new Channel(channelInfo);
            channel.storeProvider = storeProvider;
            channels.put(channelInfo.name.toLowerCase(), channel);
        });

        if (channels.isEmpty()) {
            registrationChannel("stable");
        }
    }

    public Channel[] getChannels() {
        return channels.values().toArray(Channel[]::new);
    }

    public Channel getStableChannel() {
        return getChannel(Constants.STABLE);
    }

    public Channel getDevChannel() {
        return getChannel(Constants.DEVELOP);
    }

    public Channel getChannel(String name) {
        if (!channels.containsKey(name)) {
            throw EntityNotFoundException.channelNotFound(name);
        }

        Naming.checkChannelName(name);
        return channels.getOrDefault(name, null);
    }

    public Channel registrationChannel(String name) throws IOException {
        Naming.checkChannelName(name);
        Channel channel;
        if (channels.containsKey(name)) {
            channel = getChannel(name);
        } else {
            channel = new Channel(storeProvider.channelRegistration(name));
            channels.put(name.toLowerCase(), channel);
        }

        return channel;
    }

    // endregion

}
