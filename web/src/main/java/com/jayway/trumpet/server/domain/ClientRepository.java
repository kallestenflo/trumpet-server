package com.jayway.trumpet.server.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ClientRepository {

    private static final Logger logger = LoggerFactory.getLogger(ClientRepository.class);

    private Map<String, Client> clients = new ConcurrentHashMap<>();

    private AtomicLong idGenerator = new AtomicLong();

    public Client createClient(Double latitude,
                               Double longitude){

        Client client = new Client(Long.toString(idGenerator.incrementAndGet()), Location.create(latitude, longitude), this::delete);
        clients.put(client.id, client);
        logger.debug("Client created: {}", client.id);
        return client;
    }

    public Optional<Client> getById(String id){
        return Optional.ofNullable(clients.get(id));
    }

    public void delete(Client client){
        if(clients.containsKey(client.id)){
            clients.remove(client.id);
        }
    }

    public Stream<Client> clientsInRangeOf(Client trumpeter, int maxDistance){

        return clients.values().stream().filter(c -> c.inRangeWithOutput(trumpeter, maxDistance));
    }

}
