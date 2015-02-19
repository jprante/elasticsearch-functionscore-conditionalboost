package org.xbib.elasticsearch.test.plugin.condboost;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;
import org.xbib.elasticsearch.index.query.functionscore.condboost.CondBoostFactorFunction;
import org.xbib.elasticsearch.index.query.functionscore.condboost.CondBoostFactorFunctionBuilder;
import org.xbib.elasticsearch.plugin.condboost.CondBoostPlugin;
import org.xbib.elasticsearch.test.AbstractNodeTestHelper;

import static org.elasticsearch.client.Requests.indexRequest;
import static org.elasticsearch.client.Requests.searchRequest;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.functionScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.search.builder.SearchSourceBuilder.searchSource;
import static org.junit.Assert.assertEquals;

public class FunctionScorePluginTest extends AbstractNodeTestHelper {

    @Override
    protected Settings getNodeSettings() {
        return settingsBuilder()
                .put("plugin.types", CondBoostPlugin.class.getName())
                .put(super.getNodeSettings())
                .build();
    }

    @Test
    public void testPlugin() throws Exception {
        client().admin()
                .indices()
                .prepareCreate("test")
                .execute().actionGet();

        client().admin().cluster().prepareHealth()
                .setWaitForEvents(Priority.LANGUID).setWaitForYellowStatus().execute().actionGet();

        client().index(
                indexRequest("test").type("products").id("1")
                        .source(jsonBuilder().startObject()
                                .field("content", "foo bar")
                                .field("product", "product_name_1")
                                .endObject())).actionGet();
        client().index(
                indexRequest("test").type("products").id("2")
                        .source(jsonBuilder().startObject()
                                .field("content", "foo bar")
                                .field("product", "product_name_2")
                                .endObject())).actionGet();
        client().index(
                indexRequest("test").type("products").id("3")
                        .source(jsonBuilder().startObject()
                                .field("content", "foo bar")
                                .field("product", "product_name_3")
                                .endObject())).actionGet();

        client().index(
                indexRequest("test").type("products").id("4")
                        .source(jsonBuilder().startObject()
                                .field("content", "foo bar")
                                .field("product", "product_name_1")
                                .field("user", "user_name_1")
                                .endObject())).actionGet();
        client().index(
                indexRequest("test").type("products").id("5")
                        .source(jsonBuilder().startObject()
                                .field("content", "foo bar")
                                .field("product", "product_name_2")
                                .field("user", "user_name_1")
                                .endObject())).actionGet();
        client().index(
                indexRequest("test").type("products").id("6")
                        .source(jsonBuilder().startObject()
                                .field("content", "foo bar")
                                .field("product", "product_name_3")
                                .field("user", "user_name_1")
                                .endObject())).actionGet();

        client().admin().indices().prepareRefresh().execute().actionGet();

        CondBoostFactorFunctionBuilder cbfb = new CondBoostFactorFunctionBuilder()
                .factor(1.0f)
                .modifier(CondBoostFactorFunction.Modifier.NONE)
                .condBoost("product", "product_name_1", 2.0f)
                .condBoost("user", "user_name_1", 3.0f);

        SearchRequest searchRequest = searchRequest()
                .searchType(SearchType.QUERY_THEN_FETCH)
                .source(searchSource()
                        .explain(false)
                        .query(functionScoreQuery(termQuery("content", "foo")).add(cbfb)));

        SearchResponse sr = client().search(searchRequest).actionGet();
        SearchHits sh = sr.getHits();

        /*for (int i = 0; i < sh.hits().length; i++) {
            System.err.println( sh.getAt(i).getId() + " " + sh.getAt(i).getScore() + " -->" + sh.getAt(i).getSource());
        }*/

        assertEquals(sh.hits().length, 6);
        assertEquals(sh.getAt(0).getId(), "4");
        assertEquals(sh.getAt(1).getId(), "5");
        assertEquals(sh.getAt(2).getId(), "6");
        assertEquals(sh.getAt(3).getId(), "1");



    }



}
