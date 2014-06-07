package org.xbib.elasticsearch.index.query.functionscore.condboost;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.ScoreFunction;
import org.elasticsearch.index.fielddata.BytesValues;

import java.util.List;
import java.util.Locale;

import static org.elasticsearch.common.collect.Lists.newLinkedList;

public class CondBoostFactorFunction extends ScoreFunction {

    private AtomicReaderContext context;

    private final List<CondBoostEntry> condBoostEntryList;

    private final float boostFactor;

    private final Modifier modifier;

    private double defaultBoost;

    private double boost;

    private List<CondBoostEntry> appliedCondBoostEntryList;

    public CondBoostFactorFunction(List<CondBoostEntry> condBoostEntryList,
                                   float defaultBoost, float boostFactor, Modifier modifierType) {
        super(CombineFunction.MULT);
        this.condBoostEntryList = condBoostEntryList;
        this.defaultBoost = defaultBoost;
        this.boostFactor = boostFactor;
        this.modifier = modifierType;
    }

    @Override
    public void setNextReader(AtomicReaderContext context) {
        this.context = context;
    }

    @Override
    public double score(int docId, float subQueryScore) {
        this.boost = defaultBoost;
        this.appliedCondBoostEntryList = newLinkedList();
        for (CondBoostEntry entry : condBoostEntryList) {
            BytesValues values = entry.ifd.load(context).getBytesValues(true);
            final int numValues = values.setDocument(docId);
            for (int i = 0; i < numValues; i++) {
                BytesRef value = values.nextValue();
                if (entry.fieldValue.equals(value.utf8ToString())) {
                    this.boost = this.boost * entry.boost; // multiply boosts by default
                    appliedCondBoostEntryList.add(entry);
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
    public Explanation explainScore(int docId, Explanation subQueryExpl) {
        Explanation exp = new Explanation();
        String modifierStr = modifier != null ? modifier.toString() : "";
        double score = score(docId, subQueryExpl.getValue());
        exp.setValue(CombineFunction.toFloat(score));
        exp.setDescription("cond boost function: " +
                modifierStr + "(" + appliedCondBoostEntryList + "->value " + boost + "  * factor=" + boostFactor + ")");
        exp.addDetail(subQueryExpl);
        return exp;
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
