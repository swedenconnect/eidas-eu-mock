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
package eu.eidas.samlengineconfig;

import java.util.Properties;

/**
 * ConfigurationParameter referencing a set of properties
 */
public class PropsParameter extends ConfigurationParameter {
    String name;
    String fileName;
    Properties props;
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name=name;
    }

    @Override
    public Object getValue() {
        return props;
    }

    @Override
    public void setValue(Object value) {
        if(value instanceof Properties){
            props=(Properties)value;
        }
    }

    @Override
    public String getStringValue() {
        return getFileName();
    }

    public String getFileName(){
        return fileName;
    }
    public void setFileName(String fileName){
        this.fileName=fileName;
    }
}
