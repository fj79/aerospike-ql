package com.spikeify.aerospikeql.execute;

import com.spikeify.Spikeify;
import com.spikeify.aerospikeql.AerospikeQlService;
import com.spikeify.aerospikeql.QueryUtils;
import com.spikeify.aerospikeql.TestAerospike;
import com.spikeify.annotations.UserKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class MapEntityTest {

	Spikeify sfy;
	AerospikeQlService aerospikeQlService;


	@Before
	public void setUp() throws Exception {
		TestAerospike testAerospike = new TestAerospike();
		sfy = testAerospike.getSfy();
		QueryUtils queryUtils = new QueryUtils(sfy, "udf/");
		aerospikeQlService = new AerospikeQlService(sfy, queryUtils);
		sfy.truncateNamespace(TestAerospike.DEFAULT_NAMESPACE);
	}

	@After
	public void tearDown() {
		sfy.truncateNamespace(TestAerospike.DEFAULT_NAMESPACE);
	}

	private void createSet(int numRecords) {
		Entity entity;
		for (int i = 1; i < numRecords + 1; i++) {
			entity = new Entity();
			entity.key = String.valueOf(i);
			entity.value = i;
			entity.value2 = i + 1;
			entity.cluster = i % 4;
			sfy.create(entity).now();
		}

		int i = numRecords + 2;
		entity = new Entity();
		entity.key = String.valueOf(i);
		entity.value = null;
		entity.value2 = i + 1;
		entity.cluster = null;
		sfy.create(entity).now();
	}

	public static class Entity {

		@UserKey
		public String key;
		public Integer value;
		public Integer value2;
		public Integer cluster;

		public Entity() {
		}

	}

	@Test
	public void testMapEntity() throws Exception {
		createSet(100);
		String query = "select * from " + TestAerospike.DEFAULT_NAMESPACE + ".Entity order by value2";

		List<Entity> resultsList = aerospikeQlService.execAdhoc(Entity.class, query).now();
		assertEquals(101, resultsList.size());
		Integer count = 2;
		for (Entity entity : resultsList) {
			if (count < 101) {
				assertEquals(count++, entity.value2);
			}
		}

	}

	@Test
	public void testMapEntityAdditionalField() throws Exception {
		createSet(100);
		String query = "select cluster, " +
						"avg(value) as avgValue from " + TestAerospike.DEFAULT_NAMESPACE + ".Entity " +
						"group by cluster";

		List<Map<String, Object>> resultsList = aerospikeQlService.execAdhoc(query).now();
		assertEquals(5, resultsList.size());


	}



}
