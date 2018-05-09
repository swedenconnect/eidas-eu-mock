package member_country_specific.sp;

public class ApplicationSpecificServiceException extends RuntimeException {
	
	private final String message;
	private final String title;
	
	public ApplicationSpecificServiceException(String title, String message) {
		this.message = message;
		this.title = title;
	}
	@Override
	public String getMessage() {
		return message;
	}
	
	public String getTitle() {
		return title;
	}
}