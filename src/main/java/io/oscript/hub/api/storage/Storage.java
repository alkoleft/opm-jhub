package io.oscript.hub.api.storage;

import io.oscript.hub.api.config.HubConfiguration;
import io.oscript.hub.api.utils.Constants;
import io.oscript.hub.api.utils.Naming;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.InvalidParameterException;
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
    void initialize() throws Exception {
        initializeChannels();
    }

    // region Channels

    void initializeChannels() throws Exception {
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
        if (!Naming.isInvalid(name))
            throw new InvalidParameterException("Не корректное имя канала");
        return channels.getOrDefault(name, null);
    }

    public Channel registrationChannel(String name) throws IOException {
        if (!Naming.isInvalid(name))
            throw new InvalidParameterException("Не корректное имя канала");
        Channel channel;
        if ((channel = getChannel(name)) == null) {
            channels.put(name.toLowerCase(), channel = new Channel(storeProvider.channelRegistration(name)));
        }

        return channel;
    }

    // endregion

}
