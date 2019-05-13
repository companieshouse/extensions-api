package uk.gov.companieshouse.extensions.api.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ListResponse<T> {

    private String etag;
    @JsonProperty("items_per_page")
    private int itemsPerPage;
    @JsonProperty("start_index")
    private int startIndex;
    @JsonProperty("total_results")
    private int totalResults;
    private List<T> items;

    public ListResponse(String etag, int itemsPerPage, int startIndex,
                        int totalResults, List<T> items) {
        this.etag = etag;
        this.items = items;
        this.itemsPerPage = itemsPerPage;
        this.startIndex = startIndex;
        this.totalResults = totalResults;
    }

    public List<T> getItems() {
        return items;
    }

    public String getEtag() {
        return etag;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getTotalResults() {
        return totalResults;
    }

    @JsonIgnore
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
    public static class Builder<T> {
        private String etag;
        @JsonProperty("items_per_page")
        private int itemsPerPage;
        @JsonProperty("start_index")
        private int startIndex;
        @JsonProperty("total_results")
        private int totalResults;
        private List<T> items;

        public Builder<T> withEtag(String etag) {
            this.etag = etag;
            return this;
        }

        public Builder<T> withItemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }

        public Builder<T> withStartIndex(int startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public Builder<T> withItems(List<T> items) {
            this.items = items;
            return this;
        }

        public ListResponse<T> build() {
            if (items == null) {
                items = new ArrayList<>();
            }
            totalResults = items.size();
            return new ListResponse<T>(etag, itemsPerPage, startIndex, totalResults, items);
        }
    }
}
