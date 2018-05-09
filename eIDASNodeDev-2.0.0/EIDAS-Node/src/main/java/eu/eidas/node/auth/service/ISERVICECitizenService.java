/*
 * Copyright (c) 2017 by European Commission
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
package eu.eidas.node.auth.service;

import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;

import javax.annotation.Nonnull;

/**
 * Interface that supplies methods for processing citizen-related matters.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com,
 *         luis.felix@multicert.com, hugo.magalhaes@multicert.com,
 *         paulo.ribeiro@multicert.com
 * @version $Revision: 1.28 $, $Date: 2010-11-18 23:17:50 $
 */
public interface ISERVICECitizenService {

    /**
     * Checks that all mandatory values are present in the given attributes otherwise throws an Exception.
     *
     * @throws EIDASServiceException if the attributes are not missing a mandatory attribute
     */
    void checkMandatoryAttributes(@Nonnull ImmutableAttributeMap attributes) throws EIDASServiceException;

    /**
     * Checks whether the attribute map satifisfies the rule of representation, otherwise thorows an Exception.
     *
     * @throws EIDASServiceException if the attributes are not missing a mandatory attribute
     */
    void checkRepresentativeAttributes(@Nonnull ImmutableAttributeMap attributes) throws EIDASServiceException;
}
