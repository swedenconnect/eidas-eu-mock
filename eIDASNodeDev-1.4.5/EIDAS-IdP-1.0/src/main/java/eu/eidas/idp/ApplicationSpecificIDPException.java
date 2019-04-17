package eu.eidas.idp;

public class ApplicationSpecificIDPException extends RuntimeException {

	private String msg;
	private String title;

	public ApplicationSpecificIDPException(String title, String msg) {
		this.msg = msg;
		this.title = title;
	}

	public ApplicationSpecificIDPException(String title, Exception e) {
		this.msg = e.getMessage();
		this.title = title;
	}


	public String getMessage() {
		return msg;
	}
	
	public String getTitle() {
		return title;
	}
}