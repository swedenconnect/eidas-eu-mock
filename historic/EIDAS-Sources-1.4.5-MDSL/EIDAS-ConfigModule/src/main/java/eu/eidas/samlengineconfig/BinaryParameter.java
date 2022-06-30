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

public class BinaryParameter extends ConfigurationParameter{
    String name;
    byte[] value;
    String url;
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
        //TODO may be changed to provide lazily the binary contents
        //also may provide a cache
        if(value==null){
            return null;
        }else{
            return value.clone();
        }
    }

    @Override
    public void setValue(Object value) {
        this.value=value instanceof byte[]?(byte[])value:null;
    }

    @Override
    public String getStringValue() {
        return "";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
