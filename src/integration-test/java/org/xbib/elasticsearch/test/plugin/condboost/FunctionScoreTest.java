package org.xbib.elasticsearch.test.plugin.condboost;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;

import java.util.Arrays;

import static org.elasticsearch.client.Requests.searchRequest;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.weightFactorFunction;
import static org.elasticsearch.search.builder.SearchSourceBuilder.searchSource;

public class FunctionScoreTest {

    public void testFunctionScore() {
        SearchRequest searchRequest = searchRequest()
                .source(searchSource().query(new FunctionScoreQueryBuilder(matchQuery("party_id", "12"))
                        .add(termsQuery("course_cd", Arrays.asList("writ100", "writ112", "writ113")),
                                weightFactorFunction(3.0f))));
    }
}
