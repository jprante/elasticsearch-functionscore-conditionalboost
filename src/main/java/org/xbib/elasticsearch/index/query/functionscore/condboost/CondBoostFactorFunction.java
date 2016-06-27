package org.xbib.elasticsearch.index.query.functionscore.condboost;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Explanation;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.LeafScoreFunction;
import org.elasticsearch.common.lucene.search.function.ScoreFunction;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.SortedBinaryDocValues;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryParseContext;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CondBoostFactorFunction extends ScoreFunction {

    private final QueryParseContext queryParseContext;

    private final List<CondBoostEntry> condBoostEntryList;

    private final float boostFactor;

    private final Modifier modifier;

    private final float defaultBoost;

    private float boost;

    CondBoostFactorFunction(QueryParseContext queryParseContext, List<CondBoostEntry> condBoostEntryList,
                            float defaultBoost, float boostFactor, Modifier modifierType) {
        super(CombineFunction.MULT);
        this.queryParseContext = queryParseContext;
        this.condBoostEntryList = condBoostEntryList;
        this.defaultBoost = defaultBoost;
        this.boostFactor = boostFactor;
        this.modifier = modifierType;
    }

    @Override
    public LeafScoreFunction getLeafScoreFunction(final LeafReaderContext ctx) throws IOException {
        return new LeafScoreFunction() {
            @Override
            public double score(int docId, float subQueryScore) {
                boost = defaultBoost;
                for (CondBoostEntry entry : condBoostEntryList) {
                    String currentFieldName = entry.fieldName;
                    MappedFieldType mappedFieldType = queryParseContext.mapperService().fullName(currentFieldName);
                    if (mappedFieldType == null) {
                        throw new ElasticsearchException("unable to find field [" + currentFieldName + "]");
                    }
                    IndexFieldData indexFieldData = queryParseContext.getForField(mappedFieldType);
                    SortedBinaryDocValues values = indexFieldData.load(ctx).getBytesValues();
                    values.setDocument(docId);
                    for (int i = 0; i < values.count(); i++) {
                        if (entry.fieldValue.equals(values.valueAt(i).utf8ToString())) {
                            boost = boost * entry.boost; // multiply boosts by default
                        }
                    }
                }
                double val = boost * boostFactor;
                double result = modifier.apply(val);
                if (Double.isNaN(result) || Double.isInfinite(result)) {
                    throw new ElasticsearchException("result of field modification [" + modifier.toString() +
                            "(" + val + ")] must be a number");
                }
                return result;
            }

            @Override
            public Explanation explainScore(int docId, Explanation subQueryScore) throws IOException {
                return Explanation.match(boost, "condboost");
            }
        };
    }

    @Override
    public boolean needsScores() {
        return false;
    }

    @Override
    public String toString() {
        return "condboost[" + boost + "]";
    }

    public enum Modifier {
        NONE {
            @Override
            public double apply(double n) {
                return n;
            }
        },
        LOG {
            @Override
            public double apply(double n) {
                return Math.log10(n);
            }
        },
        LOG1P {
            @Override
            public double apply(double n) {
                return Math.log10(n + 1);
            }
        },
        LOG2P {
            @Override
            public double apply(double n) {
                return Math.log10(n + 2);
            }
        },
        LN {
            @Override
            public double apply(double n) {
                return Math.log(n);
            }
        },
        LN1P {
            @Override
            public double apply(double n) {
                return Math.log1p(n);
            }
        },
        LN2P {
            @Override
            public double apply(double n) {
                return Math.log1p(n + 1);
            }
        },
        SQUARE {
            @Override
            public double apply(double n) {
                return Math.pow(n, 2);
            }
        },
        SQRT {
            @Override
            public double apply(double n) {
                return Math.sqrt(n);
            }
        },
        RECIPROCAL {
            @Override
            public double apply(double n) {
                return 1.0 / n;
            }
        };

        public abstract double apply(double n);

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ROOT);
        }
    }
}
