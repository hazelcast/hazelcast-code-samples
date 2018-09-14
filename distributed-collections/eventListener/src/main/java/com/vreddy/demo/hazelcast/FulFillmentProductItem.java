package com.vreddy.demo.hazelcast;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@Generated("org.jsonschema2pojo")
/*
 *  This class represents the item feed from FP
 */
public class FulFillmentProductItem {
    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("dpci")
    private String dpci;
    

    @JsonProperty("ship_alone")
    private String shipAlone;

    @JsonProperty("item_status")
    private String itemStatus;

    @JsonProperty("max_zone")
    private String maxZone;

    @JsonProperty("node_priority")
    private List<String> nodePriority;

    public List<String> getNodePriority() {
        return nodePriority;
    }

    public void setNodePriority(List<String> nodePriority) {
        System.out.println("Here!!");
        this.nodePriority = formatNodePriorityListFromFPFeed(nodePriority);
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDpci() {
        return dpci;
    }

    public void setDpci(String dpci) {
        this.dpci = dpci;
    }

    public String getShipAlone() {
        return shipAlone;
    }

    public void setShipAlone(String shipAlone) {
        this.shipAlone = shipAlone;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getMaxZone() {
        return maxZone;
    }

    public void setMaxZone(String maxZone) {
        this.maxZone = maxZone;
    }

    public static List<String> formatNodePriorityListFromFPFeed(List<String> nodePriorityListFromFPFeed){
        if(nodePriorityListFromFPFeed == null)
            return null;

        List<String> formattedNodePriorityList = new ArrayList<String>();
        int index = 0;
        for(String node : nodePriorityListFromFPFeed){
            index = node.indexOf("-") + 1;
            if(node.contains("STORE")){
                formattedNodePriorityList.add("STR" + node.substring(index));
            } else {
                formattedNodePriorityList.add(node.substring(index));
            }
        }
        return formattedNodePriorityList;
    }
}