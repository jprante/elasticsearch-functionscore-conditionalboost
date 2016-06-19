package org.xbib.elasticsearch.index.query.functionscore.condboost;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class CondBoostFactorFunctionBuilder extends ScoreFunctionBuilder {

    private Float value = 1.0f;

    private Float factor = 1.0f;

    private CondBoostFactorFunction.Modifier modifier = CondBoostFactorFunction.Modifier.NONE;

    private List<CondBoostEntry> entryList = new LinkedList<>();

    @Override
    public String getName() {
        return CondBoostFactorFunctionParser.NAMES[0];
    }

    public CondBoostFactorFunctionBuilder condBoost(String fieldName,
                                                    String fieldValue, float boost) {
        CondBoostEntry entry = new CondBoostEntry();
        entry.fieldName = fieldName;
        entry.fieldValue = fieldValue;
        entry.boost = boost;
        entryList.add(entry);
        return this;
    }

    public CondBoostFactorFunctionBuilder value(float boost) {
        this.value = boost;
        return this;
    }

    public CondBoostFactorFunctionBuilder factor(float boostFactor) {
        this.factor = boostFactor;
        return this;
    }

    public CondBoostFactorFunctionBuilder modifier(CondBoostFactorFunction.Modifier modifier) {
        this.modifier = modifier;
        return this;
    }

    @Override
    public void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(getName());
        if (entryList != null) {
            builder.startArray("cond");
            for (CondBoostEntry entry : entryList) {
                entry.toXContent(builder, params);
            }
            builder.endArray();
        }
        if (value != null) {
            builder.field("value", value);
        }
        if (factor != null) {
            builder.field("factor", factor);
        }
        if (modifier != null) {
            builder.field("modifier", modifier.toString().toLowerCase(Locale.ROOT));
        }
        builder.endObject();
    }
}
