package com.logicoy.bpelmon.models;

import java.util.List;

public class UniqueBpelPaginatedResponse extends BasePaginatedResponse {

	private List<BpelUniqueInstanceData> data;

	public List<BpelUniqueInstanceData> getData() {
		return data;
	}

	public void setData(List<BpelUniqueInstanceData> data) {
		this.data = data;
	}

}
