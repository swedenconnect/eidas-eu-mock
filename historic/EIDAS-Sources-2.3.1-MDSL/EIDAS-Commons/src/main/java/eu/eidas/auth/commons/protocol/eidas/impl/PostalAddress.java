/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.commons.protocol.eidas.impl;

import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * PostalAddress as per CORA ISA Vocabulary v0.3
 */
public final class PostalAddress implements Serializable {

    /**
     * Builder pattern for the {@link PostalAddress} class.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <br>
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder {

        private String poBox;

        private String locatorDesignator;

        private String locatorName;

        private String cvAddressArea;

        private String thoroughfare;

        private String postName;

        private String adminUnitFirstLine;

        private String adminUnitSecondLine;

        private String postCode;

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            Preconditions.checkNotNull(copy, "copy");
            poBox = copy.poBox;
            locatorDesignator = copy.locatorDesignator;
            locatorName = copy.locatorName;
            cvAddressArea = copy.cvAddressArea;
            thoroughfare = copy.thoroughfare;
            postName = copy.postName;
            adminUnitFirstLine = copy.adminUnitFirstLine;
            adminUnitSecondLine = copy.adminUnitSecondLine;
            postCode = copy.postCode;

        }

        public Builder(@Nonnull PostalAddress copy) {
            Preconditions.checkNotNull(copy, "copy");
            poBox = copy.poBox;
            locatorDesignator = copy.locatorDesignator;
            locatorName = copy.locatorName;
            cvAddressArea = copy.cvAddressArea;
            thoroughfare = copy.thoroughfare;
            postName = copy.postName;
            adminUnitFirstLine = copy.adminUnitFirstLine;
            adminUnitSecondLine = copy.adminUnitSecondLine;
            postCode = copy.postCode;
        }

        public Builder poBox(final String poBox) {
            this.poBox = poBox;
            return this;
        }

        public Builder locatorDesignator(final String locatorDesignator) {
            this.locatorDesignator = locatorDesignator;
            return this;
        }

        public Builder locatorName(final String locatorName) {
            this.locatorName = locatorName;
            return this;
        }

        public Builder cvAddressArea(final String cvAddressArea) {
            this.cvAddressArea = cvAddressArea;
            return this;
        }

        public Builder thoroughfare(final String thoroughfare) {
            this.thoroughfare = thoroughfare;
            return this;
        }

        public Builder postName(final String postName) {
            this.postName = postName;
            return this;
        }

        public Builder adminUnitFirstLine(final String adminUnitFirstLine) {
            this.adminUnitFirstLine = adminUnitFirstLine;
            return this;
        }

        public Builder adminUnitSecondLine(final String adminUnitSecondLine) {
            this.adminUnitSecondLine = adminUnitSecondLine;
            return this;
        }

        public Builder postCode(final String postCode) {
            this.postCode = postCode;
            return this;
        }

        private void validate() throws IllegalArgumentException {
            //TODO with validator
        }

        @Nonnull
        public PostalAddress build() {
            validate();
            return new PostalAddress(this);
        }
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
    public static Builder builder(@Nonnull PostalAddress copy) {
        return new Builder(copy);
    }

    private static final long serialVersionUID = 7410409986167152563L;

    @Nullable
    private final String poBox;

    @Nullable
    private final String locatorDesignator;

    @Nullable
    private final String locatorName;

    @Nullable
    private final String cvAddressArea;

    @Nullable
    private final String thoroughfare;

    @Nullable
    private final String postName;

    @Nullable
    private final String adminUnitFirstLine;

    @Nullable
    private final String adminUnitSecondLine;

    @Nullable
    private final String postCode;


    private PostalAddress(@Nonnull Builder builder) {
        poBox = builder.poBox;
        locatorDesignator = builder.locatorDesignator;
        locatorName = builder.locatorName;
        cvAddressArea = builder.cvAddressArea;
        thoroughfare = builder.thoroughfare;
        postName = builder.postName;
        adminUnitFirstLine = builder.adminUnitFirstLine;
        adminUnitSecondLine = builder.adminUnitSecondLine;
        postCode = builder.postCode;
    }

    @Nullable
    public String getPoBox() {
        return poBox;
    }

    @Nullable
    public String getLocatorDesignator() {
        return locatorDesignator;
    }

    @Nullable
    public String getLocatorName() {
        return locatorName;
    }

    @Nullable
    public String getCvAddressArea() {
        return cvAddressArea;
    }

    @Nullable
    public String getThoroughfare() {
        return thoroughfare;
    }

    @Nullable
    public String getPostName() {
        return postName;
    }

    @Nullable
    public String getAdminUnitFirstLine() {
        return adminUnitFirstLine;
    }

    @Nullable
    public String getAdminUnitSecondLine() {
        return adminUnitSecondLine;
    }

    @Nullable
    public String getPostCode() {
        return postCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostalAddress that = (PostalAddress) o;

        if (poBox != null ? !poBox.equals(that.poBox) : that.poBox != null) {
            return false;
        }
        if (locatorDesignator != null ? !locatorDesignator.equals(that.locatorDesignator)
                                      : that.locatorDesignator != null) {
            return false;
        }
        if (locatorName != null ? !locatorName.equals(that.locatorName) : that.locatorName != null) {
            return false;
        }
        if (cvAddressArea != null ? !cvAddressArea.equals(that.cvAddressArea) : that.cvAddressArea != null) {
            return false;
        }
        if (thoroughfare != null ? !thoroughfare.equals(that.thoroughfare) : that.thoroughfare != null) {
            return false;
        }
        if (postName != null ? !postName.equals(that.postName) : that.postName != null) {
            return false;
        }
        if (adminUnitFirstLine != null ? !adminUnitFirstLine.equals(that.adminUnitFirstLine)
                                       : that.adminUnitFirstLine != null) {
            return false;
        }
        if (adminUnitSecondLine != null ? !adminUnitSecondLine.equals(that.adminUnitSecondLine)
                                        : that.adminUnitSecondLine != null) {
            return false;
        }
        return postCode != null ? postCode.equals(that.postCode) : that.postCode == null;

    }

    @Override
    public int hashCode() {
        int result = poBox != null ? poBox.hashCode() : 0;
        result = 31 * result + (locatorDesignator != null ? locatorDesignator.hashCode() : 0);
        result = 31 * result + (locatorName != null ? locatorName.hashCode() : 0);
        result = 31 * result + (cvAddressArea != null ? cvAddressArea.hashCode() : 0);
        result = 31 * result + (thoroughfare != null ? thoroughfare.hashCode() : 0);
        result = 31 * result + (postName != null ? postName.hashCode() : 0);
        result = 31 * result + (adminUnitFirstLine != null ? adminUnitFirstLine.hashCode() : 0);
        result = 31 * result + (adminUnitSecondLine != null ? adminUnitSecondLine.hashCode() : 0);
        result = 31 * result + (postCode != null ? postCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer();
        if (poBox != null)
            ret.append("poBox: "+poBox+"\n");
        if (locatorDesignator != null)
            ret.append("locatorDesignator: "+locatorDesignator+"\n");
        if (locatorName != null)
            ret.append("locatorName: "+locatorName+"\n");
        if (cvAddressArea != null)
            ret.append("cvAddressArea: "+cvAddressArea+"\n");
        if (thoroughfare != null)
            ret.append("thoroughfare: "+thoroughfare+"\n");
        if (postName != null)
            ret.append("postName: "+postName+"\n");
        if (adminUnitFirstLine != null)
            ret.append("adminUnitFirstLine: "+adminUnitFirstLine+"\n");
        if (adminUnitSecondLine != null)
            ret.append("adminUnitSecondLine: "+adminUnitSecondLine+"\n");
        if (postCode != null)
            ret.append("postCode: "+postCode+"\n");
        return ret.toString();
    }

    /**
     * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
     * <p>
     * Used upon de-serialization, not serialization.
     * <p>
     * The state of this class is transformed back into the class it represents.
     */
    private Object readResolve() throws ObjectStreamException {
        return new Builder(this).build();
    }
}
