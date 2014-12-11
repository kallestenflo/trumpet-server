package com.jayway.trumpet.server;

import com.jayway.fixture.ServerRunningRule;
import com.jayway.fixture.TrumpetClient;
import com.jayway.fixture.TrumpetClientException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.fixture.ServerRunningRule.local;
import static com.jayway.fixture.ThrowableExpecter.expect;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

public class TrumpetIntegrationTest {

    @ClassRule
    public static ServerRunningRule server = local();
    //public static ServerRunningRule server = remote("elefant-server.herokuapp.com", 80);

    private static final String MESSAGE = "Ho ho";

    @Before public void
    clear_trumpeteer_repository_before_each_test() {
        server.trumpeteerRepository().clear();
    }

    @Test
    public void a_trumpeteer_receives_messages_when_in_range() {
        TrumpetClient sender = createClient().connect(55.583985D, 12.957578D);
        TrumpetClient inRange1 = createClient().connect(55.584126D, 12.957406D);
        TrumpetClient inRange2 = createClient().connect(55.584126D, 12.957406D);
        TrumpetClient outOfRange1 = createClient().connect(55.581212D, 12.959208D);

        sender.trumpet(MESSAGE);

        await().until(() -> assertThat(inRange1.trumpetMessages()).extracting("message").contains(MESSAGE).hasSize(1)); // One trumpet
        await().until(() -> assertThat(inRange1.inRangeMessages()).extracting("trumpeteersInRange").contains(2).hasSize(2)); //two notification trumpetMessages

        await().until(() -> assertThat(inRange2.trumpetMessages()).extracting("message").contains(MESSAGE).hasSize(1)); // One trumpet and one notification message
        await().until(() -> assertThat(inRange2.inRangeMessages()).extracting("trumpeteersInRange").contains(2).hasSize(1)); // One notification message

        //await().until(() -> assertThat(outOfRange1.trumpetMessages().size()).isEqualTo(1)); // Notification message is sent to self when subscribing
    }

    @Test
    public void a_trumpet_message_can_not_be_null_or_empty() {
        TrumpetClient sender = createClient().connect(55.583985D, 12.957578D);

        TrumpetClientException exception = expect(TrumpetClientException.class).when(() -> sender.trumpet(""));

        assertThat(exception.response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void number_of_trumpeteers_in_range_is_returned_when_location_is_updated() {
        TrumpetClient trumpeteer = createClient().connect(55.583985D, 12.957578D);

        long inRangeBefore = trumpeteer.countTrumpeteersInRange();

        createClient().connect(55.583985D, 12.957578D);

        long inRangeAfter = trumpeteer.countTrumpeteersInRange();

        assertThat(inRangeAfter - inRangeBefore).isEqualTo(1);
    }

    @Test
    public void trumpeteers_in_range_can_be_counted() {
        TrumpetClient one = createClient().connect(55.583985D, 12.957578D);

        int adjacentTrumpeteersBefore = one.countTrumpeteersInRange();

        createClient().connect(55.583985D, 12.957578D);

        int adjacentTrumpeteersAfter = one.countTrumpeteersInRange();

        assertThat(adjacentTrumpeteersAfter).isEqualTo(adjacentTrumpeteersBefore + 1);
    }

    @Test
    public void trumpeteers_can_be_deleted() {
        TrumpetClient one = createClient().connect(55.583985D, 12.957578D);
        TrumpetClient two = createClient().connect(55.583985D, 12.957578D);
        assertThat(two.countTrumpeteersInRange()).isEqualTo(1);

        one.delete();

        assertThat(two.countTrumpeteersInRange()).isEqualTo(0);
    }

    @Ignore
    @Test
    public void subscribers_are_closed_by_server() throws Exception {
        TrumpetClient one = createClient().connect(55.583985D, 12.957578D);
        one.diconnect();

        Thread.sleep(4000);

        TrumpetClient two = createClient().connect(55.583985D, 12.957578D);
        TrumpetClient three = createClient().connect(55.583985D, 12.957578D);

        int inRange = two.countTrumpeteersInRange();

        assertThat(inRange).isEqualTo(1);
    }


    @Test
    public void ext_parameters_are_included_in_trumpet() {
        TrumpetClient sender = createClient().connect(55.583985D, 12.957578D);

        sender.trumpet("WITH EXT", Collections.singletonMap("ext.one-key", "one-val"));

        await().until(() -> !sender.trumpetMessages().isEmpty());

        Map<String, String> ext = sender.lastMessage().getExt();

        assertThat(ext).containsEntry("one-key", "one-val");
    }

    @Test
    public void trumpets_can_not_exceed_max_length() {

        String max_message = createString('A', 320);
        String max_exceeded = createString('A', 321);

        TrumpetClient sender = createClient().connect(55.583985D, 12.957578D);

        sender.trumpet(max_message);

        await().until(() -> !sender.trumpetMessages().isEmpty());

        assertThat(sender.trumpetMessages()).extracting("message").contains(max_message);

        TrumpetClientException exception = expect(TrumpetClientException.class).when(() -> sender.trumpet(max_exceeded));

        assertThat(exception.response.getStatus()).isEqualTo(400);
    }

    @Test
    public void trumpeteers_receive_max_message_length_when_created() {

        TrumpetClient sender = createClient().connect(55.583985D, 12.957578D);

        assertThat(sender.trumpeteer()).containsEntry("maxMessageLength", 320);
    }

    public static String createString(char character, int length) {
        char[] chars = new char[length];
        Arrays.fill(chars, character);
        return new String(chars);
    }

    private TrumpetClient createClient(){
        return TrumpetClient.create(server.host(), server.port());
    }
}
