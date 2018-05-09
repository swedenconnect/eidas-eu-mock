package member_country_specific.idp;

public class ApplicationSpecificIDPException extends RuntimeException {

    private final String message;
    private final String title;

    public ApplicationSpecificIDPException(String title, String message) {
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