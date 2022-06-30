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
 * a contact in a piece of metadata
 */
public class ContactData implements Serializable {

    private final String email;
    private final String company;
    private final String givenName;
    private final String surName;
    private final String phone;

    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder {
        private String email;
        private String company;
        private String givenName;
        private String surName;
        private String phone;

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            email = copy.email;
            company = copy.company;
            givenName = copy.givenName;
            surName = copy.surName;
            phone = copy.phone;
        }

        public Builder(@Nonnull ContactData copy) {
            email = copy.email;
            company = copy.company;
            givenName = copy.givenName;
            surName = copy.surName;
            phone = copy.phone;
        }

        public Builder email(final String email) {
            this.email = email;
            return this;
        }

        public Builder company(final String company) {
            this.company = company;
            return this;
        }

        public Builder givenName(final String givenName) {
            this.givenName = givenName;
            return this;
        }

        public Builder surName(final String surName) {
            this.surName = surName;
            return this;
        }

        public Builder phone(final String phone) {
            this.phone = phone;
            return this;
        }

        @Nonnull
        public ContactData build() {
            return new ContactData(this);
        }

    }

    private ContactData(@Nonnull Builder builder) {
        email = StringUtils.isNotBlank(builder.email) ? builder.email : "";
        company = StringUtils.isNotBlank(builder.company) ? builder.company : "";
        givenName = StringUtils.isNotBlank(builder.givenName) ? builder.givenName : "";
        surName = StringUtils.isNotBlank(builder.surName) ? builder.surName : "";
        phone = StringUtils.isNotBlank(builder.phone) ? builder.phone : "";
    }

    ContactData(@Nonnull ContactData copy) {
        email = copy.email;
        company = copy.company;
        givenName = copy.givenName;
        surName = copy.surName;
        phone = copy.phone;
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
    public static Builder builder(@Nonnull ContactData copy) {
        return new Builder(copy);
    }

    public String getEmail() {
        return email;
    }

    public String getCompany() {
        return company;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getSurName() {
        return surName;
    }

    public String getPhone() {
        return phone;
    }

}
