package sessionManagement;

public class SessionValues
{
	public Integer sessionVersion;
	public String sessionMessage;
	public String sessionExpiredTS;
	public String locMetaData;
	
	public SessionValues(Integer version, String message, String expiredTS)
	{
		this.sessionVersion = version;
		this.sessionMessage = message;
		this.sessionExpiredTS = expiredTS;
	}
	
	public SessionValues()
	{
		this(0, "", "");
	}
	
	@Override
	public String toString()
	{
		return super.toString();
	}
}
