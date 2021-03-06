package ecologylab.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ecologylab.serialization.annotations.simpl_scalar;

/**
 * The base class for logging events.
 * 
 * @author quyin
 */
public class LogEvent
{

  static SimpleDateFormat dateFormat;

  static
  {
    dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'Z");
    dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
  }

  @simpl_scalar
  private long            timestamp;

  @simpl_scalar
  private String          time;

  @simpl_scalar
  private String          app;

  @simpl_scalar
  private String          username;

  @simpl_scalar
  private String          hashKey;

  public LogEvent()
  {
    // Default constructor must be empty, for deserialization.
  }

  /**
   * @return Timestamp of this event.
   */
  public long getTimestamp()
  {
    return timestamp;
  }

  public void setTimestamp(long timestamp)
  {
    this.timestamp = timestamp;
    time = dateFormat.format(new Date(timestamp));
  }

  /**
   * @return A human readable string for the timestamp of this event.
   */
  public String getReadableTime()
  {
    if (time == null)
    {
      time = dateFormat.format(new Date(timestamp));
    }
    return time;
  }

  /**
   * @return The app that generates this event.
   */
  public String getApp()
  {
    return app;
  }

  public void setApp(String app)
  {
    this.app = app;
  }

  /**
   * @return The username associated with this event.
   */
  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  /**
   * @return The hash key associated with this event.
   */
  public String getHashKey()
  {
    return hashKey;
  }

  public void setHashKey(String hashKey)
  {
    this.hashKey = hashKey;
  }

}
