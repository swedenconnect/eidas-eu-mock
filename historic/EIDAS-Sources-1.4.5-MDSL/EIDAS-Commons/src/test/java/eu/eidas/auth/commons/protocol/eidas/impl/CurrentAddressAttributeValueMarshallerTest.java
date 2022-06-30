package eu.eidas.auth.commons.protocol.eidas.impl;

import org.junit.Test;

/**
 * PostalAddressAttributeValueMarshallerTest
 *
 * @since 1.1
 */
public final class CurrentAddressAttributeValueMarshallerTest extends PostalAddressAttributeValueMarshallerTest {

    public CurrentAddressAttributeValueMarshallerTest() {
        super(new CurrentAddressAttributeValueMarshaller());
    }

    @Test
    public void marshal() throws Exception {
        super.testMarshal();
    }

    @Test
    public void unmarshal() throws Exception {
        super.testUnmarshal();
    }
}
