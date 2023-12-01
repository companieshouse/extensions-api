package uk.gov.companieshouse.extensions.api.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Tag("UnitTest")
public class ListResponseUnitTest {

    @Test
    public void canBuildASpecificListResponse() {
        ListResponse<String> response = ListResponse.<String>builder()
            .withEtag("etag")
            .withItemsPerPage(10)
            .withStartIndex(0)
            .withItems(Arrays.asList("item1", "item2"))
            .build();

        Assertions.assertEquals(10, response.getItemsPerPage());
        Assertions.assertEquals(0, response.getStartIndex());
        Assertions.assertEquals(2, response.getTotalResults());
        Assertions.assertEquals("etag", response.getEtag());
        Assertions.assertArrayEquals(new String[]{"item1", "item2"},
            response.getItems().toArray());
    }

    @Test
    public void willReturnEmptyItemsIfNotSet() {
        ListResponse<String> response = ListResponse.<String>builder().build();

        assertEquals(0, response.getTotalResults());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    public void builderWillNotBePresentInJsonResponse() throws JsonProcessingException {
        String response = new ObjectMapper().writeValueAsString(ListResponse.builder()
            .withEtag("etag")
            .withItemsPerPage(10)
            .withStartIndex(0)
            .withItems(Arrays.asList("item1"))
            .build());

        assertEquals("{" +
            "\"etag\":\"etag\"," +
            "\"items\":[\"item1\"]," +
            "\"items_per_page\":10," +
            "\"start_index\":0," +
            "\"total_results\":1}", response);
    }
}
