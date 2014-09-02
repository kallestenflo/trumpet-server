package com.jayway.trumpet.server.domain;

import com.jayway.trumpet.server.boot.TrumpetDomainConfig;
import com.jayway.trumpet.server.boot.TrumpetServerConfig;
import org.aeonbits.owner.ConfigFactory;
import org.glassfish.jersey.media.sse.EventOutput;
import org.junit.Test;

import java.util.Properties;

import static com.jayway.trumpet.server.boot.TrumpetDomainConfig.TRUMPETER_PURGE_INTERVAL;
import static com.jayway.trumpet.server.boot.TrumpetDomainConfig.TRUMPETER_STALE_THRESHOLD;
import static org.assertj.core.api.Assertions.assertThat;

public class TrumpeterTest {




    @Test
    public void a_trumpeter_becomes_stale() throws Exception {
        TrumpeterRepository repository = createTrumpeterRepository();

        Trumpeter trumpeter = repository.createTrumpeter(0D, 0D);

        assertThat(trumpeter.isStale(10)).isFalse();

        Thread.sleep(20);

        assertThat(trumpeter.isStale(10)).isTrue();
    }

    private TrumpeterRepository createTrumpeterRepository(Properties... props) {
        TrumpetDomainConfig config = ConfigFactory.create(TrumpetDomainConfig.class, props);

        return new TrumpeterRepository(config);
    }

    @Test
    public void a_trumpeter_is_removed_from_repo_when_closed() {

        Properties props = new Properties();
        props.setProperty(TRUMPETER_STALE_THRESHOLD, "1000000");
        props.setProperty(TRUMPETER_PURGE_INTERVAL, "1000000");

        TrumpeterRepository repository = createTrumpeterRepository(props);

        Trumpeter trumpeter = repository.createTrumpeter(0D, 0D);

        assertThat(repository.findById(trumpeter.id).isPresent()).isTrue();

        trumpeter.close();

        assertThat(repository.findById(trumpeter.id).isPresent()).isFalse();
    }

    @Test
    public void a_trumpeter_is_removed_from_repo_when_output_closed() throws Exception {

        Properties props = new Properties();
        props.setProperty(TRUMPETER_STALE_THRESHOLD, "100000");
        props.setProperty(TRUMPETER_PURGE_INTERVAL, "100000");

        TrumpeterRepository repository = createTrumpeterRepository(props);

        Trumpeter trumpeter = repository.createTrumpeter(0D, 0D);

        EventOutput output = trumpeter.subscribe();

        assertThat(repository.findById(trumpeter.id).isPresent()).isTrue();

        output.close();

        assertThat(repository.findById(trumpeter.id).isPresent()).isFalse();
    }

    @Test
    public void stale_trumpeters_are_purged() throws Exception {

        Properties props = new Properties();
        props.setProperty(TRUMPETER_STALE_THRESHOLD, "1");
        props.setProperty(TRUMPETER_PURGE_INTERVAL, "10");

        TrumpeterRepository repository = createTrumpeterRepository(props);

        Trumpeter trumpeter = repository.createTrumpeter(0D, 0D);

        assertThat(repository.findById(trumpeter.id).isPresent()).isTrue();

        Thread.sleep(300);

        assertThat(repository.findById(trumpeter.id).isPresent()).isFalse();
    }

}
