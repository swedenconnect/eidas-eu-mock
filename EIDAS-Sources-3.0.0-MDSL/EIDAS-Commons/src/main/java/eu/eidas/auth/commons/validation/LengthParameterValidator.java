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

package eu.eidas.auth.commons.validation;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasParameters;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Length parameter validator.
 */
public class LengthParameterValidator extends BaseValueValidator<Object> {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LengthParameterValidator.class);

    /**
     * Max prefix.
     */
    private static final String MAX_PARAM_PREFIX = "max.";

    /**
     * param's size prefix to get max param size.
     */
    private static final String MAX_PARAM_SUFFIX = ".size";

    private String paramName;

    private String getParamName() {
        return paramName;
    }

    @Override
    protected  boolean isValidValue(Object value) {
        if (value == null) {
            LOG.info("ERROR : Invalid parameter [" + getParamName() + "]: value is null");
            return false;
        } else if (value instanceof String) {
            return isValidValue((String) value);
        } else if (value instanceof Number) {
            Number valueAsNumber = (Number) value;
            return isValidLength(valueAsNumber.intValue());
        } else {
            throw new UnsupportedOperationException("LengthParameterValidator doesn't handle this type of value: " + value.getClass());
        }
    }

    private boolean isValidValue(String value) {
        if (isValueBlank(value)) {
            return false;
        }
        return isValidLength(value.length());
    }

    private boolean isValidLength(int length) {
        final Optional<Integer> maxParamSize = getMaxParamSize();
        if (!maxParamSize.isPresent()) {
            return false;
        }
        if (length > maxParamSize.get()) {
            LOG.info("ERROR : Invalid parameter [" + getParamName() + "] too long value");
            LOG.info("Check the value for the parameter " + getMaxParamConfName() + " in the configuration file "
                    + EidasParameters.getPropertiesFilename());
            return false;
        }
        return true;
    }

    private boolean isValueBlank(String value) {
        if (StringUtils.isBlank(value)) {
            LOG.info("ERROR : Invalid parameter [" + getParamName() + "]: value is blank");
            return true;
        }
        return false;
    }

    /**
     * Method to get the maximum size of the attribute with paramName.
     * @return the maximum size for this param as optional or an empty optional if the max size is not found.
     */
    protected Optional<Integer> getMaxParamSize() {
        String paramConf = getMaxParamConfName();
        final String paramSizeStr = EidasParameters.get(paramConf);
        // Checking if the parameter size exists and if it's numeric
        if (StringUtils.isNumeric(paramSizeStr)) {
            final int maxParamSize = Integer.parseInt(paramSizeStr);
            return  Optional.of(maxParamSize);
        } else {
            LOG.info("ERROR : Missing \"" + paramConf + "\" configuration in the "
                    + EidasParameters.getPropertiesFilename() + " configuration file");
            return Optional.empty();
        }
    }

    private String getMaxParamConfName() {
        return MAX_PARAM_PREFIX + getParamName() + MAX_PARAM_SUFFIX;
    }


    /**
     * Method to get a LengthParameterValidator builder.
     * @return a new LengthParameterValidator builder.
     */
    public static LengthParameterValidator.Builder Builder() {
        return new LengthParameterValidator.Builder();
    }

    /**
     * Method to get LengthParameterValidator
     * @param paramKey the EidasParameterKey for which the max size should be check
     * @return the LengthParameterValidator
     */
    public static LengthParameterValidator forParam(EidasParameterKeys paramKey) {
        return Builder().paramName(paramKey).build();
    }

    /**
     * Builder class for a Length based validator.
     */
    public static class Builder extends BaseValueValidator.Builder<LengthParameterValidator> {

        private String paramName;

        public Builder() {
            super(LengthParameterValidator::new);
        }

        /**
         * Set the parameter name
         * @param paramName the parameter name.
         * @return this builder
         */
        public Builder paramName(String paramName) {
            this.paramName = paramName;
            return this;
        }

        /**
         * Set the parameter name
         * @param paramName the eidas parameter key for which the value will be used as parameter name.
         * @return this builder
         */
        public Builder paramName(EidasParameterKeys paramName) {
            this.paramName = paramName.getValue();
            return this;
        }

        /**
         * Constructs a ParameterValidator.
         * @return the build ParameterValidator
         */
        public LengthParameterValidator build() {
            assert StringUtils.isNotBlank(this.paramName) : "Parameter name cannot be null or empty";
            LengthParameterValidator validator = super.build();
            validator.paramName = this.paramName;
            return validator;
        }
    }

}
