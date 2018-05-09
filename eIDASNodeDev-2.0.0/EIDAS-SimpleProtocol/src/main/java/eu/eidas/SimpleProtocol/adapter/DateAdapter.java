package eu.eidas.SimpleProtocol.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateAdapter extends XmlAdapter<String, Date> {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public String marshal(Date date) throws Exception {
        synchronized (dateFormat) {
            return (date == null) ? null : dateFormat.format(date);
        }
    }

    @Override
    public Date unmarshal(String str) throws Exception {
        synchronized (dateFormat) {
            return dateFormat.parse(str);
        }
    }

}
