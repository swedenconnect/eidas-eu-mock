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
package eu.eidas.config.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * persistence for nodemetadata
 */
@XmlRootElement(name = "NODEMetadata")
@XmlAccessorType(XmlAccessType.NONE)
public class EIDASNodeMetaconfigHolderImpl {
    private CategoryListImpl categories;
    private EIDASNodeMetaconfigListImpl nodeMetadataList;
    private FileListImpl fileList;
    @XmlElement(name = "categories", type=CategoryListImpl.class)
    public CategoryListImpl getCategoryList(){
        return categories;
    }
    public void setCategoryList(CategoryListImpl categories){
        this.categories=categories;
    }

    @XmlElement(name = "files", type=FileListImpl.class)
    public FileListImpl getFileList(){
        return fileList;
    }
    public void setFileList(FileListImpl fileList){
        this.fileList=fileList;
    }

    @XmlElement(name = "parameters", type=EIDASNodeMetaconfigListImpl.class)
    public EIDASNodeMetaconfigListImpl getNodeMetadataList(){
        return nodeMetadataList;
    }
    public void setNodeMetadataList(EIDASNodeMetaconfigListImpl nodeMetadataList){
        this.nodeMetadataList=nodeMetadataList;
    }
}
