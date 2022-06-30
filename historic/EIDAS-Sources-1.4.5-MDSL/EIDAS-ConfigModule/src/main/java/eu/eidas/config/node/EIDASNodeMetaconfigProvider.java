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
package eu.eidas.config.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.eidas.config.impl.EIDASNodeConfFile;

/**
 * stores metadata information and performs operation on it
 */
public abstract class EIDASNodeMetaconfigProvider {
    private Map<String, EIDASNodeParameterMeta> parameterMap=new HashMap<String, EIDASNodeParameterMeta>();
    private List<String> parameterOrder=new ArrayList<String>();
    private Map<String, List<EIDASNodeParameterMeta>> categorizedParameters=new HashMap<String, List<EIDASNodeParameterMeta>>();
    private List<EIDASNodeParameterCategory> categories=new ArrayList<EIDASNodeParameterCategory>();
    public void addMetadata(String paramName, EIDASNodeParameterMeta parameter){
        parameterMap.put(paramName, parameter);
        parameterOrder.add(paramName);
    }
    public List<EIDASNodeParameterCategory> getCategories(){
        return categories;
    }
    public EIDASNodeParameterMeta getMetadata(String parameterName){
        return parameterMap.get(parameterName);
    }
    public List<EIDASNodeParameterMeta> getCategoryParameter(String categoryName){
        return getCategorizedParameters().get(categoryName);
    }

    public Map<String, List<EIDASNodeParameterMeta>> getCategorizedParameters() {
        if(categorizedParameters.isEmpty()){
            synchronized (EIDASNodeMetaconfigProvider.class){
                if(categorizedParameters.isEmpty()) {
                    for(EIDASNodeParameterCategory c:categories){
                        categorizedParameters.put(c.getName(), new ArrayList<EIDASNodeParameterMeta>());
                    }

                    for(String paramName:parameterOrder){
                        EIDASNodeParameterMeta p = parameterMap.get(paramName);
                        if(p!=null){
                            for(String categoryName:p.getCategories()){
                                if(categorizedParameters.containsKey(categoryName)){
                                    categorizedParameters.get(categoryName).add(p);
                                }
                            }

                        }
                    }
                }
            }
        }
        return categorizedParameters;
    }

    public void setCategorizedParameters(Map<String, List<EIDASNodeParameterMeta>> categorizedParameter) {
        this.categorizedParameters = categorizedParameter;
    }
    public abstract EIDASNodeConfFile getDefaultConfFile();
}
