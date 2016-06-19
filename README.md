
# Elasticsearch Conditional Boost Plugin

This plugin offers function score conditional boosting, depending on values of other fields in a document.

Note, conditional boosting is also possible by scripting. 

This plugin demonstrates how to implement a solution without scripting by extending the 
Elasticsearch function score module.

## Example

Imagine a shop wants to advertise non-sellers in the search list of the online shop without telling the 
visitors. Your boss comes in and in a harsh voice you hear the command, "Push those damned non-sellers 
out this month and keep the search response time low, or you are fired!"

Your idea is to use document boosting for changing the hit orders of the shop search lists. 
Normally, you would have to re-index all the content with adjusted boost values 
in a boost field, or use a script for function score, to mangle with the document fields, 
evaluating them, and change the boost.

With this plugin, you can fake the relevance ranking on the fly, without reindexing, but also without scripting.
 
Assume the product `product_name_1` has to appear in the top results, you can formulate
a boost condition with a specific boost value for `product_name_1`, and the score value will be modified 
for these documents.

Assume also a supplier `user_name_1` who has paid you an illegal extra bonus if you rank him
high. You could combine these boosts, many string field and string values can be specified 
to trigger a combined conditional document boost in a single query. 

If there is more than one match in the specified field values in a document, scores multiply each other. 
For smoothing the scoring function, you can use a `factor` and a `modifier` like
in field value factor score function, see [here](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html#_field_value_factor)

Full example:

    curl -XDELETE '0:9200/test'
    
    curl -XPUT '0:9200/test' -d '
    {
        "index.number_of_shards" : 2,
        "index.number_of_replicas" : 1
    }
    '
    
    curl -XPUT '0:9200/test/products/1' -d '
    {
        "content" : "foo bar",
        "product" : "product_name_1"
    }
    '
    
    curl -XPUT '0:9200/test/products/2' -d '
    {
        "content" : "foo bar",
        "product" : "product_name_2"
    }
    '
    
    curl -XPUT '0:9200/test/products/3' -d '
    {
        "content" : "foo bar",
        "product" : "product_name_3"
    }
    '
    
    curl -XPUT '0:9200/test/products/4' -d '
    {
        "content" : "foo bar",
        "product" : "product_name_1",
        "user" : "user_name_1"
    }
    '
    
    curl -XPUT '0:9200/test/products/5' -d '
    {
        "content" : "foo bar",
        "product" : "product_name_2",
        "user" : "user_name_1"
    }
    '
    
    curl -XPUT '0:9200/test/products/6' -d '
    {
        "content" : "foo bar",
        "product" : "product_name_3",
        "user" : "user_name_1"
    }
    '
    
    curl -XGET '0:9200/test/_refresh'
    
    curl -XGET '0:9200/test/products/_search' -d '
    {
      "query":{
        "function_score":{
          "query":{"term": { "content" : "foo"} },
          "cond_boost":{
            "cond":[
              {"product":"product_name_1","value":2.0},
              {"user":"user_name_1","value":3.0}
            ],
            "value":1.0,
            "factor":1.0,
            "modifier":"NONE"
          } 
        }
      },
      "explain":false
    }
    '

## Versions

| Plugin      | Elasticsearch version  | Release date |
| ----------- | ---------------------- | -------------|
| 2.3.3.0     | 2.3.3                  | Jun 19, 2016 |
| 1.4.0.0     | 1.4.0                  | Feb 19, 2015 |
| 1.2.1.0     | 1.2.1                  | Jun  7, 2014 |


## Installation 2.x

    ./bin/plugin install http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-condboost/2.3.3.0/elasticsearch-condboost-2.3.3.0-plugin.zip


## Installation

    ./bin/plugin -install condboost -url http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-condboost/1.4.0.0/elasticsearch-condboost-1.4.0.0.zip

Do not forget to restart the node after installing.

## Issues

All feedback is welcome! If you find issues, please post them at [Github](https://github.com/jprante/elasticsearch-condboost/issues)

# License

Elasticsearch Conditional Boost Plugin

Copyright (C) 2014 Jörg Prante

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.