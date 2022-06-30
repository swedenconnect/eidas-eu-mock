/*
* Copyright  (c) $today.year European Commission
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
package eu.eidas.config.impl;

import javax.xml.bind.annotation.XmlAttribute;

public class EIDASNodeConfFile {
    String id;
    String fileName;
    enum FileType{
        XML,
        PROPERTIES
    }

    @XmlAttribute(name="id", required = true, namespace="")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name="name", required = true, namespace="")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    String type;
    @XmlAttribute(name="type", required = true, namespace="")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof EIDASNodeConfFile){
            return this.id!=null && this.id.equals(((EIDASNodeConfFile)o).id);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return this.id==null?0:this.id.hashCode();
    }
}
