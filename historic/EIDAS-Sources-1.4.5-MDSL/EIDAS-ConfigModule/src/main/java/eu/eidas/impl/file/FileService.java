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
package eu.eidas.impl.file;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasStringUtil;

/**
 * provide file services - reading/writing to disk
 *
 * @deprecated use the java 7 {@link java.nio.file.FileSystem#newWatchService()} instead, which implements this natively, is thread-safe and does not start a Thread.
 */
@Deprecated
public class FileService {
    private static final Logger LOG = LoggerFactory.getLogger(FileService.class.getName());
    private static final int MAX_FILE_SIZE=50000;
    String repositoryDir;
    String alternateLocation;
    boolean validRepositoryLocation=false;
    public String getRepositoryDir() {
        return repositoryDir;
    }

    public void setRepositoryDir(String repositoryDir) {
        this.repositoryDir = repositoryDir;
    }

    private void checkDirectory(){
        if (validRepositoryLocation) {
            return;
        }
        checkForAlternateLocation();
        if (StringUtils.isNotBlank(repositoryDir)) {
            File repository = new File(repositoryDir);
            if (!repository.exists()) {
                LOG.info("ERROR : the directory " + repositoryDir + " does not exist");
                return;
            }
            if (!repository.isDirectory()) {
                repositoryDir = repository.getPath();
                repository = new File(repositoryDir);
            }
            if (!repository.isDirectory()) {
                LOG.info("ERROR : the directory " + repositoryDir + " does not represent an existing directory");
                return;
            }
            normalizeDirectory(repository);
        }
    }

    private void checkForAlternateLocation(){
        if((repositoryDir==null || repositoryDir.isEmpty()) &&getAlternateLocation()!=null && !getAlternateLocation().isEmpty()){
            LOG.info("ERROR : invalid value for repository directory, trying alternate location");
            repositoryDir=getAlternateLocation();
        }
    }

    /**
     *
     * @param repository - assumes a valid directory
     */
    private void normalizeDirectory(File repository){
        validRepositoryLocation=true;
        repositoryDir=repository.getAbsolutePath();
        if(!repositoryDir.endsWith(File.separator) && !repositoryDir.endsWith("/")) {
            repositoryDir += "/";
        }

    }
    public boolean existsFile(String fileName){
        checkDirectory();
        if(!validRepositoryLocation) {
            return false;
        }
        File f=new File(getAbsoluteFileName(fileName));
        boolean b=f.exists();
        return b;
    }

    public String getAbsoluteFileName(String fileName){
        checkDirectory();
        if(validRepositoryLocation && fileName!=null) {
            String repositoryPrefix = repositoryDir.substring(0, repositoryDir.length()-1);
            File f=new File(fileName);
            if(!fileName.isEmpty() && f.exists() && f.getParent()!=null){
                return fileName;
            }
            return fileName.startsWith(repositoryPrefix) ? fileName : normalizePath(repositoryDir + fileName);
        }else{
            LOG.info("ERROR : trying to access an invalid repository");
            return "";
        }
    }

    private String normalizePath(final String path){
        if(path!=null){
            String normalPath=path.replace('/', File.separatorChar);
            normalPath=normalPath.replace('\\', File.separatorChar);
            return normalPath;
        }
        return null;
    }
    /**
     *
     * @param fileName
     * @return a Properties object loaded from the file
     */
    public Properties loadPropsFromXml(String fileName){
        Properties props=new Properties();
        InputStream is=null;
        try{
            is=new FileInputStream(new File(getAbsoluteFileName(fileName)));
            props.loadFromXML(is);
        }catch(FileNotFoundException fnfe){
            LOG.info("ERROR : FileNotFoundException: ", fnfe.getMessage());
            LOG.debug("ERROR : FileNotFoundException: ", fnfe);
        }catch(InvalidPropertiesFormatException ipfe){
            LOG.info("ERROR : InvalidPropertiesFormatException: ", ipfe.getMessage());
            LOG.debug("ERROR : InvalidPropertiesFormatException: ", ipfe);
        }catch(IOException ioe){
            LOG.info("ERROR : IOException: ", ioe.getMessage());
            LOG.debug("ERROR : IOException: ", ioe);
        }finally{
            safeClose(is);
        }
        return props;
    }

    /**
     *
     * @param fileName
     * @return a Properties object loaded from the file
     */
    public Properties loadPropsFromTextFile(String fileName){
        Properties props=new Properties();
        InputStream is=null;
        try{
            is=new FileInputStream(new File(getAbsoluteFileName(fileName)));
            props.load(is);
        }catch(FileNotFoundException fnfe){
            LOG.error("ERROR : FileNotFoundException: ", fnfe.getMessage());
            LOG.debug("ERROR : FileNotFoundException: ", fnfe);
        }catch(InvalidPropertiesFormatException ipfe){
            LOG.error("ERROR : InvalidPropertiesFormatException: ", ipfe.getMessage());
            LOG.debug("ERROR : InvalidPropertiesFormatException: ", ipfe);
        }catch(IOException ioe){
            LOG.error("ERROR : IOException: ", ioe.getMessage());
            LOG.debug("ERROR : IOException: ", ioe);
        }finally{
            safeClose(is);
        }
        return props;
    }

    /**
     * stores a Properties object to a file
     * @param fileName
     * @param props
     * @return
     */
    public void saveToXMLFile(String fileName, Properties props){
        OutputStream os=null;
        try{
            os=new FileOutputStream(new File(getAbsoluteFileName(fileName)));
            props.storeToXML(os, fileName);
        }catch(FileNotFoundException fnfe){
            LOG.error("ERROR : FileNotFoundException: ", fnfe.getMessage());
            LOG.debug("ERROR : FileNotFoundException: ", fnfe);
        }catch(IOException ioe){
            LOG.error("ERROR : IOException: ", ioe.getMessage());
            LOG.debug("ERROR : IOException: ", ioe);
        }finally{
            safeClose(os);
        }
    }

    public void saveToPropsFile(String fileName, Properties props){
        OutputStream os=null;
        try{
            os=new FileOutputStream(new File(getAbsoluteFileName(fileName)));
            props.store(os, fileName);
        }catch(FileNotFoundException fnfe){
            LOG.error("ERROR : FileNotFoundException: ", fnfe.getMessage());
            LOG.debug("ERROR : FileNotFoundException: ", fnfe);
        }catch(IOException ioe){
            LOG.error("ERROR : IOException: ", ioe.getMessage());
            LOG.debug("ERROR : IOException: ", ioe);
        }finally{
            safeClose(os);
        }
    }


    public String fileToString(String fileName){
        String fileContents=null;
        byte data[]=loadBinaryFile(fileName);
        if(data!=null && data.length>0) {
            fileContents = EidasStringUtil.toString(data);
        }

        return fileContents;
    }

    public byte[] loadBinaryFile(String fileName){
        byte[] data = null;
        try{
            Path path = Paths.get(getAbsoluteFileName(fileName));
            data = Files.readAllBytes(path);
        }catch(FileNotFoundException fnfe){
            LOG.error("ERROR : FileNotFoundException: ", fnfe.getMessage());
            LOG.debug("ERROR : FileNotFoundException: ", fnfe);
        }catch(IOException ioe){
            LOG.error("ERROR : IOException: ", ioe.getMessage());
            LOG.debug("ERROR : IOException: ", ioe);
        }
        return data;
    }

    public void saveBinaryFile(String fileName, byte[] data){
        if(data==null || data.length>MAX_FILE_SIZE || data.length==0){
            //should also return an error code?
            return;
        }
        FileOutputStream fos=null;
        try{
            fos=new FileOutputStream(getAbsoluteFileName(fileName));
            fos.write(data);
        }catch(FileNotFoundException fnfe){
            LOG.error("ERROR : FileNotFoundException: ", fnfe.getMessage());
            LOG.debug("ERROR : FileNotFoundException: ", fnfe);
        }catch(IOException ioe){
            LOG.error("ERROR : IOException: ", ioe.getMessage());
            LOG.debug("ERROR : IOException: ", ioe);
        }finally{
            safeClose(fos);
        }
    }

    public void stringToFile(String fileName, String data){
        FileOutputStream os=null;
        try{
            os=new FileOutputStream(new File(getAbsoluteFileName(fileName)));
            os.write(data.getBytes(Charset.forName("UTF-8")));
        }catch(FileNotFoundException fnfe){
            LOG.error("ERROR : FileNotFoundException: ", fnfe.getMessage());
            LOG.debug("ERROR : FileNotFoundException: ", fnfe);
        }catch(IOException ioe){
            LOG.error("ERROR : IOException: ", ioe.getMessage());
            LOG.debug("ERROR : IOException: ", ioe);
        }finally{
            safeClose(os);
        }

    }

    private void safeClose(Closeable c){
        if(c!=null){
            try{
                c.close();
            }catch(IOException ioe){
                LOG.error("ERROR : IOException while closing inputstream: ", ioe.getMessage());
                LOG.debug("ERROR : IOException while closing inputstream: ", ioe);
            }
        }
    }

    public String getAlternateLocation() {
        if(alternateLocation!=null && !alternateLocation.isEmpty()){
            //clean up
            alternateLocation = cleanPath(alternateLocation);
        }
        return alternateLocation;
    }
    private static final String FILE_PROTOCOL="file:";

    /**
     *
     * @param path assumed to be not null
     * @return the path equivalent, cleaning the file protocol, if it exists
     */
    private String cleanPath(String path){
        String cleanPath=path;
        if(path.startsWith(FILE_PROTOCOL)){
            cleanPath=cleanPath.substring(FILE_PROTOCOL.length());
        }
        return cleanPath;
    }

    public void setAlternateLocation(String alternateLocation) {
        this.alternateLocation = alternateLocation;
    }

    /**
     *
     * @return the list of all the files contained in the current location
     */
    public List<String> getFileList(boolean filterBackups){
        List<String> files=new ArrayList<String>();
        checkDirectory();
        if(!validRepositoryLocation){
            return files;
        }
        files=iterateFiles(new File(repositoryDir), filterBackups);
        return files;
    }

    private List<String> iterateFiles(File directory, boolean filterBackups){
        File[] directoryListing = directory.listFiles();
        List<String> files=new ArrayList<String>();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if(child.isDirectory()) {
                    files.addAll(iterateFiles(child, filterBackups));
                }else if(child.exists()){
                    String completeFileName=child.getAbsolutePath();
                    if(!filterBackups || !(completeFileName.length()>4 && ".zip".equalsIgnoreCase(completeFileName.substring(completeFileName.length()-4).toLowerCase(Locale.ENGLISH)))) {
                        files.add(child.getAbsolutePath());
                    }
                }
            }
        }
        return files;
    }

    /**
     * perform a backup of the current configuration
     * the result is stored in the same directory, in a file named backupTIMESTAMP.zip
     */
    public void backup(){
        String archiveFileName= getAbsoluteFileName(getArchiveFileName());
        List<String> sourceFile=getFileList(true);
        Zip.zip(sourceFile, archiveFileName, repositoryDir);
    }

    private String getArchiveFileName(){
        Calendar calendar=Calendar.getInstance();
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        String ret = formatter.format("backup%1$4d%2$02d%3$02d%4$02d%5$02d%6$02d%7$03d.zip", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND)).toString();
        formatter.close();
        return ret;
    }
}
