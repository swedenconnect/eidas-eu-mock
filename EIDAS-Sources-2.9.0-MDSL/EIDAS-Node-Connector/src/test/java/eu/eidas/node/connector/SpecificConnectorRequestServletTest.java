/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.node.connector;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.ConnectorBeanNames;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.auth.connector.ICONNECTORSAMLService;
import eu.eidas.node.auth.connector.ICONNECTORService;
import eu.eidas.node.connector.exceptions.ConnectorError;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import eu.eidas.specificcommunication.protocol.validation.IncomingLightRequestValidatorLoAComponent;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Test class for the {@link SpecificConnectorRequestServlet}
 */
@RunWith(MockitoJUnitRunner.class)
public class SpecificConnectorRequestServletTest {

    private final String samlRequestBase64 = "PHNhbWwycDpBdXRoblJlcXVlc3QgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiIHhtbG5zOmVpZGFzPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L3NhbWwtZXh0ZW5zaW9ucyIKICAgICAgICAgICAgICAgICAgICAgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iCiAgICAgICAgICAgICAgICAgICAgIHhtbG5zOnNhbWwycD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIgogICAgICAgICAgICAgICAgICAgICBDb25zZW50PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6Y29uc2VudDp1bnNwZWNpZmllZCIKICAgICAgICAgICAgICAgICAgICAgRGVzdGluYXRpb249Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9FaWRhc05vZGVQcm94eS9Db2xsZWFndWVSZXF1ZXN0IiBGb3JjZUF1dGhuPSJ0cnVlIgogICAgICAgICAgICAgICAgICAgICBJRD0iX2pQaVB0dnVzX3JVLlJ2dWpheHQ5aDlQUkxMdnpILm50YW9FMXFVX0RkY1Y0eWp5eG81ejd1WnRvX19EMVlBciIgSXNQYXNzaXZlPSJmYWxzZSIKICAgICAgICAgICAgICAgICAgICAgSXNzdWVJbnN0YW50PSIyMDIyLTEyLTE0VDE0OjE5OjAzLjg2MFoiIFByb3ZpZGVyTmFtZT0iREVNTy1TUC1DQSIgVmVyc2lvbj0iMi4wIj4KICAgIDxzYW1sMjpJc3N1ZXIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6bmFtZWlkLWZvcm1hdDplbnRpdHkiPgogICAgICAgIGh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9FaWRhc05vZGVDb25uZWN0b3IvQ29ubmVjdG9yTWV0YWRhdGEKICAgIDwvc2FtbDI6SXNzdWVyPgogICAgPGRzOlNpZ25hdHVyZT4KICAgICAgICA8ZHM6U2lnbmVkSW5mbz4KICAgICAgICAgICAgPGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KICAgICAgICAgICAgPGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI2VjZHNhLXNoYTUxMiIvPgogICAgICAgICAgICA8ZHM6UmVmZXJlbmNlIFVSST0iI19qUGlQdHZ1c19yVS5SdnVqYXh0OWg5UFJMTHZ6SC5udGFvRTFxVV9EZGNWNHlqeXhvNXo3dVp0b19fRDFZQXIiPgogICAgICAgICAgICAgICAgPGRzOlRyYW5zZm9ybXM+CiAgICAgICAgICAgICAgICAgICAgPGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIi8+CiAgICAgICAgICAgICAgICAgICAgPGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPgogICAgICAgICAgICAgICAgPC9kczpUcmFuc2Zvcm1zPgogICAgICAgICAgICAgICAgPGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3NoYTUxMiIvPgogICAgICAgICAgICAgICAgPGRzOkRpZ2VzdFZhbHVlPgogICAgICAgICAgICAgICAgICAgIDVlVE9xYzlsTjdXeHJKeVJtRTBJNzJqSFJyWjM0TXBGb0tBbmUrd3FOb2gyYkp1TXhJMUxzMmpRZTlLcVNlUkJkSVZNcFlQOE1lb2VVbjhjUzBxaUhBPT0KICAgICAgICAgICAgICAgIDwvZHM6RGlnZXN0VmFsdWU+CiAgICAgICAgICAgIDwvZHM6UmVmZXJlbmNlPgogICAgICAgIDwvZHM6U2lnbmVkSW5mbz4KICAgICAgICA8ZHM6U2lnbmF0dXJlVmFsdWU+U0tPaTk5YUVyeTdseE5QMkEyM1FJUmxBZzFoTHVKNjBiak9WMDd0UEVtbnhRY1NYNzJkRVhjYVY1QjBFQjNnNCtaaXB0T01qd0c3aStBZjJITFlEOGc9PQogICAgICAgIDwvZHM6U2lnbmF0dXJlVmFsdWU+CiAgICAgICAgPGRzOktleUluZm8+CiAgICAgICAgICAgIDxkczpYNTA5RGF0YT4KICAgICAgICAgICAgICAgIDxkczpYNTA5Q2VydGlmaWNhdGU+TUlJQ0l6Q0NBY21nQXdJQkFnSVVWTXJNYlhuR3hwbUhRZVYrQUh2L0JVSjNqbG93Q2dZSUtvWkl6ajBFQXdJd1p6RUxNQWtHQTFVRQogICAgICAgICAgICAgICAgICAgIEJoTUNRa1V4RURBT0JnTlZCQWdNQjBKbGJHZHBkVzB4RVRBUEJnTlZCQWNNQ0VKeWRYTnpaV3h6TVFzd0NRWURWUVFLREFKRlF6RU8KICAgICAgICAgICAgICAgICAgICBNQXdHQTFVRUN3d0ZSRWxIU1ZReEZqQVVCZ05WQkFNTURXNXBjM1JRTWpVMkxXTmxjblF3SGhjTk1Ua3hNVEU0TVRBeE9UTTFXaGNOCiAgICAgICAgICAgICAgICAgICAgTkRrd056QTRNVEF4T1RNMVdqQm5NUXN3Q1FZRFZRUUdFd0pDUlRFUU1BNEdBMVVFQ0F3SFFtVnNaMmwxYlRFUk1BOEdBMVVFQnd3SQogICAgICAgICAgICAgICAgICAgIFFuSjFjM05sYkhNeEN6QUpCZ05WQkFvTUFrVkRNUTR3REFZRFZRUUxEQVZFU1VkSlZERVdNQlFHQTFVRUF3d05ibWx6ZEZBeU5UWXQKICAgICAgICAgICAgICAgICAgICBZMlZ5ZERCWk1CTUdCeXFHU000OUFnRUdDQ3FHU000OUF3RUhBMElBQk9lTDNtckJLRHJhSG54dGE0ODZFeTBGa0tSVlM2dzlYR1RuCiAgICAgICAgICAgICAgICAgICAgeEJZYmJ0RjNKa2VzZkJVSW1QTG8wNjFkdmhQQ014am1HdDM4OFFGM3c5MGlCaWpwbVFDalV6QlJNQjBHQTFVZERnUVdCQlRMakI5bQogICAgICAgICAgICAgICAgICAgIFVOc29JeCs0VTh2cEFvRXJEM2E5U2pBZkJnTlZIU01FR0RBV2dCVExqQjltVU5zb0l4KzRVOHZwQW9FckQzYTlTakFQQmdOVkhSTUIKICAgICAgICAgICAgICAgICAgICBBZjhFQlRBREFRSC9NQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUURuK0RYSzJoZWhPemVtWGlvOXpwZWNjcW9FVVFNWEtjTnBzeWpXCiAgICAgICAgICAgICAgICAgICAgNnFYYlRnSWdRS1JVY0xycjdIaDBDTWdTTVNZcVZyNWFWUnZNcjAwUklna2RaL1lLT0ZZPQogICAgICAgICAgICAgICAgPC9kczpYNTA5Q2VydGlmaWNhdGU+CiAgICAgICAgICAgIDwvZHM6WDUwOURhdGE+CiAgICAgICAgPC9kczpLZXlJbmZvPgogICAgPC9kczpTaWduYXR1cmU+CiAgICA8c2FtbDJwOkV4dGVuc2lvbnM+CiAgICAgICAgPGVpZGFzOlNQVHlwZT5wdWJsaWM8L2VpZGFzOlNQVHlwZT4KICAgICAgICA8ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlcz4KICAgICAgICAgICAgPGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IkQtMjAxMi0xNy1FVUlkZW50aWZpZXIiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL2xlZ2FscGVyc29uL0QtMjAxMi0xNy1FVUlkZW50aWZpZXIiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9ImZhbHNlIi8+CiAgICAgICAgICAgIDxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJFT1JJIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vRU9SSSIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz4KICAgICAgICAgICAgPGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IkxFSSIgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL2xlZ2FscGVyc29uL0xFSSIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz4KICAgICAgICAgICAgPGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IkxlZ2FsTmFtZSIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vTGVnYWxOYW1lIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJ0cnVlIi8+CiAgICAgICAgICAgIDxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJMZWdhbEFkZHJlc3MiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL2xlZ2FscGVyc29uL0xlZ2FsUGVyc29uQWRkcmVzcyIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz4KICAgICAgICAgICAgPGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IkxlZ2FsUGVyc29uSWRlbnRpZmllciIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vTGVnYWxQZXJzb25JZGVudGlmaWVyIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJ0cnVlIi8+CiAgICAgICAgICAgIDxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJTRUVEIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vU0VFRCIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz4KICAgICAgICAgICAgPGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IlNJQyIgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL2xlZ2FscGVyc29uL1NJQyIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz4KICAgICAgICAgICAgPGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IlRheFJlZmVyZW5jZSIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vVGF4UmVmZXJlbmNlIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSIvPgogICAgICAgICAgICA8ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iVkFUUmVnaXN0cmF0aW9uIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9sZWdhbHBlcnNvbi9WQVRSZWdpc3RyYXRpb25OdW1iZXIiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9ImZhbHNlIi8+CiAgICAgICAgICAgIDxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJCaXJ0aE5hbWUiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vQmlydGhOYW1lIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSIvPgogICAgICAgICAgICA8ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iQ3VycmVudEFkZHJlc3MiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vQ3VycmVudEFkZHJlc3MiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9ImZhbHNlIi8+CiAgICAgICAgICAgIDxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJGYW1pbHlOYW1lIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9uYXR1cmFscGVyc29uL0N1cnJlbnRGYW1pbHlOYW1lIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJ0cnVlIi8+CiAgICAgICAgICAgIDxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJGaXJzdE5hbWUiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vQ3VycmVudEdpdmVuTmFtZSIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0idHJ1ZSIvPgogICAgICAgICAgICA8ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iRGF0ZU9mQmlydGgiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vRGF0ZU9mQmlydGgiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9InRydWUiLz4KICAgICAgICAgICAgPGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IkdlbmRlciIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbmF0dXJhbHBlcnNvbi9HZW5kZXIiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9ImZhbHNlIi8+CiAgICAgICAgICAgIDxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJQZXJzb25JZGVudGlmaWVyIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9uYXR1cmFscGVyc29uL1BlcnNvbklkZW50aWZpZXIiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9InRydWUiLz4KICAgICAgICAgICAgPGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IlBsYWNlT2ZCaXJ0aCIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbmF0dXJhbHBlcnNvbi9QbGFjZU9mQmlydGgiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9ImZhbHNlIi8+CiAgICAgICAgICAgIDxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJlSnVzdGljZUxlZ2FsUGVyc29uUm9sZSIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lPSJodHRwOi8vZS1qdXN0aWNlLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL2xlZ2FscGVyc29uL2VKdXN0aWNlTGVnYWxQZXJzb25Sb2xlIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSIvPgogICAgICAgICAgICA8ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iZUp1c3RpY2VOYXR1cmFsUGVyc29uUm9sZSIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lPSJodHRwOi8vZS1qdXN0aWNlLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vZUp1c3RpY2VOYXR1cmFsUGVyc29uUm9sZSIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz4KICAgICAgICA8L2VpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZXM+CiAgICA8L3NhbWwycDpFeHRlbnNpb25zPgogICAgPHNhbWwycDpSZXF1ZXN0ZWRBdXRobkNvbnRleHQgQ29tcGFyaXNvbj0ibWluaW11bSI+CiAgICAgICAgPHNhbWwyOkF1dGhuQ29udGV4dENsYXNzUmVmPmh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvTG9BL2xvdzwvc2FtbDI6QXV0aG5Db250ZXh0Q2xhc3NSZWY+CiAgICA8L3NhbWwycDpSZXF1ZXN0ZWRBdXRobkNvbnRleHQ+Cjwvc2FtbDJwOkF1dGhuUmVxdWVzdD4=";

    private HttpServletRequest mockHttpServletRequest;
    private SpecificConnectorRequestServlet specificConnectorRequestServlet;
    private SpecificCommunicationService mockSpecificConnectorCommunicationService;
    private ApplicationContext oldContext = null;
    private Properties properties;

    @Mock
    private ApplicationContext mockApplicationContext;

    @Mock
    private Collection mockCollection;

    @Mock
    private ICONNECTORService mockConnectorService;
    @Mock
    private AUCONNECTORUtil mockAuthConnectorUtil;
    @Mock
    private HttpServletResponse mockHttpServletResponse;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        mockSpecificConnectorCommunicationService();

        Mockito.when(mockApplicationContext.getBean(AUCONNECTORUtil.class)).thenReturn(mockAuthConnectorUtil);
        properties = Mockito.mock(Properties.class);
        Mockito.when(mockAuthConnectorUtil.getConfigs()).thenReturn(properties);

        mockHttpServletRequest = createMockHttpServletRequest();
        specificConnectorRequestServlet = new SpecificConnectorRequestServlet();
    }

    @After
    public void tearDown() {
        if (oldContext != null) {
            ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
            oldContext = null;
        }
    }

    /**
     * Test method for
     * {@link SpecificConnectorRequestServlet#getiLightRequest(HttpServletRequest, Collection)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetLightRequest() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, SpecificCommunicationException {
        final Method getLightRequestMethod = getLightRequestMethod();
        final ILightRequest mockLightRequest = Mockito.mock(ILightRequest.class);

        Mockito.when(mockSpecificConnectorCommunicationService.getAndRemoveRequest(anyString(), any())).thenReturn(mockLightRequest);

        getLightRequestMethod.invoke(specificConnectorRequestServlet, mockHttpServletRequest, mockCollection);
    }

    /**
     * Test method for
     * {@link SpecificConnectorRequestServlet#getiLightRequest(HttpServletRequest, Collection)}
     * when {@link ILightRequest} is null
     * <p>
     * Must fail.
     */
    @Test
    public void testGetLightRequestWithLightRequestNull() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(ConnectorError.class));

        final Method getLightRequestMethod = getLightRequestMethod();

        getLightRequestMethod.invoke(specificConnectorRequestServlet, mockHttpServletRequest, mockCollection);
    }

    /**
     * Test method for
     * {@link SpecificConnectorRequestServlet#getiLightRequest(HttpServletRequest, Collection)}
     * when {@link ConnectorError} is thrown inside
     * {@link SpecificConnectorRequestServlet#getiLightRequest(HttpServletRequest, Collection)} method
     * <p>
     * Must fail.
     */
    @Test
    public void testGetLightRequestWhenIllegalArgumentExceptionIsCaught() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SpecificCommunicationException {
        expectedException.expectCause(isA(ConnectorError.class));

        final Method getLightRequestMethod = getLightRequestMethod();

        Mockito.doThrow(IllegalArgumentException.class).when(mockSpecificConnectorCommunicationService).getAndRemoveRequest(anyString(), any());

        getLightRequestMethod.invoke(specificConnectorRequestServlet, mockHttpServletRequest, mockCollection);
    }

    /**
     * Test method for
     * {@link SpecificConnectorRequestServlet#getiLightRequest(HttpServletRequest, Collection)}
     * when {@link SpecificCommunicationException} is thrown inside {@link SpecificConnectorRequestServlet#getiLightRequest(HttpServletRequest, Collection)}
     * the errorMessage contains {@link IncomingLightRequestValidatorLoAComponent#ERROR_LIGHT_REQUEST_BASE} constant value from {@link IncomingLightRequestValidatorLoAComponent}
     * <p>
     * Must fail.
     */
    @Test
    public void testGetLightRequestWhenSpecificCommunicationExceptionIsCaught() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SpecificCommunicationException {
        expectedException.expectCause(isA(ConnectorError.class));

        final Method getLightRequestMethod = getLightRequestMethod();
        final String errorMessage = IncomingLightRequestValidatorLoAComponent.ERROR_LIGHT_REQUEST_BASE;

        Mockito.doThrow(new SpecificCommunicationException(errorMessage)).when(mockSpecificConnectorCommunicationService).getAndRemoveRequest(anyString(), any());

        getLightRequestMethod.invoke(specificConnectorRequestServlet, mockHttpServletRequest, mockCollection);
    }

    /**
     * Test method for
     * {@link SpecificConnectorRequestServlet#validateRelayState(ILightRequest)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidateRelayState() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method validateRelayStateMethod = SpecificConnectorRequestServlet.class.getDeclaredMethod("validateRelayState", ILightRequest.class);
        validateRelayStateMethod.setAccessible(true);

        final ILightRequest mockLightRequest = Mockito.mock(ILightRequest.class);
        Mockito.when(mockLightRequest.getRelayState()).thenReturn("relayState");

        validateRelayStateMethod.invoke(specificConnectorRequestServlet, mockLightRequest);
    }

    /**
     * Test method for
     * {@link SpecificConnectorRequestServlet#validateRelayState(ILightRequest)}
     * when relayState is invalid
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateRelayStateWithInvalidValue() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(ConnectorError.class));

        final Method validateRelayStateMethod = SpecificConnectorRequestServlet.class.getDeclaredMethod("validateRelayState", ILightRequest.class);
        validateRelayStateMethod.setAccessible(true);

        final ILightRequest mockLightRequest = Mockito.mock(ILightRequest.class);
        Mockito.when(mockLightRequest.getRelayState()).thenReturn("VALIDATION123456789123456789123456789123456789123456789123456789123456789123456789VALIDATION");

        validateRelayStateMethod.invoke(specificConnectorRequestServlet, mockLightRequest);
    }

    /**
     * Test method for
     * {@link SpecificConnectorRequestServlet#doPost(HttpServletRequest, HttpServletResponse)}
     * when SAMLRequest is valid
     * <p>
     * Must succeed.
     */
    @Test
    public void testDoPostWithValidSAMLRequest() throws ServletException, IOException, SpecificCommunicationException {
        final ILightRequest mockLightRequest = Mockito.mock(ILightRequest.class);
        final IRequestMessage mockIRequestMessage = Mockito.mock(IRequestMessage.class);

        mockServletContext();
        mockConnectorService("testRedirectUrl");
        mockSupportedAttributes();
        mockRequestMessage(mockIRequestMessage, "fakeId", "fakeServiceRedirectUrl");

        Mockito.when(mockSpecificConnectorCommunicationService.getAndRemoveRequest(anyString(), any())).thenReturn(mockLightRequest);

        byte[] samlRequest = EidasStringUtil.decodeBytesFromBase64(samlRequestBase64);
        Mockito.when(mockIRequestMessage.getMessageBytes()).thenReturn(samlRequest);

        specificConnectorRequestServlet.doPost(mockHttpServletRequest, mockHttpServletResponse);
    }

    /**
     * Test method for
     * {@link SpecificConnectorRequestServlet#doPost(HttpServletRequest, HttpServletResponse)}
     * when SAMLRequest is invalid
     * <p>
     * Must fail.
     */
    @Test
    public void testDoPostWithInvalidSAMLRequest() throws ServletException, IOException, SpecificCommunicationException {
        expectedException.expect(ConnectorError.class);

        final ILightRequest mockLightRequest = Mockito.mock(ILightRequest.class);
        final IRequestMessage mockIRequestMessage = Mockito.mock(IRequestMessage.class);

        mockServletContext();
        mockConnectorService("testRedirectUrl");
        mockSupportedAttributes();
        mockRequestMessage(mockIRequestMessage, "fakeId", "fakeServiceRedirectUrl");

        Mockito.when(mockSpecificConnectorCommunicationService.getAndRemoveRequest(anyString(), any())).thenReturn(mockLightRequest);
        Mockito.when(mockIRequestMessage.getMessageBytes()).thenReturn(new byte[0]);

        specificConnectorRequestServlet.doPost(mockHttpServletRequest, mockHttpServletResponse);
    }

    private Method getLightRequestMethod() throws NoSuchMethodException {
        final Method getLightRequestMethod = SpecificConnectorRequestServlet.class.getDeclaredMethod("getiLightRequest", HttpServletRequest.class, Collection.class);
        getLightRequestMethod.setAccessible(true);

        return getLightRequestMethod;
    }

    private void mockSpecificConnectorCommunicationService() {
        final String beanName = ConnectorBeanNames.SPECIFIC_CONNECTOR_COMMUNICATION_SERVICE.toString();
        mockSpecificConnectorCommunicationService = Mockito.mock(SpecificCommunicationService.class);
        Mockito.when(mockApplicationContext.getBean(beanName)).thenReturn(mockSpecificConnectorCommunicationService);
    }

    private HttpServletRequest createMockHttpServletRequest() {
        final MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(HttpMethod.POST.toString(), null);
        mockHttpServletRequest.setParameter(EidasParameterKeys.LIGHT_REQUEST.toString(), "Fake request");
        mockHttpServletRequest.setParameter(EidasParameterKeys.TOKEN.toString(), "fakeToken");

        return mockHttpServletRequest;
    }

    private ConnectorControllerService mockConnectorService(String assertionConsUrl) {
        final ConnectorControllerService mockConnectorControllerService = Mockito.mock(ConnectorControllerService.class);
        Mockito.when(mockConnectorControllerService.getConnectorService()).thenReturn(mockConnectorService);
        Mockito.when(mockConnectorControllerService.getAssertionConsUrl()).thenReturn(assertionConsUrl);
        Mockito.when(mockApplicationContext.getBean(ConnectorBeanNames.EIDAS_CONNECTOR_CONTROLLER.toString()))
                .thenReturn(mockConnectorControllerService);

        return mockConnectorControllerService;
    }

    private void mockSupportedAttributes() {
        final ProtocolEngineI mockProtocolEngineI = Mockito.mock(ProtocolEngineI.class);
        final ProtocolProcessorI mockProtocolProcessorI = Mockito.mock(ProtocolProcessorI.class);
        final ICONNECTORSAMLService mockICONNECTORSAMLService = Mockito.mock(ICONNECTORSAMLService.class);
        final AttributeDefinition[] expectedAttributeDefinitions = new AttributeDefinition[0];

        Mockito.when(mockConnectorService.getSamlService()).thenReturn(mockICONNECTORSAMLService);
        Mockito.when(mockICONNECTORSAMLService.getSamlEngine()).thenReturn(mockProtocolEngineI);
        Mockito.when(mockProtocolEngineI.getProtocolProcessor()).thenReturn(mockProtocolProcessorI);
        List<AttributeDefinition<?>> attributeDefinitionList = Arrays.stream(expectedAttributeDefinitions)
                .map(attribute -> (AttributeDefinition<?>) attribute)
                .collect(Collectors.toList());

        SortedSet<AttributeDefinition<?>> sortedAttributeDefinitionSet = new TreeSet<>(attributeDefinitionList);
        Mockito.when(mockProtocolProcessorI.getAllSupportedAttributes()).thenReturn(sortedAttributeDefinitionSet);
    }

    private void mockServletContext() throws ServletException {
        final ServletConfig mockServletConfig = Mockito.mock(ServletConfig.class);
        final ServletContext mockServletContext = Mockito.mock(ServletContext.class);
        final RequestDispatcher mockRequestDispatcher = Mockito.mock(RequestDispatcher.class);

        specificConnectorRequestServlet.init(mockServletConfig);

        Mockito.when(mockServletConfig.getServletContext()).thenReturn(mockServletContext);
        Mockito.when(mockServletContext.getRequestDispatcher(any())).thenReturn(mockRequestDispatcher);
    }

    private void mockRequestMessage(IRequestMessage mockIRequestMessage, String requestId, String destination) {
        final IAuthenticationRequest mockIAuthenticationRequest = Mockito.mock(IAuthenticationRequest.class);

        Mockito.when(mockConnectorService.getAuthenticationRequest(any(), any())).thenReturn(mockIRequestMessage);
        Mockito.when(mockIRequestMessage.getRequest()).thenReturn(mockIAuthenticationRequest);
        Mockito.when(mockIAuthenticationRequest.getId()).thenReturn(requestId);
        Mockito.when(mockIAuthenticationRequest.getDestination()).thenReturn(destination);
    }
}