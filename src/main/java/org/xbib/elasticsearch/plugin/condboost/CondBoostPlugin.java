package org.xbib.elasticsearch.plugin.condboost;

import org.elasticsearch.index.query.functionscore.FunctionScoreModule;
import org.elasticsearch.plugins.AbstractPlugin;
import org.xbib.elasticsearch.index.query.functionscore.condboost.CondBoostFactorFunctionParser;

public class CondBoostPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "condboost-"
                + Build.getInstance().getVersion() + "-"
                + Build.getInstance().getShortHash();
    }

    @Override
    public String description() {
        return "Conditional boost plugin";
    }

    public void onModule(FunctionScoreModule scoreModule) {
        scoreModule.registerParser(CondBoostFactorFunctionParser.class);
    }

}
