package sledgehammer.interfaces;

public interface ExceptionListener {
	
	void onError(String reason, Throwable throwable);
}