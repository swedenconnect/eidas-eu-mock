package se.swedenconnect.eidas.test.cef20demohub.data;

import lombok.Data;

@Data
public class FormPostData {
    private String name;
    private String value;
    private FormPostDataType type;

    public FormPostData() {
    }

    public FormPostData(String name, String value, FormPostDataType type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }
}
