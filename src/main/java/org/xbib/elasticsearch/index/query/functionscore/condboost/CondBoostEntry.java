package org.xbib.elasticsearch.index.query.functionscore.condboost;

import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

class CondBoostEntry implements ToXContent {

    public final static String BOOST = "value";

    String fieldName;

    String fieldValue;

    float boost;

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject()
                .field(fieldName, fieldValue)
                .field(BOOST, boost)
                .endObject();
        return builder;
    }

    public String toString() {
        return "{" + fieldName + "=" + fieldValue + "},{"+BOOST+"=" + boost + "}";
    }
}