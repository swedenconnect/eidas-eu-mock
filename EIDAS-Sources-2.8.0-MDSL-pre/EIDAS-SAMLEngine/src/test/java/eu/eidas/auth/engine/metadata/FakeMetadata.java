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

package eu.eidas.auth.engine.metadata;

import eu.eidas.auth.engine.metadata.impl.EidasMetadataParameters;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataRoleParameters;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;

import java.util.Set;

public class FakeMetadata {

    public static EidasMetadataParameters proxyService() {
        final EidasMetadataParameters eidasMetadataParameters = shared();
        final EidasMetadataRoleParameters eidasMetadataRoleParameters = new EidasMetadataRoleParameters();
        eidasMetadataRoleParameters.setRole(MetadataRole.IDP);
        eidasMetadataParameters.setRoleDescriptors(Set.of(eidasMetadataRoleParameters));
        return eidasMetadataParameters;
    }

    public static EidasMetadataParameters connector() {
        final EidasMetadataParameters eidasMetadataParameters = shared();
        final EidasMetadataRoleParameters eidasMetadataRoleParameters = new EidasMetadataRoleParameters();
        eidasMetadataRoleParameters.setRole(MetadataRole.SP);
        eidasMetadataParameters.setRoleDescriptors(Set.of(eidasMetadataRoleParameters));
        return eidasMetadataParameters;
    }

    public static EidasMetadataParameters shared() {
        final EidasMetadataParameters eidasMetadataParameters = new EidasMetadataParameters();
        // add common test attributes here
        return eidasMetadataParameters;
    }
}
