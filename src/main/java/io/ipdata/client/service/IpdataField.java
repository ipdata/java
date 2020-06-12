package io.ipdata.client.service;

import io.ipdata.client.model.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(of = "name")
public class IpdataField<T> {
  public static final IpdataField<String> IP = new IpdataField<String>("ip", String.class);
  public static final IpdataField<Boolean> IS_EU = new IpdataField<Boolean>("is_eu", Boolean.class);
  public static final IpdataField<String> CITY = new IpdataField<String>("city", String.class);
  public static final IpdataField<String> REGION = new IpdataField<String>("region", String.class);
  public static final IpdataField<String> COUNTRY_NAME = new IpdataField<String>("country_name", String.class);
  public static final IpdataField<String> COUNTRY_CODE = new IpdataField<String>("country_code", String.class);
  public static final IpdataField<String> CONTINENT_CODE = new IpdataField<String>("continent_code", String.class);
  public static final IpdataField<Double> LATITUDE = new IpdataField<Double>("latitude", Double.class);
  public static final IpdataField<Double> LONGITUDE = new IpdataField<Double>("longitude", Double.class);
  public static final IpdataField<AsnModel> ASN = new IpdataField<AsnModel>("asn", AsnModel.class);
  public static final IpdataField<String> ORGANISATION = new IpdataField<String>("organisation", String.class);
  public static final IpdataField<String> POSTAL = new IpdataField<String>("postal", String.class);
  public static final IpdataField<String> CALLING_CODE = new IpdataField<String>("calling_code", String.class);
  public static final IpdataField<String> FLAG = new IpdataField<String>("flag", String.class);
  public static final IpdataField<String> EMOJI_FLAG = new IpdataField<String>("emoji_flag", String.class);
  public static final IpdataField<String> EMOJI_UNICODE = new IpdataField<String>("emoji_unicode", String.class);
  public static final IpdataField<Carrier> CARRIER = new IpdataField<Carrier>("carrier", Carrier.class);
  public static final IpdataField<Language> LANGUAGES = new IpdataField<Language>("languages", Language.class);
  public static final IpdataField<Currency> CURRENCY = new IpdataField<Currency>("currency", Currency.class);
  public static final IpdataField<TimeZone> TIME_ZONE = new IpdataField<TimeZone>("time_zone", TimeZone.class);
  public static final IpdataField<TimeZone> THREAT = new IpdataField<TimeZone>("threat", TimeZone.class);
  public static final IpdataField<Integer> COUNT = new IpdataField<Integer>("count", Integer.class);
  private final String name;
  private final Class<T> type;

  @Override
  public String toString() {
    return name;
  }

  public static class IpdataFieldComparator implements java.util.Comparator<IpdataField<?>> {
    @Override
    public int compare(IpdataField o1, IpdataField o2) {
      if (o1 == null && o2 == null) {
        return 0;
      }
      if (o1 == null) {
        return o2.name == null ? 0 : Integer.MIN_VALUE;
      }
      if (o2 == null) {
        return o1.name == null ? 0 : Integer.MAX_VALUE;
      }
      return o1.name.compareTo(o2.name);
    }
  }

}
