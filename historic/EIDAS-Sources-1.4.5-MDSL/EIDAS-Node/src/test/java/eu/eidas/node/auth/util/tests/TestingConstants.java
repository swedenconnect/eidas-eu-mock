/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.node.auth.util.tests;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;

/**
 * This enum class contains all the EidasNode testing constants.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com,
 *         luis.felix@multicert.com,
 * @version $Revision: $, $Date: $
 */
public enum TestingConstants {
  /**
   * Represents the 'all' constant.
   */
  ALL_CONS("all"),
  /**
   * Represents the 'Idade' constant.
   */
  ALLOWED_ATTRIBUTES_CONS("Idade;"),
  /**
   * Represents the 'ASSERTION_URL' constant.
   */
  ASSERTION_URL_CONS("ASSERTION_URL"),
  /**
   * Represents the 'ASSERTION_URL' constant.
   */
  CITIZEN_COUNTRY_CODE_CONS("PT"),
  /**
   * Represents the 'CONNECTOR-METADATA-URL' constant.
   */
  CONNECTOR_METADATA_URL_CONS("CONNECTOR-METADATA-URL"),
  /**
   * Represents the 'DESTINATION_CONS' constant.
   */
  DESTINATION_CONS("SP-URL"),
  /**
   * Represents the '' constant.
   */
  EMPTY_CONS(""),
  /**
   * Represents the 'ERROR_CODE' constant.
   */
  ERROR_CODE_CONS("ERROR_CODE"),
  /**
   * Represents the 'STATUS_CODE' constant.
   */
  ERROR_STATUS_CONS("STATUS_CODE"),
  /**
   * Represents the 'ERROR_MESSAGE' constant.
   */
  ERROR_MESSAGE_CONS("ERROR_MESSAGE"),
  /**
   * Represents the '127.0.0.1' constant.
   */
  IP_ADDRESS("127.0.0.1"),
  /**
   * Represents the 'LOCAL-Connector' constant.
   */
  ISSUER_CONS("LOCAL-Connector"),
  /**
   * Represents the 'LOCAL' constant.
   */
  LOCAL_CONS("LO"),
  /**
   * Represents the LevelOfAssurance.LOW stringValue.
   */
  LEVEL_OF_ASSURANCE_LOW_CONS(LevelOfAssurance.LOW.stringValue()),
  /**
   * Represents the LevelOfAssurance.HIGH stringValue.
   */
  LEVEL_OF_ASSURANCE_HIGH_CONS(LevelOfAssurance.HIGH.stringValue()),
   /**
   * Represents the 'LOCAL_DUMMY_URL' constant.
   */
  LOCAL_URL_CONS("LOCAL_DUMMY_URL"),
  /**
   * Represents the 'maxQaa' constant.
   */
  MAX_QAA_CONS("4"),
  /**
   * Represents the 'minQaa' constant.
   */
  MIN_QAA_CONS("1"),
  /**
   * Represents the '1' constant.
   */
  ONE_CONS("1"),
  /**
   * Represents the 'PROVIDERNAME_CERT' constant.
   */
  PROVIDERNAME_CERT_CONS("PROVIDERNAME_CERT"),
  /**
   * Represents the 'SP_PROV' constant.
   */
  PROVIDERNAME_CONS("SP_PROV"),
  /**
   * Represents the 'REQUEST_CITIZEN_COUNTRY_CODE' constant.
   */
  REQUEST_CITIZEN_COUNTRY_CODE_CONS("REQUEST_CITIZEN_COUNTRY_CODE"),
  /**
   * Represents the 'f5e7e0f5-b9b8-4256-a7d0-4090141b326d' constant.
   */
  REQUEST_ID_CONS("f5e7e0f5-b9b8-4256-a7d0-4090141b326d"),
  /**
   * Represents the 'REQUEST_DESTINATION' constant.
   */
  REQUEST_DESTINATION_CONS("REQUEST_DESTINATION"),
  /**
   * Represents the '_fHhEts9JPpseTQXGCaSwyqNX-waO6lnphoG7xTOe6c0Tw10oDGIlsPLkh97Uiq' const.
   */
  RESPONSE_ID_CONS("_fHhEts9JPpseTQXGCaSwyqNX-waO6lnphoG7xTOe6c0Tw10oDGIlsPLkh97Uiq"),
  /**
   * Represents the 'REQUEST_ISSUER' constant.
   */
  REQUEST_ISSUER_CONS("REQUEST_ISSUER"),
  /**
   * Represents the 'LevelOfAssurance.LOW.stringValue()' value.
   */
  REQUEST_LEVEL_OF_ASSURANCE_LOW_CONS(LevelOfAssurance.LOW.stringValue()),
  /**
   * Represents the 'EIDASStatusCode.SUCCESS_URI.toString()' value.
   */
  RESPONSE_STATUS_CODE_SUCCESS_CONS(EIDASStatusCode.SUCCESS_URI.toString()),
  /**
   * Represents the 'RESPONSE_ISSUER' value.
   */
  RESPONSE_ISSUER_CONS("RESPONSE_ISSUER"),
  /**
   * Represents the 'SERVICE-METADATA-URL' constant.
   */
  SERVICE_METADATA_URL_CONS("SERVICE-METADATA-URL"),
  /**
   * Represents the 'SP_RELAY' constant.
   */
  SP_RELAY_STATE_CONS("SP_RELAY"),
  /**
   * Represents the 'qaaLevel' constant.
   */
  QAALEVEL_CONS("3"),
  /**
   * Represents the 'samlId' constant.
   */
    SAML_ID_CONS("_12341234123412341234123412341234"),
  /**
   * Represents the 'SAML_ISSUER_CONS' constant.
   */
  SAML_ISSUER_CONS("http://ConnectorMetadata"),
  /**
   * Represents the 'samlInstance' constant.
   */
  SAML_INSTANCE_CONS("Service"),
  /**
   * Represents the 'SAML_TOKEN_CONS' constant.
   */
  SAML_TOKEN_CONS("<saml>...</saml>"),
  /**
   * Represents the 'spid' constant.
   */
  SPID_CONS("SP"),
  /**
   * Represents the 'SP_APP' constant.
   */
  SP_APPLICATION_CONS("SP_APP"),
  /**
   * Represents the 'SP_INST' constant.
   */
  SP_INSTITUTION_CONS("SP_INST"),
  /**
   * Represents the 'SP-REQUEST-DESTINATION-URL' constant.
   */
  SP_REQUEST_DESTINATION_CONS("SP-REQUEST-DESTINATION-URL"),
  /**
   * Represents the 'SP-REQUEST-ISSUER' constant.
   */
  SP_REQUEST_ISSUER_CONS("SP-REQUEST-ISSUER"),
  /**
   * Represents the 'SP_SECT' constant.
   */
  SP_SECTOR_CONS("SP_SECT"),
  /**
   * Represents the 'public' constant.
   */
  SP_TYPE_PUBLIC_CONS("public"),
  /**
   * Represents the 'SUB_ERROR_CODE' constant.
   */
  SUB_ERROR_CODE_CONS("SUB_ERROR_CODE"),
  /**
   * Represents the 'USER_IP_CONS' constant.
   */
  USER_IP_CONS("10.10.10.10"),
  /**
   * Represents the 'true' constant.
   */
  TRUE_CONS("true"),
  /**
   * Represents the '2' constant.
   */
  TWO_CONS("2"),
  /**
   * Represents a skew time of 0
   */
  SKEW_ZERO_CONS("0"),
  /**
   * Represents the 'false' constant.
   */
  FALSE_CONS("false"),
  ;

  /**
   * Represents the constant's value.
   */
  private final transient String value;

  /**
   * Solo Constructor.
   *
   * @param nValue The Constant value.
   */
  private TestingConstants(final String nValue) {
    this.value = nValue;
  }

  /**
   * Return the Constant Value.
   *
   * @return The constant value.
   */
  @Override
  public String toString() {
    return value;
  }

  /**
   * Return the Constant integer Value.
   *
   * @return The constant int value.
   */
  public int intValue() {
    return Integer.valueOf(value).intValue();
  }

  /**
   * Return the SP Constant plus ".qaalevel" string.
   *
   * @return The SP constant value plus '.qaalevel" string.
   */
  public String getQaaLevel() {
    return SPID_CONS.toString() + ".qaalevel";
  }
}
