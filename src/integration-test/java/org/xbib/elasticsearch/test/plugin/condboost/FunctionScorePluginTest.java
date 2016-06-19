package org.xbib.elasticsearch.test.plugin.condboost;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Priority;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;
import org.xbib.elasticsearch.index.query.functionscore.condboost.CondBoostFactorFunction;
import org.xbib.elasticsearch.index.query.functionscore.condboost.CondBoostFactorFunctionBuilder;
import org.xbib.elasticsearch.test.NodeTestUtils;

import static org.elasticsearch.client.Requests.indexRequest;
import static org.elasticsearch.client.Requests.searchRequest;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.functionScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.search.builder.SearchSourceBuilder.searchSource;
import static org.junit.Assert.assertEquals;

public class FunctionScorePluginTest extends NodeTestUtils {

    @Test
    public void testPlugin() throws Exception {
        client("1").admin()
                .indices()
                .prepareCreate("test")
                .execute().actionGet();

        client("1").admin().cluster().prepareHealth()
                .setWaitForEvents(Priority.LANGUID).setWaitForYellowStatus().execute().actionGet();

        client("1").index(
                indexRequest("test").type("products").id("1")
                        .source(jsonBuilder().startObject()
                                .field("content", "foo bar")
                                .field("product", "product_name_1")
                                .endObject())).actionGet();
        client("1").index(
                indexRequest("test").type("products").id("2")
                        .source(jsonBuilder().startObject()
                                .field("content", "foo bar")
                                .field("product", "product_name_2")
                                .endObject())).actionGet();
        client("1").index(
                indexRequest("test").type("products").id("3")
                        .source(jsonBuilder().startObject()
                                .field("content", "foo bar")
                                .field("product", "product_name_3")
                                .endObject())).actionGet();

        client("1").index(
                indexRequest("test").type("products").id("4")
                        .source(jsonBuilder().startObject()
                                .field("content", "foo bar")
                                .field("product", "product_name_1")
                                .field("user", "user_name_1")
                                .endObject())).actionGet();
        client("1").index(
                indexRequest("test").type("products").id("5")
                        .source(jsonBuilder().startObject()
                                .field("content", "foo bar")
                                .field("product", "product_name_2")
                                .field("user", "user_name_1")
                                .endObject())).actionGet();
        client("1").index(
                indexRequest("test").type("products").id("6")
                        .source(jsonBuilder().startObject()
                                .field("content", "foo bar")
                                .field("product", "product_name_3")
                                .field("user", "user_name_1")
                                .endObject())).actionGet();

        client("1").admin().indices().prepareRefresh().execute().actionGet();

        CondBoostFactorFunctionBuilder cbfb = new CondBoostFactorFunctionBuilder()
                .factor(1.0f)
                .modifier(CondBoostFactorFunction.Modifier.NONE)
                .condBoost("product", "product_name_1", 2.0f)
                .condBoost("user", "user_name_1", 3.0f);

        SearchRequest searchRequest = searchRequest()
                .source(searchSource()
                        .explain(false)
                        .query(functionScoreQuery(matchAllQuery()).add(cbfb)));

        SearchResponse sr = client("1").search(searchRequest).actionGet();
        SearchHits sh = sr.getHits();

        //for (int i = 0; i < sh.hits().length; i++) {
        //     System.err.println( sh.getAt(i).getId() + " " + sh.getAt(i).getScore() + " -->" + sh.getAt(i).getSource());
        //}

        assertEquals(sh.hits().length, 6);

        assertEquals("4", sh.getAt(0).getId());
        assertEquals("5", sh.getAt(1).getId());
        assertEquals("6", sh.getAt(2).getId());

        assertEquals("1", sh.getAt(3).getId());
        assertEquals("2", sh.getAt(4).getId());
        assertEquals("3", sh.getAt(5).getId());
    }
}
