/*
 * Copyright (c) 2020 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package eu.eidas.auth.engine.core.validator.eidas;

import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.engine.exceptions.ValidationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml.saml2.core.impl.RequestedAuthnContextBuilder;

import static org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration.BETTER;
import static org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration.EXACT;
import static org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration.MINIMUM;


public class EidasRequestedAuthContextValidatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "minimum" and a low notified level of assurance
     * <p>
     * Must succeed.
     */
    @Test
    public void PassOnMinimumSingleNotified() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        final RequestedAuthnContext context = createRequestedAuthnContext(
                MINIMUM,
                NotifiedLevelOfAssurance.LOW.toString());
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "exact" and two non-notified levels of assurance
     * <p>
     * Must succeed.
     */
    @Test
    public void PassOnExactNonNotified() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        final RequestedAuthnContext context = createRequestedAuthnContext(
                EXACT,
                "http://service.memberstate.ms/NotNotified/LoA/low",
                "http://non.eidas.eu/NotNotified/LoA/1"
        );
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "exact", notified high and two non-notified levels of assurance
     * <p>
     * Must succeed.
     */
    @Test
    public void PassOnExactMixedHigh() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        final RequestedAuthnContext context = createRequestedAuthnContext(
                EXACT,
                NotifiedLevelOfAssurance.HIGH.stringValue(),
                "http://service.memberstate.ms/NotNotified/LoA/low",
                "http://non.eidas.eu/NotNotified/LoA/1"
        );
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "exact", notified low (and higher:substantial, high) and two non-notified levels of assurance
     * <p>
     * Must succeed.
     */
    @Test
    public void PassOnMixedLow() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        final RequestedAuthnContext context = createRequestedAuthnContext(
                EXACT,
                NotifiedLevelOfAssurance.LOW.stringValue(),
                NotifiedLevelOfAssurance.SUBSTANTIAL.stringValue(),
                NotifiedLevelOfAssurance.HIGH.stringValue(),
                "http://service.memberstate.ms/NotNotified/LoA/low",
                "http://non.eidas.eu/NotNotified/LoA/1"
        );
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with its comparison missing, notified high and two non-notified levels of assurance
     * <p>
     * Must succeed.
     */
    @Test()
    public void PassOnComparisonTypeNull() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        final RequestedAuthnContext context = createRequestedAuthnContext(
                null,
                NotifiedLevelOfAssurance.HIGH.stringValue(),
                "http://service.memberstate.ms/NotNotified/LoA/low",
                "http://non.eidas.eu/NotNotified/LoA/1"
        );
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "minumum", notified low and high levels of assurance
     * <p>
     * Must fail.
     */
    @Test()
    public void FailOnMinimumMultipleNotified() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        thrown.expect(ValidationException.class);
        thrown.expectMessage(authContextValidator.MINIMUM_MORE_THEN_ONE);

        final RequestedAuthnContext context = createRequestedAuthnContext(
                MINIMUM,
                NotifiedLevelOfAssurance.LOW.stringValue(),
                NotifiedLevelOfAssurance.HIGH.stringValue()
        );
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "minumum", non-notified level of assurance
     * <p>
     * Must fail.
     */
    @Test()
    public void FailOnMinimumNonNotified() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        thrown.expect(ValidationException.class);
        thrown.expectMessage(authContextValidator.MINIMUM_CONTAINS_NON_NOTIFIED);

        final RequestedAuthnContext context = createRequestedAuthnContext(
                MINIMUM,
                "http://service.memberstate.ms/NotNotified/LoA/low"
        );
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "exact", notified high levels of assurance
     * <p>
     * Must fail.
     */
    @Test()
    public void FailOnExactSingleNotified() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        thrown.expect(ValidationException.class);
        thrown.expectMessage(authContextValidator.EXACT_CONTAINS_ONLY_NOTIFIED);

        final RequestedAuthnContext context = createRequestedAuthnContext(
                EXACT,
                NotifiedLevelOfAssurance.HIGH.stringValue()
        );
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "exact", notified low and high levels of assurance (but not substantial)
     * <p>
     * Must fail.
     */
    @Test()
    public void FailOnExactNotifiedMissingHigherLevels() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        thrown.expect(ValidationException.class);
        thrown.expectMessage(authContextValidator.EXACT_NOTIFIED_MISSING_HIGHER_LEVELS);

        final RequestedAuthnContext context = createRequestedAuthnContext(
                EXACT,
                NotifiedLevelOfAssurance.LOW.stringValue(),
                NotifiedLevelOfAssurance.HIGH.stringValue()
        );
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "exact", two identical non-notified notified levels of assurance
     * <p>
     * Must fail.
     */
    @Test()
    public void FailOnExactNotifiedDuplicates() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        thrown.expect(ValidationException.class);
        thrown.expectMessage(authContextValidator.DUPLICATES);

        final RequestedAuthnContext context = createRequestedAuthnContext(
                EXACT,
                NotifiedLevelOfAssurance.LOW.stringValue(),
                NotifiedLevelOfAssurance.LOW.stringValue()
        );
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "exact", two identical non-notified levels of assurance
     * <p>
     * Must fail.
     */
    @Test()
    public void FailOnExactNonNotifiedDuplicates() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        thrown.expect(ValidationException.class);
        thrown.expectMessage(authContextValidator.DUPLICATES);

        final RequestedAuthnContext context = createRequestedAuthnContext(
                EXACT,
                "http://non.eidas.eu/NotNotified/LoA/1",
                "http://non.eidas.eu/NotNotified/LoA/1"
        );
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "exact", notified low(missing higher levels) and two non-notified levels of assurance
     * <p>
     * Must fail.
     */
    @Test()
    public void FailOnExactMixedNotifiedMissingHigherLevels() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        thrown.expect(ValidationException.class);
        thrown.expectMessage(authContextValidator.EXACT_NOTIFIED_MISSING_HIGHER_LEVELS);

        final RequestedAuthnContext context = createRequestedAuthnContext(
                EXACT,
                NotifiedLevelOfAssurance.LOW.stringValue(),
                "http://service.memberstate.ms/NotNotified/LoA/low",
                "http://non.eidas.eu/NotNotified/LoA/1"
        );
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "exact", notified low(missing higher levels) and two non-notified levels of assurance
     * <p>
     * Must fail.
     */
    @Test()
    public void FailOnExactNotifiedMissingHigherLevelsReverseOrder() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        thrown.expect(ValidationException.class);
        thrown.expectMessage(authContextValidator.EXACT_NOTIFIED_MISSING_HIGHER_LEVELS);

        final RequestedAuthnContext context = createRequestedAuthnContext(
                EXACT,
                "http://service.memberstate.ms/NotNotified/LoA/low",
                "http://non.eidas.eu/NotNotified/LoA/1",
                NotifiedLevelOfAssurance.LOW.stringValue()
        );
        authContextValidator.validate(context);
    }

    /**
     * Test method for
     * {@link EidasRequestedAuthContextValidator#validate(RequestedAuthnContext)}
     * when Eidas Request contains an RequestedAuthnContext
     * with comparison "better", notified high and two non-notified levels of assurance
     * <p>
     * Must fail.
     */
    @Test()
    public void FailOnBetterComparisonNotSupported() throws ValidationException {
        final EidasRequestedAuthContextValidator authContextValidator = new EidasRequestedAuthContextValidator();
        thrown.expect(ValidationException.class);
        thrown.expectMessage(authContextValidator.COMPARISON_NOT_SUPPORTED);

        final RequestedAuthnContext context = createRequestedAuthnContext(
                BETTER,
                NotifiedLevelOfAssurance.HIGH.stringValue(),
                "http://service.memberstate.ms/NotNotified/LoA/low",
                "http://non.eidas.eu/NotNotified/LoA/1"
        );
        authContextValidator.validate(context);
    }


    private RequestedAuthnContext createRequestedAuthnContext(AuthnContextComparisonTypeEnumeration comparison, String... loas) {
        RequestedAuthnContext authnContext = new RequestedAuthnContextBuilder().buildObject();
        authnContext.setComparison(comparison);
        for (String loaString : loas) {
            AuthnContextClassRef authnContextClassRef = new AuthnContextClassRefBuilder().buildObject();
            authnContextClassRef.setAuthnContextClassRef(loaString);
            authnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        }
        return authnContext;
    }
}