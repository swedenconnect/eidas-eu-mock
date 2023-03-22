/*
 * Copyright (c) 2021 by European Commission
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

package eu.eidas.auth.commons.attribute;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.io.SingletonAccessor;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

/**
 * AttributeRegistryTest
 *
 * @since 1.1
 */
public final class AttributeRegistryTest {

    private static final String TEST_FILE = "src/test/resources/exampleAttributes.xml";

    private static final String TEST_FILE_WITH_SPACES = "src/test/resources/folder with spaces/exampleAttributes2.xml";

    private static final AttributeDefinition<String> CURRENT_FAMILY_NAME =
            new AttributeDefinition.Builder<String>()
                    .nameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
                    .friendlyName("FamilyName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .transliterationMandatory(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final AttributeDefinition<String> TOTALLY_WRONG_ATTRIBUTE =
            new AttributeDefinition.Builder<String>()
                    .nameUri("http://eidas.europa.eu/attributes/naturalperson/WrongAttribute")
                    .friendlyName("WrongAttribute")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .transliterationMandatory(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "WrongAttribute", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static AttributeRegistry attributeRegistry;

    @BeforeClass
    public static void startup() {
        attributeRegistry = AttributeRegistries.fromFile(TEST_FILE, null);
    }
        @SuppressWarnings({"PublicField"})
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for
     * {@link AttributeRegistry#logRetrievedAttributes()}
     * when Trace level logging is enabled
     * <p>
     * Must succeed.
     */
    @Test
    public void testLogRetrievedAttributesTrace() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Logger logger = (Logger) LoggerFactory.getLogger(AttributeRegistry.class.getName());
        ListAppender<ILoggingEvent> infoAppender = createStartListAppender(new LevelFilter());
        logger.addAppender(infoAppender);
        logger.setLevel(Level.TRACE);

        Field loggerField = AttributeRegistry.class.getDeclaredField("LOG");
        loggerField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(loggerField, loggerField.getModifiers() & ~Modifier.FINAL);

        Logger oldLogger = (Logger) loggerField.get(attributeRegistry);
        loggerField.set(attributeRegistry,logger);

        Method logRetrievedAttributesMethod = AttributeRegistry.class.getDeclaredMethod("logRetrievedAttributes");
        logRetrievedAttributesMethod.setAccessible(true);

        logRetrievedAttributesMethod.invoke(attributeRegistry);

        assertThat(infoAppender.list.size(), Matchers.greaterThanOrEqualTo(1));

        String loggedMessage = infoAppender.list.get(0).getMessage();
        assertThat(loggedMessage, containsString("AttributeRegistry contains attributes"));

        logger.detachAndStopAllAppenders();
        loggerField.set(attributeRegistry, oldLogger);
    }

    /**
     * Test method for
     * {@link AttributeRegistry#logRetrievedAttributes()}
     * when Accessors are invalid
     * <p>
     * Must fail.
     */
    @Test
    public void testLogRetrievedAttributesIoException() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, IOException {
        expectedException.expectCause(isA(InternalErrorEIDASException.class));

        SingletonAccessor mockAccessor = Mockito.mock(SingletonAccessor.class);
        ImmutableList mockAccessors = ImmutableList.builder()
                .add(mockAccessor)
                .build();

        AttributeDefinitionDao mockedDao = Mockito.mock(AttributeDefinitionDao.class);
        AttributeRegistry testRegistry = AttributeRegistries.fromFile(TEST_FILE, null);
        Field attributeDefinitionDaoField = getAttributeDefinitionDaoField(testRegistry);
        attributeDefinitionDaoField.set(testRegistry,mockedDao);

        Mockito.when(mockedDao.getAttributeDefinitionAccessors()).thenReturn(mockAccessors);
        Mockito.when(mockAccessor.get()).thenThrow(IOException.class);

        Method logRetrievedAttributesMethod = AttributeRegistry.class.getDeclaredMethod("logRetrievedAttributes");
        logRetrievedAttributesMethod.setAccessible(true);

        logRetrievedAttributesMethod.invoke(testRegistry);
    }


    /**
     * Test method for
     * {@link AttributeRegistry#getAttributes()}
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetAttributes() {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getAttributes();

        assertThat(attributes.size(), is(Integer.valueOf(20)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.TRUE));
    }

    /**
     * Test method for
     * {@link AttributeRegistry#getAttributes()}
     * when Accessors are invalid
     * <p>
     * Must fail.
     */
    @Test
    public void testGetAttributesIoException() throws IOException {
        expectedException.expect(InternalErrorEIDASException.class);

        final SingletonAccessor mockAccessor = Mockito.mock(SingletonAccessor.class);
        Mockito.when(mockAccessor.get()).thenReturn(Mockito.mock(ImmutableSortedSet.class));
        final ImmutableList accessors = new ImmutableList.Builder().add(mockAccessor).build();
        final AttributeDefinitionDao mockedDao = Mockito.mock(AttributeDefinitionDao.class);
        Mockito.when(mockedDao.getAttributeDefinitionAccessors()).thenReturn(accessors);
        final AttributeRegistry attributeRegistry = new AttributeRegistry(mockedDao);

        Mockito.when(mockAccessor.get()).thenThrow(IOException.class);
        attributeRegistry.getAttributes();
    }

    /**
     * Test method for
     * {@link AttributeRegistry#getByFilter(AttributeRegistry.AttributeDefinitionFilter)}
     * when Accessors are invalid
     * <p>
     * Must fail.
     */
    @Test
    public void testGetAttributesGetByFilterIoException() throws NoSuchFieldException, IllegalAccessException, IOException {
        expectedException.expect(InternalErrorEIDASException.class);

        SingletonAccessor mockAccessorOne = Mockito.mock(SingletonAccessor.class);
        SingletonAccessor mockAccessorTwo = Mockito.mock(SingletonAccessor.class);
        ImmutableList mockAccessors = ImmutableList.of(mockAccessorOne,mockAccessorTwo);
        AttributeDefinitionDao mockedDao = Mockito.mock(AttributeDefinitionDao.class);
        AttributeRegistry testRegistry = AttributeRegistries.fromFile(TEST_FILE, null);
        Field attributeDefinitionDaoField = getAttributeDefinitionDaoField(testRegistry);
        attributeDefinitionDaoField.set(testRegistry,mockedDao);

        Mockito.when(mockedDao.getAttributeDefinitionAccessors()).thenReturn(mockAccessors);
        Mockito.when(mockAccessorOne.get()).thenThrow(IOException.class);

        testRegistry.getAttributes();
    }

    /**
     * Test method for
     * {@link AttributeRegistry#getByFriendlyName(String)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetByFriendlyName() {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getByFriendlyName("FamilyName");

        assertThat(attributes.size(), is(Integer.valueOf(1)));
        assertThat(attributes.iterator().next(), is((Object) CURRENT_FAMILY_NAME));
    }

    /**
     * Test method for
     * {@link AttributeRegistry#getByRequired(boolean)}
     * when boolean is set to true
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetByRequiredTrue() {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getByRequired(true);

        assertThat(attributes.size(), is(Integer.valueOf(6)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.TRUE));
    }

    /**
     * Test method for
     * {@link AttributeRegistry#getByRequired(boolean)}
     * when boolean is set to false
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetByRequiredFalse() {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getByRequired(false);

        assertThat(attributes.size(), is(Integer.valueOf(14)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.FALSE));
    }

    /**
     * Test method for
     * {@link AttributeRegistry#getByPersonType(PersonType)}
     * when PersonType is NATURAL_PERSON
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetByPersonTypeNatural() {
        ImmutableSortedSet<AttributeDefinition<?>> attributes =
                attributeRegistry.getByPersonType(PersonType.NATURAL_PERSON);

        assertThat(attributes.size(), is(Integer.valueOf(9)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.TRUE));
    }

    /**
     * Test method for
     * {@link AttributeRegistry#getByPersonType(PersonType)}
     * when PersonTYpe is LEGAL_PERSON
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetByPersonTypeLegal() {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getByPersonType(PersonType.LEGAL_PERSON);

        assertThat(attributes.size(), is(Integer.valueOf(11)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.FALSE));
    }

    /**
     * Test method for
     * {@link AttributeRegistry#getByXmlType(QName)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetByXmlType() {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getByXmlType(
                new QName("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType"));

        assertThat(attributes.size(), is(Integer.valueOf(1)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.TRUE));
    }

    /**
     * Test method for
     * {@link AttributeRegistries#fromFile(String, String)}
     * when file cannot be found
     * <p>
     * Must fail.
     */
    @Test
    public void testWrongFile() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(startsWith("java.io.FileNotFoundException: File \"WrongFile.xml\" cannot be found from path: \""));
        expectedException.expectMessage(endsWith("WrongFile.xml\""));

        AttributeRegistry attributeRegistry = AttributeRegistries.fromFile("WrongFile.xml", null);
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getAttributes();

        assertThat(attributes.size(), is(Integer.valueOf(20)));
    }

    /**
     * Test method for
     * {@link AttributeRegistries#fromFile(String, String)}
     * when filepath contains spaces
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetAttributesFromFileWithSpacesInPath() {

        AttributeRegistry attributeRegistry = AttributeRegistries.fromFile(TEST_FILE_WITH_SPACES, null);

        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getAttributes();

        assertThat(attributes.size(), is(Integer.valueOf(20)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.TRUE));
    }

    /**
     * Test method for
     * {@link AttributeRegistry#getAttributes()}
     * <p>
     * Must succeed.
     */
    @Test
    public void testWrongAttributeCannotBeFound() {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getAttributes();
        assertThat(attributes.contains(TOTALLY_WRONG_ATTRIBUTE), is(Boolean.FALSE));
    }

    private Field getAttributeDefinitionDaoField(AttributeRegistry registry) throws NoSuchFieldException, IllegalAccessException {
        Field daoField = registry.getClass().getDeclaredField("attributeDefinitionDao");
        daoField.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(daoField, daoField.getModifiers() & ~Modifier.FINAL);

        return daoField;
    }
    private static ListAppender<ILoggingEvent> createStartListAppender(LevelFilter levelFilter) {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.addFilter(levelFilter);
        listAppender.start();

        return listAppender;
    }
}
