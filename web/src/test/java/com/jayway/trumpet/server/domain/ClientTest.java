package com.jayway.trumpet.server.domain;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.Consumer;

public class ClientTest {


    @Test
    public void a_client_can_tell_if_in_range() {

        Consumer<Client> closeHandler = Mockito.mock(Consumer.class);

        Client sender = new Client("sender", Location.create(55.583985D, 12.957578D), closeHandler);  //38

        Client receiver = new Client("receiver", Location.create(55.584126D, 12.957406D), closeHandler); //32

        boolean b = sender.inRange(receiver, 500);

        System.out.println("In range " + b);
    }

}
