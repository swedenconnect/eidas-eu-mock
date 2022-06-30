package eu.eidas.auth.commons.protocol.eidas.impl;

import org.junit.Test;

/**
 * PostalAddressAttributeValueMarshallerTest
 *
 * @since 1.1
 */
public final class LegalAddressAttributeValueMarshallerTest extends PostalAddressAttributeValueMarshallerTest {

    public LegalAddressAttributeValueMarshallerTest() {
        super(new LegalAddressAttributeValueMarshaller());
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
