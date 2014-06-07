
package org.xbib.elasticsearch.test;

import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public abstract class AbstractNodeTestHelper {

    protected final static ESLogger logger = ESLoggerFactory.getLogger("test");

    private Map<String, Node> nodes = newHashMap();

    private Map<String, Client> clients = newHashMap();

    private AtomicInteger counter = new AtomicInteger();

    private String cluster;

    private String host;

    private int port;

    protected void setClusterName() {
        this.cluster = "test-cluster-" + NetworkUtils.getLocalAddress().getHostName() + "-" + counter.incrementAndGet();
    }

    protected String getClusterName() {
        return cluster;
    }

    protected String getHost() {
        return host;
    }

    protected int getPort() {
        return port;
    }

    protected Settings getNodeSettings() {
        return ImmutableSettings
                .settingsBuilder()
                .put("cluster.name", getClusterName())
                .put("index.number_of_shards", 2)
                .put("index.number_of_replicas", 1)
                .put("cluster.routing.schedule", "50ms")
                .put("gateway.type", "none")
                .put("index.store.type", "ram")
                .put("http.enabled", false)
                .put("discovery.zen.multicast.enabled", false)
                .put("threadpool.bulk.queue_size", 200)
                .build();
    }

    @Before
    public void startNodes() throws Exception {
        setClusterName();
        startNode("1");
        // find node address
        NodesInfoRequest nodesInfoRequest = new NodesInfoRequest().transport(true);
        NodesInfoResponse response = client("1").admin().cluster().nodesInfo(nodesInfoRequest).actionGet();
        Object obj = response.iterator().next().getTransport().getAddress()
                .publishAddress();
        if (obj instanceof InetSocketTransportAddress) {
            InetSocketTransportAddress address = (InetSocketTransportAddress) obj;
            host = address.address().getHostName();
            port = address.address().getPort();
        }
    }

    @After
    public void stopNodes() throws Exception {
        closeAllNodes();
    }

    protected Node startNode(String id) {
        return buildNode(id).start();
    }

    private Node buildNode(String id) {
        String settingsSource = getClass().getName().replace('.', '/') + ".yml";
        Settings finalSettings = settingsBuilder()
                .loadFromClasspath(settingsSource)
                .put(getNodeSettings())
                .put("name", id)
                .build();
        Node node = nodeBuilder().settings(finalSettings).build();
        Client client = node.client();
        nodes.put(id, node);
        clients.put(id, client);
        return node;
    }

    public Client client() {
        return clients.get("1");
    }

    public Client client(String id) {
        return clients.get(id);
    }

    public void closeAllNodes() {
        for (Client client : clients.values()) {
            client.close();
        }
        clients.clear();
        for (Node node : nodes.values()) {
            if (node != null) {
                node.close();
            }
        }
        nodes.clear();
    }

}
