package com.jayway.trumpet.server.domain;

public class TrumpeteerTest {




    /*@Test
    public void a_trumpeteer_becomes_stale() throws Exception {
        TrumpeteerRepository repository = createTrumpeteerRepository();

        Trumpeteer trumpeteer = repository.createTrumpeteer(0D, 0D);

        assertThat(trumpeteer.isStale(10)).isFalse();

        Thread.sleep(20);

        assertThat(trumpeteer.isStale(10)).isTrue();
    }

    private TrumpeteerRepository createTrumpeteerRepository(Properties... props) {
        TrumpetDomainConfig config = ConfigFactory.create(TrumpetDomainConfig.class, props);

        return new TrumpeteerRepository(config);
    }

    @Test
    public void a_trumpeteer_is_removed_from_repo_when_closed() {

        Properties props = new Properties();
        props.setProperty(TRUMPETEER_STALE_THRESHOLD, "1000000");
        props.setProperty(TRUMPETEER_PURGE_INTERVAL, "1000000");

        TrumpeteerRepository repository = createTrumpeteerRepository(props);

        Trumpeteer trumpeteer = repository.createTrumpeteer(0D, 0D);

        assertThat(repository.findById(trumpeteer.id).isPresent()).isTrue();

        trumpeteer.close();

        assertThat(repository.findById(trumpeteer.id).isPresent()).isFalse();
    }

    @Test
    public void a_trumpeteer_is_removed_from_repo_when_output_closed() throws Exception {

        Properties props = new Properties();
        props.setProperty(TRUMPETEER_STALE_THRESHOLD, "100000");
        props.setProperty(TRUMPETEER_PURGE_INTERVAL, "100000");

        TrumpeteerRepository repository = createTrumpeteerRepository(props);

        Trumpeteer trumpeteer = repository.createTrumpeteer(0D, 0D);

        EventOutput output = trumpeteer.subscribe();

        assertThat(repository.findById(trumpeteer.id).isPresent()).isTrue();

        output.close();

        assertThat(repository.findById(trumpeteer.id).isPresent()).isFalse();
    }

    @Test
    public void stale_trumpeteers_are_purged() throws Exception {

        Properties props = new Properties();
        props.setProperty(TRUMPETEER_STALE_THRESHOLD, "1");
        props.setProperty(TRUMPETEER_PURGE_INTERVAL, "10");

        TrumpeteerRepository repository = createTrumpeteerRepository(props);

        Trumpeteer trumpeteer = repository.createTrumpeteer(0D, 0D);

        assertThat(repository.findById(trumpeteer.id).isPresent()).isTrue();

        Thread.sleep(300);

        assertThat(repository.findById(trumpeteer.id).isPresent()).isFalse();
    } */

}
