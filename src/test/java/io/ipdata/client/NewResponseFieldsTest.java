package io.ipdata.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ipdata.client.model.Blocklist;
import io.ipdata.client.model.IpdataModel;
import io.ipdata.client.model.ThreatModel;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.junit.Test;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import static io.ipdata.client.service.IpdataField.IS_ANYCAST;
import static io.ipdata.client.service.IpdataField.IS_EU;
import static io.ipdata.client.service.IpdataField.IS_SATELLITE;
import static io.ipdata.client.service.IpdataField.THREAT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The {@code is_satellite} and {@code is_anycast} flags (sent by the API only when true) and the optional
 * {@code first_seen}, {@code last_seen} and {@code days_seen} attributes of blocklist entries.
 */
public class NewResponseFieldsTest {

  private static final MockIpdataServer MOCK = MockIpdataServer.getInstance();
  private static final TestContext TEST_CONTEXT = new TestContext(MockIpdataServer.API_KEY, MOCK.getUrl());

  /** Sample A: satellite ISP, empty blocklists. */
  private static final String SATELLITE_IP = "98.97.34.0";
  /** Sample B: anycast address, two dated blocklist entries. */
  private static final String ANYCAST_IP = "8.8.8.8";
  /** Sample C: VPN exit, neither flag present, third blocklist entry without dates. */
  private static final String VPN_IP = "187.13.250.1";

  private static List<IpdataService> services() {
    return Arrays.asList(TEST_CONTEXT.ipdataService(), TEST_CONTEXT.cachingIpdataService());
  }

  private static void assertDated(Blocklist entry, String name, String type, int daysSeen, String firstSeen, String lastSeen) {
    assertEquals(name, entry.getName());
    assertEquals(type, entry.getType());
    assertEquals(Integer.valueOf(daysSeen), entry.getDaysSeen());
    assertEquals(firstSeen, entry.getFirstSeen());
    assertEquals(lastSeen, entry.getLastSeen());
  }

  @Test
  @SneakyThrows
  public void satelliteFlagIsMapped() {
    for (IpdataService service : services()) {
      IpdataModel model = service.ipdata(SATELLITE_IP);
      assertTrue(model.isSatellite());
      assertTrue(model.satellite());
      assertFalse(model.isAnycast());
      assertFalse(model.isEu());
      assertEquals("AS14593", model.asn().asn());
      assertTrue(model.threat().getBlocklists().isEmpty());
      assertEquals(97, model.threat().getScores().getTrustScore());
    }
  }

  @Test
  @SneakyThrows
  public void anycastFlagAndDatedBlocklistEntriesAreMapped() {
    for (IpdataService service : services()) {
      IpdataModel model = service.ipdata(ANYCAST_IP);
      assertTrue(model.isAnycast());
      assertTrue(model.anycast());
      assertFalse(model.isSatellite());
      ThreatModel threat = model.threat();
      assertTrue(threat.isDatacenter());
      assertEquals(2, threat.getBlocklists().size());
      assertDated(threat.getBlocklists().get(0), "ipdata", "cloud", 1, "2026-11-02", "2026-11-02");
      assertDated(threat.getBlocklists().get(1), "ipdata", "open_resolvers", 1, "2026-11-02", "2026-11-02");
      assertEquals("https://ipdata.co", threat.getBlocklists().get(0).getSite());
      assertEquals(2, threat.getScores().getTrustScore());
    }
  }

  @Test
  @SneakyThrows
  public void flagsReadFalseWhenAbsentAndUndatedBlocklistEntriesHaveNullDates() {
    for (IpdataService service : services()) {
      IpdataModel model = service.ipdata(VPN_IP);
      assertFalse(model.isSatellite());
      assertFalse(model.isAnycast());
      assertFalse(model.isEu());
      ThreatModel threat = model.threat();
      assertTrue(threat.isVpn());
      assertTrue(threat.isThreat());
      List<Blocklist> blocklists = threat.getBlocklists();
      assertEquals(6, blocklists.size());
      assertDated(blocklists.get(0), "Gluetun", "vpn", 2, "2026-10-31", "2026-11-02");
      assertDated(blocklists.get(1), "ipdata", "cloud", 1, "2026-11-02", "2026-11-02");
      Blocklist undated = blocklists.get(2);
      assertEquals("ipdata", undated.getName());
      assertEquals("https://ipdata.co", undated.getSite());
      assertEquals("vpn", undated.getType());
      assertNull(undated.getFirstSeen());
      assertNull(undated.getLastSeen());
      assertNull(undated.getDaysSeen());
      assertDated(blocklists.get(3), "NordVPN", "vpn", 2, "2026-10-31", "2026-11-02");
      assertDated(blocklists.get(4), "Wikimedia Global Blocklist", "general", 2, "2026-10-31", "2026-11-02");
      assertDated(blocklists.get(5), "X4BNet", "vpn", 2, "2026-10-31", "2026-11-02");
      assertEquals(0, threat.getScores().getTrustScore());
    }
  }

  @Test
  @SneakyThrows
  public void threatEndpointCarriesTheBlocklistDates() {
    for (IpdataService service : services()) {
      ThreatModel anycast = service.threat(ANYCAST_IP);
      assertDated(anycast.getBlocklists().get(1), "ipdata", "open_resolvers", 1, "2026-11-02", "2026-11-02");
      ThreatModel vpn = service.threat(VPN_IP);
      assertEquals(6, vpn.getBlocklists().size());
      assertNull(vpn.getBlocklists().get(2).getDaysSeen());
      assertEquals("2026-10-31", vpn.getBlocklists().get(5).getFirstSeen());
    }
  }

  @Test
  @SneakyThrows
  public void singleFieldEndpointsReturnTheFlags() {
    for (IpdataService service : services()) {
      assertTrue(service.isSatellite(SATELLITE_IP));
      assertTrue(service.isAnycast(ANYCAST_IP));
      assertFalse(service.isEu(ANYCAST_IP));
    }
    IpdataService service = TEST_CONTEXT.ipdataService();
    service.isSatellite(SATELLITE_IP);
    assertEquals("/" + SATELLITE_IP + "/is_satellite", MOCK.getLastRawPath());
    service.isAnycast(ANYCAST_IP);
    assertEquals("/" + ANYCAST_IP + "/is_anycast", MOCK.getLastRawPath());
  }

  @Test
  @SneakyThrows
  public void fieldSelectionAcceptsTheNewFieldNames() {
    assertEquals("is_satellite", IS_SATELLITE.name());
    assertEquals("is_anycast", IS_ANYCAST.name());
    assertEquals("is_satellite", IS_SATELLITE.toString());
    assertEquals(Boolean.class, IS_SATELLITE.type());
    assertEquals(Boolean.class, IS_ANYCAST.type());

    for (IpdataService service : services()) {
      IpdataModel model = service.getFields(SATELLITE_IP, IS_SATELLITE, IS_ANYCAST, IS_EU);
      assertTrue(model.isSatellite());
      assertFalse(model.isAnycast()); // the mock, like the API, sends the key only when it is true
      assertFalse(model.isEu());
      assertNull(model.city());

      model = service.getFields(ANYCAST_IP, IS_ANYCAST, THREAT);
      assertTrue(model.isAnycast());
      assertFalse(model.isSatellite());
      assertEquals(Integer.valueOf(1), model.threat().getBlocklists().get(0).getDaysSeen());
    }

    TEST_CONTEXT.ipdataService().getFields(VPN_IP, IS_SATELLITE, IS_ANYCAST);
    String query = URLDecoder.decode(MOCK.getLastRawQuery(), "UTF-8");
    assertTrue(query, query.contains("fields=is_anycast,is_satellite"));
  }

  @Test
  @SneakyThrows
  public void bulkResponsesCarryTheNewFieldsPerItem() {
    for (IpdataService service : services()) {
      List<IpdataModel> models = service.bulk(Arrays.asList(SATELLITE_IP, ANYCAST_IP, VPN_IP));
      assertEquals(3, models.size());

      assertEquals(SATELLITE_IP, models.get(0).ip());
      assertTrue(models.get(0).isSatellite());
      assertFalse(models.get(0).isAnycast());

      assertEquals(ANYCAST_IP, models.get(1).ip());
      assertTrue(models.get(1).isAnycast());
      assertFalse(models.get(1).isSatellite());
      assertDated(models.get(1).threat().getBlocklists().get(1), "ipdata", "open_resolvers", 1, "2026-11-02", "2026-11-02");

      assertEquals(VPN_IP, models.get(2).ip());
      assertFalse(models.get(2).isSatellite());
      assertFalse(models.get(2).isAnycast());
      assertNull(models.get(2).threat().getBlocklists().get(2).getFirstSeen());
      assertDated(models.get(2).threat().getBlocklists().get(3), "NordVPN", "vpn", 2, "2026-10-31", "2026-11-02");
    }
  }

  @Test
  @SneakyThrows
  public void modelRoundTripsTheNewFields() {
    for (String ip : Arrays.asList(SATELLITE_IP, ANYCAST_IP, VPN_IP)) {
      IpdataModel model = TEST_CONTEXT.ipdataService().ipdata(ip);
      // the strict test mapper (fails on unknown properties) must know every key the fixtures carry
      String expected = TEST_CONTEXT.get("/" + ip, null);
      TEST_CONTEXT.mapper().readValue(expected, IpdataModel.class);
      String actual = TEST_CONTEXT.mapper().writeValueAsString(model);
      TEST_CONTEXT.assertEqualJson(actual, expected, TEST_CONTEXT.configuration().whenIgnoringPaths("time_zone.current_time"));
    }
  }

  @Test
  @SneakyThrows
  public void decoderIgnoresUnknownKeys() {
    String ip = "203.0.113.7";
    ObjectNode response = (ObjectNode) TEST_CONTEXT.mapper().readTree(TEST_CONTEXT.get("/" + VPN_IP, null));
    response.put("ip", ip);
    response.put("is_some_future_flag", true);
    response.putObject("some_future_object").put("level", 3);
    ((ObjectNode) response.get("threat")).put("is_some_future_threat", true);
    ((ObjectNode) response.get("threat").get("blocklists").get(0)).put("confidence", 0.9);
    ((ObjectNode) response.get("asn")).put("some_future_attribute", "x");
    MOCK.registerFixture(ip, response.toString());

    for (IpdataService service : services()) {
      IpdataModel model = service.ipdata(ip);
      assertEquals(ip, model.ip());
      assertTrue(model.threat().isVpn());
      assertFalse(model.isAnycast());
      assertDated(model.threat().getBlocklists().get(0), "Gluetun", "vpn", 2, "2026-10-31", "2026-11-02");
      assertEquals("AS28573", model.asn().asn());

      ThreatModel threat = service.threat(ip);
      assertEquals(6, threat.getBlocklists().size());
      assertNull(threat.getBlocklists().get(2).getLastSeen());
    }
  }
}
