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

package eu.eidas.node.auth.util.tests;

import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class FileUtils {
    public static void copyFile(File fSource, File fDest) {
        try {
            if (fSource.isDirectory()) {
                String[] fList = fSource.list();
                for (int i = 0; i < fList.length; i++) {
                    File dest = new File(fDest, fList[i]);
                    File source = new File(fSource, fList[i]);
                    copyFile(source, dest);
                }
            }
            else {
                copyOneFile(fSource, fDest);
            }
        }
        catch (Exception ex) {
            Assert.fail("Error initializing the test environment");
        }
    }
    private static void copyOneFile(File fSource, File fDest) throws IOException {

        try (FileInputStream fis = new FileInputStream(fSource);
             FileOutputStream fos = new FileOutputStream(fDest)) {
            byte[] buffer = new byte[4 * 1024];
            int iBytesReads;
            while ((iBytesReads = fis.read(buffer)) >= 0)
                fos.write(buffer, 0, iBytesReads);
        }
    }

    /**
     * Copies the files from a source path to a destination path
     *
     * @param sourcePath the source path
     * @param destinationPath the destination path
     * @throws IOException  if an I/O error is thrown when accessing the starting file.
     */
    public static void copyFolder(Path sourcePath, Path destinationPath) throws IOException {
        Files.walk(sourcePath)
                .forEach(source -> copy(source, destinationPath.resolve(sourcePath.relativize(source))));
    }

    private static void copy(Path sourcePath, Path destinationPath) {
        try {
            Files.copy(sourcePath, destinationPath, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
