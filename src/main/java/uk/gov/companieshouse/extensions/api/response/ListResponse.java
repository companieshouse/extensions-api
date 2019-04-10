package uk.gov.companieshouse.extensions.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ListResponse {

    private String etag;

    @JsonProperty("items_per_page")
    private int itemsPerPage;

    @JsonProperty("start_index")
    private int startIndex;

    @JsonProperty("total_results")
    private int totalResults;

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}
