package org.xbib.elasticsearch.index.query.functionscore.condboost;

import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.fielddata.IndexFieldData;

import java.io.IOException;

class CondBoostEntry implements ToXContent {

    String fieldName;

    String fieldValue;

    float boost;

    IndexFieldData ifd;

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject()
                .field(fieldName, fieldValue)
                .field("value", boost)
                .endObject();
        return builder;
    }

    public String toString() {
        return "{" + fieldName + "=" + fieldValue + "},{value=" + boost + "}";
    }
}