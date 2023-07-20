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

package eu.eidas.auth.commons.protocol.eidas.impl;

import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.impl.AbstractAuthenticationRequest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_NOTIFIED_HIGH;
import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_NOTIFIED_LOW;
import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL;
import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.NON_NOTIFIED_LEVELS_OF_ASSURANCE;
import static org.junit.Assert.assertEquals;

/**
 * AbstractEidasAuthenticationRequestBuilderTest
 * Collection of builder related tests for fields of {@link AbstractEidasAuthenticationRequest}
 */

public class AbstractEidasAuthenticationRequestBuilderTest {

    /**
     * Test method for
     * {@link AbstractEidasAuthenticationRequest.AbstractBuilder}
     * creating a AbstractEidasAuthenticationRequest with all fields defined using the builder
     */
    @Test
    public void testAllFieldsBuilder() {
        final AbstractEidasAuthenticationRequest.AbstractBuilder builder = AbstractEidasAuthenticationRequestBuilderTest
                .createEmptyEidasAuthenticationRequestBuilder();

        AbstractEidasAuthenticationRequestBuilderTest.populateBuilderFields(builder);
        final IEidasAuthenticationRequest abstractEidasAuthenticationRequest = (IEidasAuthenticationRequest)builder.build();

        AbstractEidasAuthenticationRequestBuilderTest.assertAbstractEidasAuthenticationRequestAllFieldsArePresent(abstractEidasAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link AbstractEidasAuthenticationRequest.AbstractBuilder}
     * creating a AbstractEidasAuthenticationRequest with only minimal fields defined using the builder
     */
    @Test
    public void testBuildWithMinimalParams() {
        final AbstractEidasAuthenticationRequest.AbstractBuilder builder = AbstractEidasAuthenticationRequestBuilderTest
                .createEmptyEidasAuthenticationRequestBuilder();
        populateMinimalBuilderFields(builder);
        final IEidasAuthenticationRequest eidasAuthenticationRequest = (IEidasAuthenticationRequest)builder.build();

        Assert.assertNotNull(eidasAuthenticationRequest);
        assertEquals(eidasAuthenticationRequest.getLevelOfAssurance(), LEVEL_OF_ASSURANCE_NOTIFIED_LOW);
    }

    /**
     * Test method for
     * {@link AbstractEidasAuthenticationRequest.AbstractBuilder#AbstractBuilder(IEidasAuthenticationRequest)}
     * creating a AbstractEidasAuthenticationRequest with a builder created from another AbstractEidasAuthenticationRequest
     */
    @Test
    public void testRequestCopyBuilder() {
        final AbstractEidasAuthenticationRequest.AbstractBuilder builder = createEmptyEidasAuthenticationRequestBuilder();
        populateMinimalBuilderFields(builder);
        final IEidasAuthenticationRequest eidasAuthenticationRequest = (IEidasAuthenticationRequest)builder.build();
        final IEidasAuthenticationRequest eidasAuthenticationRequestReplicatedByBuilder =
                (IEidasAuthenticationRequest) createEidasAuthenticationRequestBuilderFromRequest(eidasAuthenticationRequest).build();
        assertAbstractEidasAuthenticationRequestAllFieldsAreEqual(eidasAuthenticationRequest, eidasAuthenticationRequestReplicatedByBuilder);
    }

    /**
     * Test method for
     * {@link AbstractEidasAuthenticationRequest.AbstractBuilder#AbstractBuilder(IEidasAuthenticationRequest)}
     * creating a AbstractEidasAuthenticationRequest with a builder created another builder
     */
    @Test
    public void testBuilderCopyConstructor() {
        final AbstractEidasAuthenticationRequest.AbstractBuilder builder = createEmptyEidasAuthenticationRequestBuilder();
        populateMinimalBuilderFields(builder);
        final IEidasAuthenticationRequest eidasAuthenticationRequest = (IEidasAuthenticationRequest) builder.build();
        final IEidasAuthenticationRequest eidasAuthenticationRequestReplicatedByBuilder = (IEidasAuthenticationRequest) createEidasAuthenticationRequestBuilderFromRequestBuilder(builder).build();
        assertAbstractEidasAuthenticationRequestAllFieldsAreEqual(eidasAuthenticationRequest, eidasAuthenticationRequestReplicatedByBuilder);
    }

    /**
     * Utility method that
     * Calls as little methods on the builder to have a valid object at build()
     * @param builder
     */
    static void populateMinimalBuilderFields(AbstractEidasAuthenticationRequest.AbstractBuilder builder) {
        AbstractAuthenticationRequestBuilderTest.populateMinimalBuilderFields(builder);
    }

    /**
     * Utility method that
     * Calls methods on the builder to have a fully defined object at build()
     * @param builder
     */
    private static void populateBuilderFields(AbstractEidasAuthenticationRequest.AbstractBuilder builder) {
        AbstractAuthenticationRequestBuilderTest.populateAllBuilderFields(builder);
        builder.levelOfAssuranceComparison(LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_COMPARISON_EXACT_STRING);

        final List <LevelOfAssurance> levelOfAssurances = new ArrayList<>();
        levelOfAssurances.add(LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_LOW));
        levelOfAssurances.addAll(NON_NOTIFIED_LEVELS_OF_ASSURANCE);
        builder.levelsOfAssurance(levelOfAssurances);
    }

    private static void assertAbstractEidasAuthenticationRequestAllFieldsArePresent(IEidasAuthenticationRequest abstractEidasAuthenticationRequest) {
        AbstractAuthenticationRequestBuilderTest.assertAbstractAuthenticationRequestAllFieldsArePresent(abstractEidasAuthenticationRequest);

        assertEquals(abstractEidasAuthenticationRequest.getLevelOfAssuranceComparison().stringValue(), LevelOfAssuranceComparison.EXACT.stringValue());
        assertEquals(LEVEL_OF_ASSURANCE_NOTIFIED_LOW, abstractEidasAuthenticationRequest.getLevelOfAssurance());
        assertEquals(
                NON_NOTIFIED_LEVELS_OF_ASSURANCE.stream().map(LevelOfAssurance::getValue).collect(Collectors.toList()),
                abstractEidasAuthenticationRequest.getNonNotifiedLevelsOfAssurance());
        final List expectedList = Stream.of(
                Arrays.asList(eu.eidas.auth.commons.light.impl.LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_LOW)),
                NON_NOTIFIED_LEVELS_OF_ASSURANCE,
                Arrays.asList(eu.eidas.auth.commons.light.impl.LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL)),
                Arrays.asList(eu.eidas.auth.commons.light.impl.LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_HIGH))
        ).flatMap(Collection::stream).collect(Collectors.toList());
        assertEquals(expectedList, abstractEidasAuthenticationRequest.getLevelsOfAssurance());
    }

    private static void assertAbstractEidasAuthenticationRequestAllFieldsAreEqual(IEidasAuthenticationRequest expected, IEidasAuthenticationRequest actual) {
        assertEquals(expected.getLevelOfAssuranceComparison().stringValue(), actual.getLevelOfAssuranceComparison().stringValue());
        assertEquals(expected.getLevelOfAssuranceComparison(),               actual.getLevelOfAssuranceComparison());
        assertEquals(expected.getLevelOfAssurance(),                         actual.getLevelOfAssurance());
        assertEquals(expected.getNonNotifiedLevelsOfAssurance(),             actual.getNonNotifiedLevelsOfAssurance());
    }

    static AbstractEidasAuthenticationRequest.AbstractBuilder createEmptyEidasAuthenticationRequestBuilder() {
        return new ConcreteAbstractEidasAuthenticationRequestBuilder();
    }

    static AbstractEidasAuthenticationRequest.AbstractBuilder createEidasAuthenticationRequestBuilderFromRequestBuilder(AbstractEidasAuthenticationRequest.AbstractBuilder builder) {
        return new ConcreteAbstractEidasAuthenticationRequestBuilder(builder);
    }

    static AbstractEidasAuthenticationRequest.AbstractBuilder createEidasAuthenticationRequestBuilderFromRequest(IEidasAuthenticationRequest iar) {
        return new ConcreteAbstractEidasAuthenticationRequestBuilder(iar);
    }

    /**
     * Concrete class for testing {@link AbstractAuthenticationRequest.AbstractBuilder}
     */
    static class ConcreteAbstractEidasAuthenticationRequestBuilder extends AbstractEidasAuthenticationRequest.AbstractBuilder {

        ConcreteAbstractEidasAuthenticationRequestBuilder() {
            super();
        }

        ConcreteAbstractEidasAuthenticationRequestBuilder(AbstractEidasAuthenticationRequest.AbstractBuilder abstractBuilder) {
            super(abstractBuilder);
        }

        ConcreteAbstractEidasAuthenticationRequestBuilder(IEidasAuthenticationRequest eidasAuthenticationRequest) {
            super(eidasAuthenticationRequest);
        }

        @Override
        protected void validateOtherFields() throws IllegalArgumentException {

        }

        @Nonnull
        @Override
        protected IAuthenticationRequest newInstance() {
            return new AbstractEidasAuthenticationRequest(this) {};
        }
    }
}