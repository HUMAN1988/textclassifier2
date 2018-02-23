package org.ripreal.textclassifier2.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ripreal.textclassifier2.App;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {App.class})
public abstract class AbstractServiceTest<T> {

    @Before
    public void setUp() {
        DataService<T> service = createDataService();
        service.deleteAll();
    }

    protected abstract DataService<T> createDataService();

    protected abstract List<T> getTestData();

    @Test
    public void testCRUD() throws Exception {

        List<T> testData = getTestData();

        //TEST CREATE
        DataService<T> service = createDataService();

        assertNotNull(service.saveAll(testData).blockLast());
        assertNotNull(service.findAll().blockLast());
        assertNull(service.deleteAll().blockLast());
    }
}
