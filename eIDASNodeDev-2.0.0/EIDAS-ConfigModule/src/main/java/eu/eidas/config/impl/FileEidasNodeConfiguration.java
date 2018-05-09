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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.config.EIDASNodeConfiguration;
import eu.eidas.config.impl.samlmetadata.MetadataRepositoryImpl;
import eu.eidas.config.node.EIDASNodeCountry;
import eu.eidas.config.node.EIDASNodeMetaconfigProvider;
import eu.eidas.config.node.EIDASNodeParameter;
import eu.eidas.config.samlmetadata.MetadataRepository;
import eu.eidas.samlengineconfig.impl.CertificateManagerConfigurationImpl;
import eu.eidas.samlengineconfig.impl.tools.EidasConfigManagerUtil;

/**
 *
 */
public class FileEidasNodeConfiguration extends EIDASNodeConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(FileEidasNodeConfiguration.class.getName());
    Properties eidasProperties;

    private static final String COUNTRY_PREFIX="service";
    private static final String COUNTRY_ID_SUFFIX=".id";
    private static final String COUNTRY_NAME_SUFFIX=".name";
    private static final String COUNTRY_URL_SUFFIX=".url";
    private static final String COUNTRY_METADATAURL_SUFFIX=".metadata.url";
    private static final String COUNTRY_SKEW_SUFFIX=".skew";

    private final static String EIDAS_SERVICE_NUMBER_NAME="service.number";
    private final static String SAML_ENGINE_REPOSITORY_URL="engine.repo";
    @Override
    public void load() {
        parameters=new HashMap<String, EIDASNodeParameter>();
        eidasProperties = new Properties();
        if(metadataProvider instanceof EIDASNodeMetaconfigProviderImpl) {
            EIDASNodeMetaconfigProviderImpl metadataProviderImpl=(EIDASNodeMetaconfigProviderImpl)metadataProvider;
            for(EIDASNodeConfFile f:metadataProviderImpl.getFileList()){
                Properties p=new Properties();
                if(EIDASNodeConfFile.FileType.XML.toString().equalsIgnoreCase(f.getType())) {
                    p=((FileConfigurationRepository) repository).loadPropertiesFromXML(f.getFileName());
                }else if(EIDASNodeConfFile.FileType.PROPERTIES.toString().equalsIgnoreCase(f.getType())) {
                    p=((FileConfigurationRepository) repository).loadPropsFromTextFile(f.getFileName());
                }
                loadParametersMap(p,f);
                for(String key:p.stringPropertyNames()){
                    eidasProperties.put(key, p.getProperty(key));
                }
            }
            loadCountries();
        }
    }

    @Override
    public Properties getEidasProperties(){
        return eidasProperties;
    }

    private void loadParametersMap(Properties properties, EIDASNodeConfFile sourceFile){
        Iterator iterator=properties.keySet().iterator();
        while(iterator.hasNext()){
            EIDASNodeParameter p=new EIDASNodeParameter();
            p.setName(iterator.next().toString());
            p.setValue(properties.getProperty(p.getName()));
            EIDASParameterMetaImpl metadata = (EIDASParameterMetaImpl)metadataProvider.getMetadata(p.getName());
            if(metadata==null){
                metadata = new EIDASParameterMetaImpl();
                metadata.setName(p.getName());
                metadataProvider.addMetadata(p.getName(), metadata);
            }
            metadata.setSourceFile(sourceFile);
            p.setMetadata(metadata);
            parameters.put(p.getName(), p);
        }
    }

    private void loadCountries(){
        String eidasServiceNumberValue=eidasProperties.getProperty(EIDAS_SERVICE_NUMBER_NAME);
        if(eidasServiceNumberValue==null || eidasServiceNumberValue.isEmpty()){
            LOG.info("ERROR : Incorrect number of countries in eidas.xml");
        }
        int eidasServiceNumber = 0;
        try {
            eidasServiceNumber = Math.abs(Integer.parseInt(eidasServiceNumberValue));
        }catch (NumberFormatException nfe){
            LOG.info("ERROR : Incorrect number of countries in eidas.xml {}", nfe);
        }
        countries=new ArrayList<EIDASNodeCountry>(eidasServiceNumber);
        for(int i=1;i<=eidasServiceNumber;i++){
            String countryID=COUNTRY_PREFIX+i;
            String countryId=eidasProperties.getProperty(countryID+COUNTRY_ID_SUFFIX);
            String countryName=eidasProperties.getProperty(countryID+COUNTRY_NAME_SUFFIX);
            String countryUrl=eidasProperties.getProperty(countryID+COUNTRY_URL_SUFFIX);
            String countryMetadataUrl=eidasProperties.getProperty(countryID+COUNTRY_METADATAURL_SUFFIX);
            String countrySkewTime=eidasProperties.getProperty(countryID+COUNTRY_SKEW_SUFFIX);
            if(StringUtils.isBlank(countryId)){
                LOG.info("ERROR : Country in eidas.xml countryId={1}, countryName={2}, url={3}, skewTime={4}", countryId, countryName, countryUrl, countrySkewTime);
                break;
            }
            EIDASNodeCountry country=new EIDASNodeCountry(countryId, countryName, countryUrl, countrySkewTime);
            country.setServiceMetadataUrl(countryMetadataUrl);
            countries.add(country);
            eidasProperties.remove(countryID + COUNTRY_ID_SUFFIX);
            eidasProperties.remove(countryID + COUNTRY_NAME_SUFFIX);
            eidasProperties.remove(countryID + COUNTRY_URL_SUFFIX);
            eidasProperties.remove(countryID + COUNTRY_SKEW_SUFFIX);
        }
        loadEncryptionConf();

    }
    private void loadEncryptionConf(){
        CertificateManagerConfigurationImpl samlConfig=null;
        Properties encryptionProps=new Properties();
        if(eidasProperties.containsKey(SAML_ENGINE_REPOSITORY_URL)){
            //in this case a SamlEngineConfiguration instance will hold the SamlEngine configuration
            samlConfig = EidasConfigManagerUtil.getInstance().getCertificateManagerConfiguration();
        }
        if(samlConfig!=null){
            encryptionProps = ((FileConfigurationRepository) repository).loadPropertiesFromXML("encryptionConf.xml");
        }
        for(EIDASNodeCountry country:countries){
            String encryptionKey="EncryptTo."+country.getCode();
            if(encryptionProps.containsKey(encryptionKey)){
                country.setEncryptionTo(Boolean.parseBoolean(encryptionProps.getProperty(encryptionKey)));
            }
            encryptionKey="EncryptFrom."+country.getCode();
            if(encryptionProps.containsKey(encryptionKey)){
                country.setEncryptionFrom(Boolean.parseBoolean(encryptionProps.getProperty(encryptionKey)));
            }
        }
    }

    private void saveCountries(){
        //merge countries with eidasProperties
        setCountryParameter(EIDAS_SERVICE_NUMBER_NAME, Integer.toString(countries.size()));
        Iterator<EIDASNodeCountry> i = countries.iterator();
        int c = 1;
        while (i.hasNext()) {
            String countryID = COUNTRY_PREFIX + c++;
            EIDASNodeCountry currentCountry = i.next();
            setCountryParameter(countryID + COUNTRY_ID_SUFFIX, currentCountry.getCode());
            setCountryParameter(countryID + COUNTRY_NAME_SUFFIX, currentCountry.getName());
            setCountryParameter(countryID + COUNTRY_URL_SUFFIX, currentCountry.getServiceUrl());
            setCountryParameter(countryID + COUNTRY_METADATAURL_SUFFIX, currentCountry.getServiceMetadataUrl());
            setCountryParameter(countryID + COUNTRY_SKEW_SUFFIX, Integer.toString(currentCountry.getSkewTime()));
        }
    }

    private void setCountryParameter(String name, String value) {
        eidasProperties.setProperty(name, value);
        EIDASParameterMetaImpl metadata = (EIDASParameterMetaImpl) (metadataProvider.getMetadata(name));
        if (metadata == null || !parameters.containsKey(name)) {
            EIDASNodeParameter newParameter=new EIDASNodeParameter();
            newParameter.setName(name);
            newParameter.setValue(value);
            metadata = new EIDASParameterMetaImpl();
            metadata.setSourceFile(metadataProvider.getDefaultConfFile());
            metadata.setName(name);
            newParameter.setMetadata(metadata);
            metadataProvider.addMetadata(name, metadata);
            parameters.put(name, newParameter);
        }
        parameters.get(name).setValue(value);
    }


    @Override
    public void save() {
        CertificateManagerConfigurationImpl samlConfig=null;
        if(eidasProperties.containsKey(SAML_ENGINE_REPOSITORY_URL)){
            //in this case a SamlEngineConfiguration instance will hold the SamlEngine configuration
            samlConfig = EidasConfigManagerUtil.getInstance().getCertificateManagerConfiguration();
        }
        saveCountries();

        Properties encryptionProps=new Properties();
        for(EIDASNodeCountry country:countries){
            encryptionProps.setProperty("EncryptTo."+country.getCode(), Boolean.toString(country.isEncryptionTo()));
            encryptionProps.setProperty("EncryptFrom."+country.getCode(), Boolean.toString(country.isEncryptionFrom()));
        }
        if(samlConfig!=null){
            EidasConfigManagerUtil.getInstance().saveProps("encryptionConf.xml", encryptionProps);
        }
        saveToFiles(splitParametersPerFile());
    }

    private Map<EIDASNodeConfFile, Properties> splitParametersPerFile(){
        Map<EIDASNodeConfFile, Properties> fileContents=new HashMap<EIDASNodeConfFile, Properties>();
        EIDASNodeMetaconfigProvider metadataProvider = getMetadataProvider();
        for(EIDASNodeParameter p:parameters.values()){
            eidasProperties.setProperty(p.getName(), p.getValue());
            EIDASParameterMetaImpl metadata = (EIDASParameterMetaImpl)(metadataProvider.getMetadata(p.getName()));
            if(metadata!=null && metadata.getSourceFile()!=null){
                Properties props=fileContents.get(metadata.getSourceFile());
                if(props==null){
                    props=new Properties();
                    fileContents.put(metadata.getSourceFile(), props);
                }
                props.setProperty(p.getName(), p.getValue());
            }
        }
        return fileContents;
    }
    private void saveToFiles(Map<EIDASNodeConfFile, Properties> fileContents){
        for(Map.Entry<EIDASNodeConfFile, Properties> entry:fileContents.entrySet()){
            if(EIDASNodeConfFile.FileType.XML.toString().equalsIgnoreCase(entry.getKey().getType())){
                ((FileConfigurationRepository) repository).savePropertiesToXML(entry.getKey().getFileName(), entry.getValue());
            }else if(EIDASNodeConfFile.FileType.PROPERTIES.toString().equalsIgnoreCase(entry.getKey().getType())) {
                ((FileConfigurationRepository) repository).savePropertiesToTextFile(entry.getKey().getFileName(), entry.getValue());
            }
        }

    }

    @Override
    public MetadataRepository getSamlMetadataRepository() {
        MetadataRepositoryImpl samlMetadataRepository=(MetadataRepositoryImpl)super.getSamlMetadataRepository();
        if(eidasProperties==null){
            load();
        }
        if(samlMetadataRepository.getFileService().getRepositoryDir()==null) {
            samlMetadataRepository.getFileService().setRepositoryDir(eidasProperties.getProperty(MetadataRepositoryImpl.SAML_METADATA_LOCATION));
        }
        return samlMetadataRepository;
    }

}
