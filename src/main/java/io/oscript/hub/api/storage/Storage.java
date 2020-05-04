package io.oscript.hub.api.storage;

import io.oscript.hub.api.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class Storage {
    @Autowired
    IStoreProvider storeProvider;

    Map<String, Channel> channels = new LinkedHashMap<>();

    @PostConstruct
    void initialize() {
        initializeChannels();
    }

    // region Channels

    void initializeChannels() {
        storeProvider.getChannels().forEach(channelInfo -> {
            Channel channel = new Channel(channelInfo);
            channel.storeProvider = storeProvider;
            channels.put(channelInfo.name.toLowerCase(), channel);
        });
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
        return channels.getOrDefault(name, null);
    }

    public Channel registrationChannel(String name) {
        Channel channel;
        if ((channel = getChannel(name)) == null) {
            channels.put(name.toLowerCase(), channel = new Channel(storeProvider.channelRegistration(name)));
        }

        return channel;
    }

    // endregion

}
