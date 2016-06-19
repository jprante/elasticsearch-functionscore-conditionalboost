package org.xbib.elasticsearch.plugin.condboost;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.search.SearchModule;
import org.xbib.elasticsearch.index.query.functionscore.condboost.CondBoostFactorFunctionParser;

public class CondBoostPlugin extends Plugin {

    @Override
    public String name() {
        return "condboost";
    }

    @Override
    public String description() {
        return "Conditional boost plugin";
    }

    public void onModule(SearchModule searchModule) {
        searchModule.registerFunctionScoreParser(CondBoostFactorFunctionParser.class);
    }

}
