package com.logicoy.bpelmon.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models._AlertNotificationSourceModel;
import com.logicoy.bpelmon.models._SourceModel;
import com.logicoy.bpelmon.models._TransactionSourceModel;
import com.logicoy.bpelmon.services.ConnectionManager;

@Component
public class Utils {

	@Autowired
	private AppConstants appConst;
	@Autowired
	ConnectionManager connectionManager;
	private AppLogger LOGGER = new AppLogger(Logger.getLogger(this.getClass().getName()));

	private SearchRequest searchRequest;
	private SearchRequest alertSearchRequest;
	private SearchRequest notifiactionSearchRequest;
	int i = 0;

	/**
	 * Returns a client connection to elastic search
	 *
	 * @return An object of type RestHighLevelClient
	 */
	public RestHighLevelClient getElasticClient(String clientId) {
		// return connectionManager.getConnectionFromPool(clientId);
		return connectionManager.createElasticConnection();
	}

	// public RestHighLevelClient getElasticConnection(String clientId) {
	// return connectionManager.getConnectionFromPool(clientId);
	// }

	// public RestHighLevelClient getElasticClientBPEL() {
	// return connectionManager.getConnectionFromPool(null);
	// }

	// public RestHighLevelClient getElasticClientTransaction() {
	// return connectionManager.getConnectionFromPool(null);
	// }

	/**
	 * Creates a list of data from elastic query result.
	 *
	 * @param hits
	 *            SearchHit array from SearchResponse
	 * @param length
	 *            Length of data list. Pass 0 for entire data
	 * @return A list of type _SourceModel containing all data
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws Exception
	 *             Throws a exception of type JsonMapping or IO if malformed data
	 *             received.
	 */
	public List<_SourceModel> createSourceModelList(SearchHit[] hits, int length, SortOrder order)
			throws JsonParseException, JsonMappingException, IOException {
		List<_SourceModel> sourceList = new ArrayList<>();
		if (length == 0) {
			length = hits.length;
		}

		ObjectMapper objectMapper = new ObjectMapper();
		for (int i = 0; i < length; i++) {
			sourceList.add(objectMapper.readValue(hits[i].getSourceAsString(), _SourceModel.class));
		}
		Collections.sort(sourceList, this.getComparator(order));
		return sourceList;
	}

	/**
	 * Creates a search request for a particular index
	 *
	 * @return An object of type SearchRequest
	 */
	public SearchRequest createTransactionIndexSearchRequest(String clientId) {
		if (clientId != null) {
			clientId = clientId.toLowerCase().trim();
			System.out.println("client Id " + clientId);
			searchRequest = new SearchRequest(appConst.getTransactionIndex() + "_" + clientId);
			searchRequest.types(appConst.getTransactionIndexType() + "_" + clientId);
			// searchRequest.types(appConst.getTransactionIndexType());
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

	public SearchRequest createTransactionCountIndexSearchRequest(String clientId) {
		if (clientId != null) {
			clientId = clientId.toLowerCase().trim();
			searchRequest = new SearchRequest(appConst.getTransactionCountIndex() + "_" + clientId);
			searchRequest.types(appConst.getTransactionCountIndexType() + "_" + clientId);
			return searchRequest;
		} else {
			searchRequest = new SearchRequest(appConst.getTransactionCountIndex());
			searchRequest.types(appConst.getTransactionCountIndexType());
			return searchRequest;
		}
	}

	public SearchRequest createBpelMonIndexSearchRequest(String clientId) {

		if (clientId != null) {
			clientId = clientId.toLowerCase().trim();
			searchRequest = new SearchRequest(appConst.getIndex() + "_" + clientId);
			searchRequest.types(appConst.getIndexType() + "_" + clientId);
			// searchRequest.types(appConst.getIndexType());
			return searchRequest;
		} else {
			searchRequest = new SearchRequest(appConst.getIndex());
			searchRequest.types(appConst.getIndexType());
			return searchRequest;
		}
		// searchRequest = new SearchRequest(appConst.getIndex());
		// searchRequest.types(appConst.getIndexType());
		// return searchRequest;

	}

	public SearchRequest createBpelCountMgmtSearchRequest(String clientId) {

		SearchRequest searchRequest = null;
		if (clientId != null) {
			clientId = clientId.toLowerCase().trim();
			searchRequest = new SearchRequest(appConst.getBpelMonCountIndex() + "_" + clientId);
			searchRequest.types(appConst.getBpelMonCountIndexType() + "_" + clientId);
		} else {
			searchRequest = new SearchRequest(appConst.getBpelMonCountIndex());
			searchRequest.types(appConst.getBpelMonCountIndexType());
		}

		return searchRequest;
	}

	/**
	 * Create search request for alert or notification index
	 *
	 * @param clientId
	 *            Client id from request
	 * @param type
	 *            0 for alerts, 1 for notifications
	 * @return SearchRequest
	 */
	public SearchRequest createAlertNotificationSearchRequest(String clientId, int type) {

		switch (type) {
		case 0:
			// Alert Search request
			if (clientId != null) {
				clientId = clientId.toLowerCase().trim();
				alertSearchRequest = new SearchRequest(appConst.getBpelAlertIndex() + "_" + clientId.toLowerCase());
				alertSearchRequest.types(appConst.getBpelAlertIndex() + "_" + clientId + "_type");
				return alertSearchRequest;
			} else {
				alertSearchRequest = new SearchRequest(appConst.getBpelAlertIndex());
				alertSearchRequest.types(appConst.getBpelAlertIndex() + "_type");
				return alertSearchRequest;
			}
		case 1:
			// Notification search request
			if (clientId != null) {
				clientId = clientId.toLowerCase().trim();
				notifiactionSearchRequest = new SearchRequest(appConst.getBpelNotificationIndex() + "_" + clientId);
				notifiactionSearchRequest.types(appConst.getBpelNotificationIndex() + "_" + clientId + "_type");
				return notifiactionSearchRequest;

			} else {
				notifiactionSearchRequest = new SearchRequest(appConst.getBpelNotificationIndex());
				notifiactionSearchRequest.types(appConst.getBpelNotificationIndex() + "_type");
				return notifiactionSearchRequest;
			}
		default:
			return null;
		}
	}

	/**
	 * Create basic search builder with predefined options of sorting, fetchSource
	 * and size
	 *
	 * @param size
	 *            No of documents to fetch from elastic search
	 * @param fetchSource
	 *            A boolean indicating whether source should be fetched or not
	 * @param order
	 *            Type of SortOrder enum. ASC or DESC.
	 * @return An object of SearchSourceBuilder
	 */
	public SearchSourceBuilder createBasicSearchBuilder(int size, boolean fetchSource, SortOrder order) {
		SearchSourceBuilder builder = new SearchSourceBuilder();
		builder.sort("event_time", order);
		builder.fetchSource(fetchSource);
		builder.size(size != 0 ? size : 500000);
		builder.from(0);
		return builder;
	}

	/**
	 * Returns a formed range query on elastic search
	 *
	 * @param reqModel
	 *            Date parameters are created from Request Model
	 * @return
	 */
	public RangeQueryBuilder getRangeQuery(RequestModel reqModel) {
		return QueryBuilders.rangeQuery("event_time").gte(reqModel.getStartDate()).lte(reqModel.getEndDate());
	}

	public Comparator<_SourceModel> getComparator(SortOrder order) {
		Comparator<_SourceModel> comp = new Comparator<_SourceModel>() {

			@Override
			public int compare(_SourceModel o1, _SourceModel o2) {
				if (order == SortOrder.ASC) {
					return o1.getEventSeqNo().compareTo(o2.getEventSeqNo());
				} else {
					return o2.getEventSeqNo().compareTo(o1.getEventSeqNo());
				}
			}
		};
		return comp;
	}

	/**
	 * Combines data
	 * 
	 * @param startedDataList
	 * @param completedDataList
	 * @return
	 */
	public List<_TransactionSourceModel> clubShowableFiel(List<_TransactionSourceModel> startedDataList,
			Set<_TransactionSourceModel> completedDataList) {
		List<_TransactionSourceModel> tempStartedDataList = new ArrayList<>(startedDataList);
		Map<String, String> dataMap = new LinkedHashMap<>();
		List<_TransactionSourceModel> finalList = new ArrayList<>();
		LOGGER.logInfo("============================ Club showable field ============================");
		LOGGER.logInfo("Started data list size: " + startedDataList.size() + " Completed Data list size: "
				+ completedDataList.size());
		for (_TransactionSourceModel startedModel : tempStartedDataList) {
			String startedShowableData[] = startedModel.getShowableData().split("\\<\\|\\|\\>");
			LOGGER.logInfo("Started Showable data: " + Arrays.toString(startedShowableData));
			String finalShowableData = "";
			_TransactionSourceModel finalModel = null;
			long eventSeqNo = 0;
			for (_TransactionSourceModel completedModel : completedDataList) {
				LOGGER.logInfo("Previous event seqNo: " + eventSeqNo + " Current event seqNo: "
						+ completedModel.getEventSeqNo());

				if (startedModel.getInstanceId().equalsIgnoreCase(completedModel.getInstanceId()) && (startedModel
						.getPrimaryTrackingValue().equalsIgnoreCase(completedModel.getPrimaryTrackingValue())
						|| (startedModel.getPrimaryTrackingValue() == null
								&& completedModel.getPrimaryTrackingValue() == null))) {
					if (Long.parseLong(completedModel.getEventSeqNo()) > eventSeqNo) {
						eventSeqNo = Long.parseLong(completedModel.getEventSeqNo());
						String completedShowableData[] = completedModel.getShowableData().split("\\<\\|\\|\\>");
						LOGGER.logInfo("Completed showable data: " + Arrays.toString(completedShowableData));
						for (String startedShowable : startedShowableData) {
							if (!startedShowable.isEmpty()) {
								String splitData[] = startedShowable.split("(\\~\\#\\~)");
								dataMap.put(splitData[0].trim(), splitData[1]);
							}
						}
						for (String completedShowable : completedShowableData) {
							if (!completedShowable.isEmpty()) {
								String splitData[] = completedShowable.split("(\\~\\#\\~)");
								dataMap.put(splitData[0].trim(), splitData[1]);
							}
						}

						if (dataMap.keySet().size() > 0) {
							for (Map.Entry<String, String> entry : dataMap.entrySet()) {
								// Create value back in string form
								// LOGGER.logInfo("KEY: " + entry.getKey().trim() + " Value: " +
								// entry.getValue().trim());
								finalShowableData += "<||>" + entry.getKey().trim() + "~#~" + entry.getValue().trim();
							}
						}
						if (!finalShowableData.isEmpty() && finalShowableData.length() > 4) {
							finalShowableData = finalShowableData.substring(4);
							finalModel = completedModel;
							// _TransactionSourceModel finalData = startedModel;
							// finalData.setEventTime(completedModel.getEventTime());
							// finalData.setShowableData(finalShowableData);
							// finalData.setStatus(completedModel.getStatus());
							// completedModel.setShowableData(finalShowableData.substring(4));
						} else {
							// completedModel.setShowableData(finalShowableData);
							finalModel = completedModel;
						}
					}
				}
			}
			if (finalModel != null) {
				LOGGER.logInfo("Final showable data: " + finalShowableData);
				_TransactionSourceModel finalData = startedModel;
				finalData.setShowableData(finalShowableData);
				finalData.setEventTime(finalModel.getEventTime());
				finalData.setStatus(finalModel.getStatus());
				finalList.add(finalModel);
			}
		}
		return finalList;

	}

	public List<_TransactionSourceModel> clubSubTransactionShowableData(List<_TransactionSourceModel> dataList) {
		Map<String, String> dataMap = new HashMap<>();
		List<_TransactionSourceModel> tempList = new ArrayList<>(dataList);
		List<_TransactionSourceModel> finalList = new ArrayList<>();

		Collections.sort(tempList, new Comparator<_TransactionSourceModel>() {

			@Override
			public int compare(_TransactionSourceModel o1, _TransactionSourceModel o2) {
				return o1.getSecondaryTrackingValue().compareTo(o2.getSecondaryTrackingValue());
			}
		});

		for (int j = 0; j < tempList.size(); j = j + 2) {
			_TransactionSourceModel model1 = tempList.get(j);
			String upperShowableData[] = model1.getShowableData().split("\\<\\|\\|\\>");
			for (int i = j + 1; i < tempList.size(); i++) {
				_TransactionSourceModel model2 = tempList.get(i);
				if (model2.getSecondaryTrackingValue().equalsIgnoreCase(model1.getSecondaryTrackingValue())) {
					String lowerShowableData[] = model2.getShowableData().split("\\<\\|\\|\\>");
					for (String startedShowable : lowerShowableData) {
						if (!startedShowable.isEmpty()) {
							String splitData[] = startedShowable.split("(\\~\\#\\~)");
							dataMap.put(splitData[0], splitData[1]);
						}
					}
					for (String completedShowable : upperShowableData) {
						if (!completedShowable.isEmpty()) {
							String splitData[] = completedShowable.split("(\\~\\#\\~)");
							dataMap.put(splitData[0], splitData[1]);
						}
					}
					String finalShowableData = "";
					for (Map.Entry<String, String> entry : dataMap.entrySet()) {
						// Create value back in string from
						finalShowableData += "<||>" + entry.getKey().trim() + "~#~" + entry.getValue().trim();
					}
					// Update showableData value in completedModel
					model1.setShowableData(finalShowableData.substring(4));
					break;
				}
			}
			finalList.add(model1);
		}
		return finalList;
	}

	public List<_TransactionSourceModel> getRunningTransactions(List<_TransactionSourceModel> tempList) {
		List<_TransactionSourceModel> runningList = new ArrayList<>();
		int flag = 0;
		for (_TransactionSourceModel model : tempList) {
			flag = 0;
			for (_TransactionSourceModel compareModel : tempList) {
				if (compareModel.getInstanceId().equalsIgnoreCase(model.getInstanceId())
						&& compareModel.getPrimaryTrackingValue().equalsIgnoreCase(model.getPrimaryTrackingValue())) {
					flag += 1;
				}
			}
			if (flag == 1) {
				// Only one instance present. Service is running
				runningList.add(model);
			}
		}
		return runningList;
	}

	public Comparator<_TransactionSourceModel> getEventSequenceComparator() {
		return new Comparator<_TransactionSourceModel>() {

			@Override
			public int compare(_TransactionSourceModel o1, _TransactionSourceModel o2) {
				return o2.getEventSeqNo().compareTo(o1.getEventSeqNo());
			}
		};
	}

	public List<_AlertNotificationSourceModel> createAlertNotificationSourceModelList(SearchHit[] hits, int length,
			SortOrder order) throws JsonParseException, JsonMappingException, IOException {
		List<_AlertNotificationSourceModel> sourceList = new ArrayList<>();
		if (length == 0) {
			length = hits.length;
		}

		ObjectMapper objectMapper = new ObjectMapper();

		for (int i = 0; i < length; i++) {
			_AlertNotificationSourceModel obj = objectMapper.readValue(hits[i].getSourceAsString(),
					_AlertNotificationSourceModel.class);
			obj.setDocId(hits[i].getId());
			obj.setIndex(hits[i].getIndex());
			obj.setIndexType(hits[i].getType());
			sourceList.add(obj);
		}
		return sourceList;
	}

	public List<_TransactionSourceModel> getRunningTransactions(List<_TransactionSourceModel> startedDataList,
			Set<_TransactionSourceModel> completedDataList) {
		// Reverse startedList to match properly
		Collections.reverse(startedDataList);
		Set<_TransactionSourceModel> runningList = new HashSet<>();
		int flag = 0;
		for (_TransactionSourceModel model : startedDataList) {
			flag = 0;
			_TransactionSourceModel comparingModel = null;
			for (_TransactionSourceModel compareModel : completedDataList) {
				if (compareModel.getInstanceId().equalsIgnoreCase(model.getInstanceId())
						&& compareModel.getPrimaryTrackingValue().equalsIgnoreCase(model.getPrimaryTrackingValue())) {
					flag += 1;
					comparingModel = compareModel;
				}
			}
			if (flag == 0) {
				// Service is running
				runningList.add(model);
			} else {
				completedDataList.remove(comparingModel);
			}
		}
		List<_TransactionSourceModel> mList = new ArrayList<>(runningList);
		return mList;
	}

	/**
	 * Combine transactions with similar transaction id. Adds a history of showable
	 * data to the combined object
	 * 
	 * @param response
	 *            list from elastic search
	 * @return Combined list of type _TransactionSourceModel
	 */
	public List<_TransactionSourceModel> combineData(List<_TransactionSourceModel> dataList) {
		if (dataList.size() <= 0) {
			return new ArrayList<>();
		} else if (dataList.size() == 1) {
			return dataList;
		}
		// Collections.sort(dataList, new Comparator<_TransactionSourceModel>() {
		//
		// @Override
		// public int compare(_TransactionSourceModel o1, _TransactionSourceModel o2) {
		//
		// return o1.getEventSeqNo().compareTo(o2.getEventSeqNo());
		// }
		// });
		List<_TransactionSourceModel> mainList = new ArrayList<>();
		List<String> showableHistoryList = new ArrayList<>();
		for (int i = 0; i < dataList.size(); i++) {
			showableHistoryList = new ArrayList<>();

			_TransactionSourceModel outerData = dataList.get(i);
			long endTime = Long.parseLong(outerData.getEventSeqNo());
			long startTime = endTime;
			// Add showable data history

			showableHistoryList.add(outerData.getShowableData() + "<||>Status~#~" + outerData.getStatus()
					+ "<||>Event Time~#~" + outerData.getEventTime());

			Map<String, String> showableMap = new LinkedHashMap<>();
			
			String showableData[] = outerData.getShowableData().split("\\<\\|\\|\\>");
			for (String data : showableData) {
				if (!data.isEmpty()) {
					String splitData[] = data.split("(\\~\\#\\~)");
					// showableMap.put(splitData[0], splitData[1]);
					if (splitData.length > 1) {
						showableMap.put(splitData[0], splitData[1]);
					} else {
						showableMap.put(splitData[0], "UNKNOWN");
					}
				}
			}
			// LOGGER.logInfo("Outer showable Map: " + showableMap.keySet().toString());
			// LOGGER.logInfo("Outer TransactionID: " + outerData.getTransactionId());

			for (int j = i + 1; j < dataList.size(); j++) {
				_TransactionSourceModel innerData = dataList.get(j);
				// LOGGER.logInfo("Inner TransactionId: " + innerData.getTransactionId());
				if (outerData.getTransactionId().equals(innerData.getTransactionId())) {
					// Add matching showable data to hitory list
					long currentTime = Long.parseLong(innerData.getEventSeqNo());
					if (currentTime > startTime) {
						endTime = currentTime;
					}
					showableHistoryList.add(innerData.getShowableData() + "<||>Status~#~" + innerData.getStatus()
							+ "<||>Event Time~#~" + innerData.getEventTime());

					String innershowableData[] = innerData.getShowableData().split("\\<\\|\\|\\>");
					for (String data : innershowableData) {
						if (!data.isEmpty()) {
							String splitData[] = data.split("(\\~\\#\\~)");
							if (splitData.length > 1) {
								showableMap.put(splitData[0], splitData[1]);
							} else {
								showableMap.put(splitData[0], "UNKNOWN");
							}
						}
					}
					// LOGGER.logInfo("Inner Showable map: " + showableMap.keySet().toString());
					if (Long.parseLong(innerData.getEventSeqNo()) > Long.parseLong(outerData.getEventSeqNo())) {
						outerData.setEventSeqNo(innerData.getEventSeqNo());
						outerData.setEventTime(innerData.getEventTime());
						outerData.setStatus(innerData.getStatus());
					}
					dataList.remove(j);
					j = j - 1;
				}
			}
			// Add outer data to main list

			String finalShowableData = "";
			for (Map.Entry<String, String> entry : showableMap.entrySet()) {
				// Create value back in string from
				finalShowableData += "<||>" + entry.getKey().trim() + "~#~" + entry.getValue().trim();
			}
			if (finalShowableData.length() > 4) {
				outerData.setShowableData(finalShowableData.substring(4));
			} else {
				outerData.setShowableData(finalShowableData);
			}
			// Add Showable history to outerData
			// Collections.reverse(showableHistoryList);
			outerData.setShowableDataHistory(showableHistoryList);
			// LOGGER.logInfo("Primary tracking var: " + outerData.getPrimaryTrackingId() +
			// " StartTime: " + startTime + " End Time: " + endTime);
			outerData.setProcessingTime(Math.abs(endTime - startTime));
			mainList.add(outerData);
		}
		Collections.sort(mainList, this.getEventSequenceComparator());
		return mainList;
	}
	
	
	public List<_TransactionSourceModel> combineDataNew(List<_TransactionSourceModel> dataList) {
		if (dataList.size() <= 0) {
			return new ArrayList<>();
		} else if (dataList.size() == 1) {
			return dataList;
		}
		// Collections.sort(dataList, new Comparator<_TransactionSourceModel>() {
		//
		// @Override
		// public int compare(_TransactionSourceModel o1, _TransactionSourceModel o2) {
		//
		// return o1.getEventSeqNo().compareTo(o2.getEventSeqNo());
		// }
		// });
		List<_TransactionSourceModel> mainList = new ArrayList<>();
		List<String> showableHistoryList = new ArrayList<>();
		for (int i = 0; i < dataList.size(); i++) {
			showableHistoryList = new ArrayList<>();
			
			_TransactionSourceModel outerData = dataList.get(i);
			long endTime = Long.parseLong(outerData.getEventSeqNo());
			long startTime = endTime;
			// Add showable data history

			showableHistoryList.add(outerData.getShowableData() + "<||>Status~#~" + outerData.getStatus()
					+ "<||>Event Time~#~" + outerData.getEventTime());

			Map<String, String> showableMap = new LinkedHashMap<>();
			
			String showableData[] = outerData.getShowableData().split("\\<\\|\\|\\>");
			for (String data : showableData) {
				if (!data.isEmpty()) {
					String splitData[] = data.split("(\\~\\#\\~)");
					// showableMap.put(splitData[0], splitData[1]);
					if (splitData.length > 1) {
						showableMap.put(splitData[0], splitData[1]);
					} else {
						showableMap.put(splitData[0], "UNKNOWN");
					}
				}
			}
			// LOGGER.logInfo("Outer showable Map: " + showableMap.keySet().toString());
			// LOGGER.logInfo("Outer TransactionID: " + outerData.getTransactionId());

			for (int j = i + 1; j < dataList.size(); j++) {
				_TransactionSourceModel innerData = dataList.get(j);
				// LOGGER.logInfo("Inner TransactionId: " + innerData.getTransactionId());
				if (outerData.getTransactionId().equals(innerData.getTransactionId())) {
					
					// Add matching showable data to hitory list
					long currentTime = Long.parseLong(innerData.getEventSeqNo());
					if (currentTime > startTime) {
						endTime = currentTime;
					}
					showableHistoryList.add(innerData.getShowableData() + "<||>Status~#~" + innerData.getStatus()
							+ "<||>Event Time~#~" + innerData.getEventTime());

					String innershowableData[] = innerData.getShowableData().split("\\<\\|\\|\\>");
					for (String data : innershowableData) {
						if (!data.isEmpty()) {
							String splitData[] = data.split("(\\~\\#\\~)");
							if (splitData.length > 1) {
								showableMap.put(splitData[0], splitData[1]);
							} else {
								showableMap.put(splitData[0], "UNKNOWN");
							}
						}
					}
					// LOGGER.logInfo("Inner Showable map: " + showableMap.keySet().toString());
					if (Long.parseLong(innerData.getEventSeqNo()) > Long.parseLong(outerData.getEventSeqNo())) {
						outerData.setEventSeqNo(innerData.getEventSeqNo());
						outerData.setEventTime(innerData.getEventTime());
						outerData.setStatus(innerData.getStatus());
					}
					dataList.remove(j);
					j = j - 1;
				}
			}
			// Add outer data to main list

			String finalShowableData = "";
			for (Map.Entry<String, String> entry : showableMap.entrySet()) {
				// Create value back in string from
				finalShowableData += "<||>" + entry.getKey().trim() + "~#~" + entry.getValue().trim();
			}
			if (finalShowableData.length() > 4) {
				outerData.setShowableData(finalShowableData.substring(4));
			} else {
				outerData.setShowableData(finalShowableData);
			}
			// Add Showable history to outerData
			// Collections.reverse(showableHistoryList);
			outerData.setShowableDataHistory(showableHistoryList);
			// LOGGER.logInfo("Primary tracking var: " + outerData.getPrimaryTrackingId() +
			// " StartTime: " + startTime + " End Time: " + endTime);
			outerData.setProcessingTime(Math.abs(endTime - startTime));
			mainList.add(outerData);
		}
		Collections.sort(mainList, this.getEventSequenceComparator());
		return mainList;
	}

	public List<_TransactionSourceModel> combineDataReports(List<_TransactionSourceModel> dataList) {
		if (dataList.size() <= 0) {
			return new ArrayList<>();
		} else if (dataList.size() == 1) {
			return dataList;
		}
		// Collections.sort(dataList, new Comparator<_TransactionSourceModel>() {
		//
		// @Override
		// public int compare(_TransactionSourceModel o1, _TransactionSourceModel o2) {
		//
		// return o1.getEventSeqNo().compareTo(o2.getEventSeqNo());
		// }
		// });
		List<_TransactionSourceModel> mainList = new ArrayList<>();
		for (int i = 0; i < dataList.size(); i++) {
			

			_TransactionSourceModel outerData = dataList.get(i);
			long endTime = Long.parseLong(outerData.getEventSeqNo());
			long startTime = endTime;

			Map<String, String> showableMap = new LinkedHashMap<>();
			
			String showableData[] = outerData.getShowableData().split("\\<\\|\\|\\>");
			for (String data : showableData) {
				if (!data.isEmpty()) {
					String splitData[] = data.split("(\\~\\#\\~)");
					// showableMap.put(splitData[0], splitData[1]);
					if (splitData.length > 1) {
						showableMap.put(splitData[0], splitData[1]);
					} else {
						showableMap.put(splitData[0], "UNKNOWN");
					}
				}
			}
			// LOGGER.logInfo("Outer showable Map: " + showableMap.keySet().toString());
			// LOGGER.logInfo("Outer TransactionID: " + outerData.getTransactionId());

			for (int j = i + 1; j < dataList.size(); j++) {
				_TransactionSourceModel innerData = dataList.get(j);
				// LOGGER.logInfo("Inner TransactionId: " + innerData.getTransactionId());
				if (outerData.getTransactionId().equals(innerData.getTransactionId())) {
					// Add matching showable data to hitory list
					long currentTime = Long.parseLong(innerData.getEventSeqNo());
					if (currentTime > startTime) {
						endTime = currentTime;
					}
					
					String innershowableData[] = innerData.getShowableData().split("\\<\\|\\|\\>");
					for (String data : innershowableData) {
						if (!data.isEmpty()) {
							String splitData[] = data.split("(\\~\\#\\~)");
							if (splitData.length > 1) {
								showableMap.put(splitData[0], splitData[1]);
							} else {
								showableMap.put(splitData[0], "UNKNOWN");
							}
						}
					}
					// LOGGER.logInfo("Inner Showable map: " + showableMap.keySet().toString());
					if (Long.parseLong(innerData.getEventSeqNo()) > Long.parseLong(outerData.getEventSeqNo())) {
						outerData.setEventSeqNo(innerData.getEventSeqNo());
						outerData.setEventTime(innerData.getEventTime());
						outerData.setStatus(innerData.getStatus());
					}
					dataList.remove(j);
					j = j - 1;
				}
			}
			// Add outer data to main list

			String finalShowableData = "";
			for (Map.Entry<String, String> entry : showableMap.entrySet()) {
				// Create value back in string from
				finalShowableData += "<||>" + entry.getKey().trim() + "~#~" + entry.getValue().trim();
			}
			if (finalShowableData.length() > 4) {
				outerData.setShowableData(finalShowableData.substring(4));
			} else {
				outerData.setShowableData(finalShowableData);
			}
			mainList.add(outerData);
		}
		Collections.sort(mainList, this.getEventSequenceComparator());
		return mainList;
	}

	public List<_TransactionSourceModel> combineSubTransactionData(List<_TransactionSourceModel> dataList) {
		LOGGER.logInfo("Combining data");
		if (dataList.size() <= 0) {
			return new ArrayList<>();
		} else if (dataList.size() == 1) {
			return dataList;
		}
		Collections.sort(dataList, new Comparator<_TransactionSourceModel>() {

			@Override
			public int compare(_TransactionSourceModel o1, _TransactionSourceModel o2) {
				return o1.getEventSeqNo().compareTo(o2.getEventSeqNo());
			}
		});

		List<_TransactionSourceModel> mainList = new ArrayList<>();

		for (int i = 0; i < dataList.size(); i++) {

			_TransactionSourceModel outerData = dataList.get(i);
			LOGGER.logInfo("Outer showable id: " + outerData.getSecondaryTrackingValue());
			// Add showable data history
			Map<String, String> showableMap = new HashMap<>();

			String showableData[] = outerData.getShowableData().split("\\<\\|\\|\\>");
			for (String data : showableData) {
				if (!data.isEmpty()) {
					String splitData[] = data.split("(\\~\\#\\~)");
					// showableMap.put(splitData[0], splitData[1]);
					if (splitData.length > 1) {
						showableMap.put(splitData[0], splitData[1]);
					} else {
						showableMap.put(splitData[0], "UNKNOWN");
					}
				}
			}
			// LOGGER.logInfo("Outer showable Map: " + showableMap.keySet().toString());
			// LOGGER.logInfo("Outer TransactionID: " + outerData.getTransactionId());

			for (int j = i + 1; j < dataList.size(); j++) {
				_TransactionSourceModel innerData = dataList.get(j);
				// LOGGER.logInfo("Inner TransactionId: " + innerData.getTransactionId());
				if (outerData.getSecondaryTrackingValue().equalsIgnoreCase(innerData.getSecondaryTrackingValue())) {
					// Add matching showable data to history list

					String innershowableData[] = innerData.getShowableData().split("\\<\\|\\|\\>");
					for (String data : innershowableData) {
						if (!data.isEmpty()) {
							String splitData[] = data.split("(\\~\\#\\~)");
							if (splitData.length > 1) {
								showableMap.put(splitData[0], splitData[1]);
							} else {
								showableMap.put(splitData[0], "UNKNOWN");
							}
						}
					}
					// LOGGER.logInfo("Inner Showable map: " + showableMap.keySet().toString());
					dataList.remove(j);
					j = j - 1;
				}
			}
			// Add outer data to main list

			String finalShowableData = "";
			for (Map.Entry<String, String> entry : showableMap.entrySet()) {
				// Create value back in string from
				finalShowableData += "<||>" + entry.getKey().trim() + "~#~" + entry.getValue().trim();
			}
			if (finalShowableData.length() > 4) {
				outerData.setShowableData(finalShowableData.substring(4));
			} else {
				outerData.setShowableData(finalShowableData);
			}

			mainList.add(outerData);
		}
		Collections.sort(mainList, this.getEventSequenceComparator());
		return mainList;
	}

	public void invokeGC() {
		// IMPORTANT - DON'T UNCOMMENT
		// System.gc();
	}
	/**
	 * Not implemented for current architecture
	 */
	public void closeElasticConnection() {
		// ConnectionManager.closeElasticConnection();
	}
	public void reInitializeElasticConnection() {
		connectionManager.reinitializeConnection();
	}
	public Map<String, List<_TransactionSourceModel>> createServiceWiseMap(
			List<_TransactionSourceModel> transactionList) {
		Map<String, List<_TransactionSourceModel>> serviceMap = new HashMap<>();

		transactionList.forEach(transaction -> {
			String key = transaction.getSuName();
			List<_TransactionSourceModel> containedList = serviceMap.get(key);
			if (containedList != null) {
				// Data already for this service name. Append
				containedList.add(transaction);
				serviceMap.put(key, containedList);
			} else {
				// No data for this service
				List<_TransactionSourceModel> newList = new ArrayList<>();
				newList.add(transaction);
				serviceMap.put(key, newList);
			}
		});

		return serviceMap;
	}

}
