/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.eidas.auth.engine.metadata;

import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;

/**
 * OrganizationData element in Metadata
 */
public class OrganizationData implements Serializable {

    private final String name;
    private final String displayName;
    private final String url;

    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder {

        private String name;
        private String displayName;
        private String url;

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            name = copy.name;
            displayName = copy.displayName;
            url = copy.url;
        }

        public Builder(@Nonnull OrganizationData copy) {
            name = copy.name;
            displayName = copy.displayName;
            url = copy.url;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder displayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder url(final String url) {
            this.url = url;
            return this;
        }

        @Nonnull
        public OrganizationData build() {
            return new OrganizationData(this);
        }

    }

    private OrganizationData(@Nonnull Builder builder) {
        name = StringUtils.isNotBlank(builder.name) ? builder.name : "";
        displayName = StringUtils.isNotBlank(builder.displayName) ? builder.displayName : "";
        url = StringUtils.isNotBlank(builder.url) ? builder.url : "";
    }

    OrganizationData(@Nonnull OrganizationData copy) {
        name = copy.name;
        displayName = copy.displayName;
        url = copy.url;
    }

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull OrganizationData copy) {
        return new Builder(copy);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrl() {
        return url;
    }

}

