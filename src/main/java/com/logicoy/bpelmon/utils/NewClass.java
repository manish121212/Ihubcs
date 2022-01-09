package com.logicoy.bpelmon.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

/*
 *
 * @author Param
 */
public class NewClass {

	private static Logger LOGGER = Logger.getLogger("NewClass");

	public static void main(String[] a) throws IOException {
		BulkRequest requestBulk = new BulkRequest();
		RestHighLevelClient client = null;
		Set<String> instanceid = new HashSet<>();
		HttpHost[] hosts = null;
		String ELASTIC_HOST = "127.0.0.1:9200:http";

		String[] hostArr = ELASTIC_HOST.split(",");
		hosts = new HttpHost[hostArr.length];
		int index = 0;
		for (String hostString : hostArr) {
			LOGGER.log(Level.INFO, "Host string : {0}", hostString);
			String[] hostSArr = hostString.split("\\:");
			LOGGER.log(Level.INFO, "Init elastic host name : {0}", hostSArr[0]);
			LOGGER.log(Level.INFO, "Init elastic host port : {0}", hostSArr[1]);
			Integer port = Integer.parseInt(hostSArr[1]);
			HttpHost host = new HttpHost(hostSArr[0], port, hostSArr[2]);
			hosts[index] = host;
			index++;
		}
		try {
			client = new RestHighLevelClient(RestClient.builder(hosts));

			// change max length to increasae load
			for (int i = 0; i < 10000; i++) {
				String instanceidstr = UUID.randomUUID().toString();

				Map<String, Object> data = new HashMap<>();

				data.put("instance_id", instanceidstr);
				instanceid.add(instanceidstr);
				// for( int j = 0;j<100;j++){
				IndexRequest indexRequest = new IndexRequest("bpel_mon_event_f11", "bpel_mon_event_type_f11")
						.source(data);

				requestBulk.add(indexRequest);

				if (i % 10000 == 0) {
					client.bulk(requestBulk);
					requestBulk = new BulkRequest();
				}

			}
			client.bulk(requestBulk);
			LOGGER.info("Total expected document count : " + instanceid.size());
		} finally {
			if(client != null)
				client.close();
		}
	}
}