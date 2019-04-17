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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.config.ConfigurationException;

/**
 * archiving utilities
 */
public class Zip {
    private static final Logger LOG = LoggerFactory.getLogger(Zip.class.getName());

    static final int BUFFER_SIZE = 4096;

    private Zip(){

    }
    public static void zip (List<String> sourceFiles, String destinationFileName, String baseDirectoryPath) throws ConfigurationException {
        List<String> relativeFileNames=new ArrayList<String>();
        String directoryPath=baseDirectoryPath.replace('\\','/');
        for(String fileName:sourceFiles){
            if(fileName.replace('\\','/').startsWith(directoryPath)) {
                fileName=fileName.substring(baseDirectoryPath.length());
            }
            relativeFileNames.add(fileName);
            zip(sourceFiles,relativeFileNames, destinationFileName);
        }
    }

    @SuppressWarnings("squid:S2095")
    public static void zip (List<String> sourceFiles, List<String> sourceFilesNames, String destinationFileName) throws ConfigurationException {
        ZipOutputStream out = null;
        FileInputStream fi = null;
        BufferedInputStream origin = null;
        try {
            FileOutputStream dest = new
                    FileOutputStream(destinationFileName);
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            out.setMethod(ZipOutputStream.DEFLATED);
            byte data[] = new byte[BUFFER_SIZE];

            for (int i=0;i<sourceFiles.size();i++) {
                String file = sourceFiles.get(i);
                String entryName=file;
                if(sourceFilesNames!=null && sourceFilesNames.size()>i){
                    entryName=sourceFilesNames.get(i);
                }
                fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                ZipEntry entry = new ZipEntry(entryName);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                    out.write(data, 0, count);
                }
            }
        } catch (Exception e) {
            LOG.error("error during backup {}", e);
            throw new ConfigurationException("", "", e);
        } finally {
            if (out != null){
                try {
                    out.close();
                }catch(IOException ioe){
                    LOG.error("ERROR : cannot close output stream {}", ioe.getMessage());
                    LOG.debug("ERROR : cannot close output stream {}", ioe);
                }
            }
            if (origin != null) {
                try {
                    origin.close();
                }catch(IOException ioe){
                    LOG.error("ERROR : cannot close input stream {}", ioe.getMessage());
                    LOG.debug("ERROR : cannot close input stream {}", ioe);
                }
            }
        }
    }
}
