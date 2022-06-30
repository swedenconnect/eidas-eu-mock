package eu.eidas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.fail;

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
            fail("Error initializing the test environment");
        }
    }
    private static void copyOneFile(File fSource, File fDest) throws IOException {
        FileInputStream fis = new FileInputStream(fSource);
        FileOutputStream fos = new FileOutputStream(fDest);
        byte[] buffer = new byte[4*1024];
        int iBytesReads;
        while ((iBytesReads = fis.read(buffer)) >= 0) {
            fos.write(buffer, 0, iBytesReads);
        }
        if (fis != null) {
            fis.close();
        }
        if (fos != null) {
            fos.close();
        }
    }

}
