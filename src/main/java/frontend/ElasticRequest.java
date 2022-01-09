package frontend;

public class ElasticRequest {

	private String elasticIndex;
	private String elasticIndexType;
	private String clientId;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getElasticIndex() {
		return elasticIndex;
	}

	public void setElasticIndex(String elasticIndex) {
		this.elasticIndex = elasticIndex;
	}

	public String getElasticIndexType() {
		return elasticIndexType;
	}

	public void setElasticIndexType(String elasticIndexType) {
		this.elasticIndexType = elasticIndexType;
	}

}
