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
package eu.eidas.samlmetadata;

import eu.eidas.FileUtils;
import eu.eidas.config.impl.samlmetadata.MetadataRepositoryImpl;
import eu.eidas.config.samlmetadata.MetadataItem;
import eu.eidas.config.samlmetadata.MetadataRepository;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileSystemUtils;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/testcontext.xml")
@FixMethodOrder(MethodSorters.JVM)
@Ignore
public class TestMetadataRepository {
    private static final String FILEREPO_DIR="target/test/samlmetadatarepository/";
    private static final String FILEREPO_SOURCE_DIR="src/test/resources/samlmetadatarepository/";
    @Autowired
    MetadataRepository metadataRepository=null;

    @Before
    public void setUp(){
        java.io.File sampleNodeRepo=new java.io.File(FILEREPO_DIR);
        FileSystemUtils.deleteRecursively(sampleNodeRepo);
        sampleNodeRepo.mkdirs();
        FileUtils.copyFile(new File(FILEREPO_SOURCE_DIR), new File(FILEREPO_DIR));
    }


    @Test
    public void testRead(){
        assertNotNull(metadataRepository);
        ((((MetadataRepositoryImpl)metadataRepository).getFileService())).setRepositoryDir(FILEREPO_DIR);
        assertNotNull(metadataRepository.getIDs());
        assertTrue(metadataRepository.getIDs().size()==2);

        String metadata1=metadataRepository.getIDs().get(0);
        MetadataItem item=metadataRepository.getMetadataItem(metadata1);
        assertNotNull(item);
        assertNotNull(item.getIssuerUrl());
    }

    @Test
    public void testWrite(){
    }
}
