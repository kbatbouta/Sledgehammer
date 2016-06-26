package sledgehammer.event;

public class LoginUsernameDefinedEvent extends Event {

	public static final String ID = "LoginUsernameDefinedEvent";
	
	private String username;
	
	public LoginUsernameDefinedEvent(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public String getLogMessage() { 
		return "User attempting to log in with username: \"" + username + "\"."; 
	}

	@Override
	public String getID() { return ID; }

}
