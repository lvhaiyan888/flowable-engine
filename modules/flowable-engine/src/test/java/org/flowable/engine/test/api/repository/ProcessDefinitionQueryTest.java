/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flowable.engine.test.api.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Joram Barrez
 */
public class ProcessDefinitionQueryTest extends PluggableFlowableTestCase {

    private String deploymentOneId;
    private String deploymentTwoId;

    @BeforeEach
    protected void setUp() throws Exception {
        deploymentOneId = repositoryService.createDeployment().name("org/flowable/engine/test/repository/one.bpmn20.xml").addClasspathResource("org/flowable/engine/test/repository/one.bpmn20.xml")
                .addClasspathResource("org/flowable/engine/test/repository/two.bpmn20.xml").deploy().getId();

        deploymentTwoId = repositoryService.createDeployment().name("org/flowable/engine/test/repository/one.bpmn20.xml").addClasspathResource("org/flowable/engine/test/repository/one.bpmn20.xml")
                .deploy().getId();

    }

    @AfterEach
    protected void tearDown() throws Exception {
        repositoryService.deleteDeployment(deploymentOneId, true);
        repositoryService.deleteDeployment(deploymentTwoId, true);
    }

    @Test
    public void testProcessDefinitionProperties() {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionName().asc().orderByProcessDefinitionVersion().asc()
                .orderByProcessDefinitionCategory().asc().list();

        ProcessDefinition processDefinition = processDefinitions.get(0);
        assertEquals("one", processDefinition.getKey());
        assertEquals("One", processDefinition.getName());
        assertTrue(processDefinition.getId().startsWith("one:1"));
        assertEquals("Examples", processDefinition.getCategory());

        processDefinition = processDefinitions.get(1);
        assertEquals("one", processDefinition.getKey());
        assertEquals("One", processDefinition.getName());
        assertTrue(processDefinition.getId().startsWith("one:2"));
        assertEquals("Examples", processDefinition.getCategory());

        processDefinition = processDefinitions.get(2);
        assertEquals("two", processDefinition.getKey());
        assertEquals("Two", processDefinition.getName());
        assertTrue(processDefinition.getId().startsWith("two:1"));
        assertEquals("Examples2", processDefinition.getCategory());
    }

    @Test
    public void testQueryByDeploymentId() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentOneId);
        verifyQueryResults(query, 2);
    }

    @Test
    public void testQueryByInvalidDeploymentId() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().deploymentId("invalid");
        verifyQueryResults(query, 0);

        try {
            repositoryService.createProcessDefinitionQuery().deploymentId(null);
            fail();
        } catch (FlowableIllegalArgumentException e) {
        }
    }

    @Test
    public void testQueryByName() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionName("Two");
        verifyQueryResults(query, 1);

        query = repositoryService.createProcessDefinitionQuery().processDefinitionName("One");
        verifyQueryResults(query, 2);
    }

    @Test
    public void testQueryByInvalidName() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionName("invalid");
        verifyQueryResults(query, 0);

        try {
            repositoryService.createProcessDefinitionQuery().processDefinitionName(null);
            fail();
        } catch (FlowableIllegalArgumentException e) {
        }
    }

    @Test
    public void testQueryByNameLike() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionNameLike("%w%");
        verifyQueryResults(query, 1);
    }

    @Test
    public void testQueryByInvalidNameLike() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionNameLike("%invalid%");
        verifyQueryResults(query, 0);
    }

    @Test
    public void testQueryByKey() {
        // process one
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one");
        verifyQueryResults(query, 2);

        // process two
        query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("two");
        verifyQueryResults(query, 1);
    }

    @Test
    public void testQueryByInvalidKey() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("invalid");
        verifyQueryResults(query, 0);

        try {
            repositoryService.createProcessDefinitionQuery().processDefinitionKey(null);
            fail();
        } catch (FlowableIllegalArgumentException e) {
        }
    }

    @Test
    public void testQueryByKeyLike() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKeyLike("%o%");
        verifyQueryResults(query, 3);
    }

    @Test
    public void testQueryByInvalidKeyLike() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKeyLike("%invalid%");
        verifyQueryResults(query, 0);

        try {
            repositoryService.createProcessDefinitionQuery().processDefinitionKeyLike(null);
            fail();
        } catch (FlowableIllegalArgumentException e) {
        }
    }

    @Test
    public void testQueryByCategory() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionCategory("Examples");
        verifyQueryResults(query, 2);
    }

    @Test
    public void testQueryByCategoryLike() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionCategoryLike("%Example%");
        verifyQueryResults(query, 3);

        query = repositoryService.createProcessDefinitionQuery().processDefinitionCategoryLike("%amples2");
        verifyQueryResults(query, 1);
    }

    @Test
    public void testQueryByVersion() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(2);
        verifyQueryResults(query, 1);

        query = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(1);
        verifyQueryResults(query, 2);
    }

    @Test
    public void testQueryByInvalidVersion() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(3);
        verifyQueryResults(query, 0);

        try {
            repositoryService.createProcessDefinitionQuery().processDefinitionVersion(-1).list();
            fail();
        } catch (FlowableIllegalArgumentException e) {
        }

        try {
            repositoryService.createProcessDefinitionQuery().processDefinitionVersion(null).list();
            fail();
        } catch (FlowableIllegalArgumentException e) {
        }
    }

    @Test
    public void testQueryByKeyAndVersion() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").processDefinitionVersion(1);
        verifyQueryResults(query, 1);

        query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").processDefinitionVersion(2);
        verifyQueryResults(query, 1);

        query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").processDefinitionVersion(3);
        verifyQueryResults(query, 0);
    }

    @Test
    public void testQueryByLatest() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().latestVersion();
        verifyQueryResults(query, 2);

        query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").latestVersion();
        verifyQueryResults(query, 1);

        query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("two").latestVersion();
        verifyQueryResults(query, 1);
    }

    @Test
    public void testQuerySorting() {

        // asc

        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionId().asc();
        verifyQueryResults(query, 3);

        query = repositoryService.createProcessDefinitionQuery().orderByDeploymentId().asc();
        verifyQueryResults(query, 3);

        query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().asc();
        verifyQueryResults(query, 3);

        query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().asc();
        verifyQueryResults(query, 3);

        // desc

        query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionId().desc();
        verifyQueryResults(query, 3);

        query = repositoryService.createProcessDefinitionQuery().orderByDeploymentId().desc();
        verifyQueryResults(query, 3);

        query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().desc();
        verifyQueryResults(query, 3);

        query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().desc();
        verifyQueryResults(query, 3);

        // Typical use case
        query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().asc().orderByProcessDefinitionVersion().desc();
        List<ProcessDefinition> processDefinitions = query.list();
        assertEquals(3, processDefinitions.size());

        assertEquals("one", processDefinitions.get(0).getKey());
        assertEquals(2, processDefinitions.get(0).getVersion());
        assertEquals("one", processDefinitions.get(1).getKey());
        assertEquals(1, processDefinitions.get(1).getVersion());
        assertEquals("two", processDefinitions.get(2).getKey());
        assertEquals(1, processDefinitions.get(2).getVersion());
    }

    private void verifyQueryResults(ProcessDefinitionQuery query, int countExpected) {
        assertEquals(countExpected, query.list().size());
        assertEquals(countExpected, query.count());

        if (countExpected == 1) {
            assertNotNull(query.singleResult());
        } else if (countExpected > 1) {
            verifySingleResultFails(query);
        } else if (countExpected == 0) {
            assertNull(query.singleResult());
        }
    }

    private void verifySingleResultFails(ProcessDefinitionQuery query) {
        try {
            query.singleResult();
            fail();
        } catch (FlowableException e) {
        }
    }

    @Test
    public void testQueryByMessageSubscription() {
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("org/flowable/engine/test/api/repository/processWithNewBookingMessage.bpmn20.xml")
                .addClasspathResource("org/flowable/engine/test/api/repository/processWithNewInvoiceMessage.bpmn20.xml").deploy();

        assertEquals(1, repositoryService.createProcessDefinitionQuery().messageEventSubscriptionName("newInvoiceMessage").count());

        assertEquals(1, repositoryService.createProcessDefinitionQuery().messageEventSubscriptionName("newBookingMessage").count());

        assertEquals(0, repositoryService.createProcessDefinitionQuery().messageEventSubscriptionName("bogus").count());

        repositoryService.deleteDeployment(deployment.getId());
    }

    @Test
    public void testNativeQuery() {
        assertEquals("ACT_RE_PROCDEF", managementService.getTableName(ProcessDefinition.class));
        assertEquals("ACT_RE_PROCDEF", managementService.getTableName(ProcessDefinitionEntity.class));
        String tableName = managementService.getTableName(ProcessDefinition.class);
        String baseQuerySql = "SELECT * FROM " + tableName;

        assertEquals(3, repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql).list().size());

        assertEquals(3, repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql + " where KEY_ like #{key}").parameter("key", "%o%").list().size());

        assertEquals(2, repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql + " where NAME_ = #{name}").parameter("name", "One").list().size());

        // paging
        assertEquals(2, repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql).listPage(0, 2).size());
        assertEquals(2, repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql).listPage(1, 3).size());
    }

    @Test
    public void testQueryByProcessDefinitionIds() {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
        Set<String> ids = new HashSet<>();
        for (ProcessDefinition processDefinition : processDefinitions) {
            ids.add(processDefinition.getId());
        }

        List<ProcessDefinition> queryResults = repositoryService.createProcessDefinitionQuery().processDefinitionIds(ids).list();
        assertEquals(queryResults.size(), ids.size());
        for (ProcessDefinition processDefinition : queryResults) {
            assertTrue(ids.contains(processDefinition.getId()));
        }
    }

}
