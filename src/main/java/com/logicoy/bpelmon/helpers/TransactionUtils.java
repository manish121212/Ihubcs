package com.logicoy.bpelmon.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicoy.bpelmon.models._TransactionSourceModel;
import com.logicoy.bpelmon.utils.AppConstants;
import com.logicoy.bpelmon.utils.Utils;

@Component
public class TransactionUtils {

	@Autowired
	AppConstants appConst;
	@Autowired
	Utils utils;

	private SearchRequest searchRequest;
	private SearchRequest subTransactionSearchRequest;

	/**
	 * Create search request for Transaction index
	 * 
	 * @return An object of type SearchRequest
	 */
	public SearchRequest createTransactionIndexSearchRequest(String clientId) {

		// Logger.getGlobal().info("Index: " + appConst.getTransactionIndex() + " Index
		// type: " + appConst.getTransactionIndexType());
		if (clientId != null) {
			clientId = clientId.toLowerCase().trim();
			searchRequest = new SearchRequest(appConst.getTransactionIndex() + "_" + clientId.toLowerCase().trim());
			searchRequest.types(appConst.getTransactionIndexType() + "_" + clientId.toLowerCase().trim());
			return searchRequest;
		} else {
			searchRequest = new SearchRequest(appConst.getTransactionIndex());
			searchRequest.types(appConst.getTransactionIndexType());
			return searchRequest;
		}
		// searchRequest = new SearchRequest(appConst.getTransactionIndex());
		// searchRequest.types(appConst.getTransactionIndexType());
		// return searchRequest;
	}

	public SearchRequest createSubTransactionSearchRequest(String clientId) {

		if (clientId != null) {
			clientId = clientId.toLowerCase().trim();
			subTransactionSearchRequest = new SearchRequest(
					appConst.getSubTransactionIndex() + "_" + clientId.toLowerCase().trim());
			subTransactionSearchRequest
					.types(appConst.getSubTransactionIndexType() + "_" + clientId.toLowerCase().trim());
			return subTransactionSearchRequest;
		} else {
			subTransactionSearchRequest = new SearchRequest(appConst.getSubTransactionIndex());
			subTransactionSearchRequest.types(appConst.getSubTransactionIndexType());
			return subTransactionSearchRequest;
		}
		// subTransactionSearchRequest = new
		// SearchRequest(appConst.getSubTransactionIndex());
		// subTransactionSearchRequest.types(appConst.getSubTransactionIndexType());
		// return subTransactionSearchRequest;
	}

	/**
	 * Creates a list of data queried from elastic server
	 * 
	 * @param hits
	 *            SearchHit array from SearchResponse object
	 * @param length
	 *            No of data to insert in list; 0 for all data
	 * @param order
	 *            Sort Order of list accepts a type of SortOrder enum
	 * @return List<_TransactionModel>
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public List<_TransactionSourceModel> createSourceList(SearchHit[] hits, int length, SortOrder order)
			throws JsonParseException, JsonMappingException, IOException {
		List<_TransactionSourceModel> sourceList = new ArrayList<>();
		if (length == 0)
			length = hits.length;

		ObjectMapper objectMapper = new ObjectMapper();
		for (int i = 0; i < length; i++) {
			_TransactionSourceModel obj = objectMapper.readValue(hits[i].getSourceAsString(),
					_TransactionSourceModel.class);
			obj.setId(hits[i].getId());
			obj.setIndex(hits[i].getIndex());
			obj.setIndexType(hits[i].getType());
			sourceList.add(obj);
		}
		Collections.sort(sourceList, this.getComparator(order));
		return sourceList;
	}

	private Comparator<_TransactionSourceModel> getComparator(SortOrder order) {
		Comparator<_TransactionSourceModel> comparator = new Comparator<_TransactionSourceModel>() {

			@Override
			public int compare(_TransactionSourceModel o1, _TransactionSourceModel o2) {
				if (order == SortOrder.ASC)
					return o1.getEventSeqNo().compareTo(o2.getEventSeqNo());
				else
					return o2.getEventSeqNo().compareTo(o1.getEventSeqNo());
			}
		};
		return comparator;
	}

	public List<_TransactionSourceModel> createSourceListNoOrder(SearchHit[] hits, int length)
			throws JsonParseException, JsonMappingException, IOException {
		List<_TransactionSourceModel> sourceList = new ArrayList<>();
		if (length == 0)
			length = hits.length;

		ObjectMapper objectMapper = new ObjectMapper();
		for (int i = 0; i < length; i++) {
			sourceList.add(objectMapper.readValue(hits[i].getSourceAsString(), _TransactionSourceModel.class));
		}

		return sourceList;
	}
}
