
package org.xbib.elasticsearch.index.query.functionscore.condboost;

import org.elasticsearch.common.lucene.search.function.ScoreFunction;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParsingException;
import org.elasticsearch.index.query.functionscore.ScoreFunctionParser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Parses out a function_score function that looks like:
 *
 * <pre>
 *     {
 *         "cond_boost": {
 *             "value" : 1.0,
 *             "factor" : 1.0,
 *             "modifier" : "NONE",
 *             "cond" : [
 *               {
 *                 "product" : "product_name_1",
 *                 "value" : 5.0
 *               },
 *               {
 *                 "user" : "user_1",
 *                 "value" : 10.0
 *               }
 *             ]
 *         }
 *     }
 * </pre>
 */
public class CondBoostFactorFunctionParser implements ScoreFunctionParser {

    static String[] NAMES = { "cond_boost", "condBoost" };

    @Override
    public String[] getNames() {
        return NAMES;
    }

    @Override
    public ScoreFunction parse(QueryParseContext parseContext, XContentParser parser) throws IOException, QueryParsingException {
        String currentFieldName = null;
        List<CondBoostEntry> condArray = new LinkedList<>();
        float defaultBoost = 1.0f;
        float boostFactor = 1.0f;
        CondBoostFactorFunction.Modifier modifier = CondBoostFactorFunction.Modifier.NONE;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_ARRAY) {
                condArray = parseCondArray(parseContext, parser, currentFieldName);
            } else if (token.isValue()) {
                if (currentFieldName != null) {
                    switch (currentFieldName) {
                        case CondBoostEntry.BOOST:
                            defaultBoost = parser.floatValue();
                            break;
                        case "factor":
                            boostFactor = parser.floatValue();
                            break;
                        case "modifier":
                            modifier = CondBoostFactorFunction.Modifier.valueOf(parser.text().toUpperCase(Locale.ROOT));
                            break;
                        default:
                            throw new QueryParsingException(parseContext, NAMES[0] + " query does not support [" + currentFieldName + "]");
                    }
                }
            }
        }
        return new CondBoostFactorFunction(parseContext, condArray, defaultBoost, boostFactor, modifier);
    }

    private List<CondBoostEntry> parseCondArray(QueryParseContext parseContext, XContentParser parser, String currentFieldName) throws IOException {
        XContentParser.Token token;
        List<CondBoostEntry> condArray = new LinkedList<>();
        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
            if (token != XContentParser.Token.START_OBJECT) {
                throw new QueryParsingException(parseContext, "malformed query, expected a "
                        + XContentParser.Token.START_OBJECT + " while parsing cond boost array, but got a " + token);
            } else {
                CondBoostEntry entry = new CondBoostEntry();
                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        currentFieldName = parser.currentName();
                    } else {
                        if (CondBoostEntry.BOOST.equals(currentFieldName)) {
                            entry.boost = parser.floatValue();
                        } else {
                            entry.fieldName = currentFieldName;
                            entry.fieldValue = parser.text();
                        }
                    }
                }
                condArray.add(entry);
            }
        }
        return condArray;
    }

}
