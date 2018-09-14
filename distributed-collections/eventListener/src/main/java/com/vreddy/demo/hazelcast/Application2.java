package com.vreddy.demo.hazelcast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Application2 {

    public static void main(String args[]) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        FulFillmentProductItem fp = mapper.readValue("{\"product_id\":\"51470525\",\"dpci\":\"060-26-1240\",\"ship_method_consolidation\":true,\"ship_from_store\":false,\"backorder_limit_qty\":300,\"backorder_type\":\"Dropship (standard)\",\"recalled_flag\":false,\"item_status\":\"Active\",\"item_code\":0,\"item_kind_code\":1,\"item_kind_status\":\"Sellable\",\"is_allow_pre_back_order\":false,\"is_published\":true,\"is_valid\":true,\"vendors\":[{\"location_identifier\":\"TDK0\",\"is_primary_vendor\":true},{\"location_identifier\":\"79730\",\"is_primary_vendor\":true}],\"dimensions\":{\"weight\":8.81,\"weight_unit_of_measure\":\"POUND\",\"width\":20.87,\"height\":9.45,\"depth\":22.83,\"dimension_unit_of_measure\":\"INCH\"},\"ship_alone\":false,\"max_order_quantity\":99,\"is_food\":false,\"is_hazmat\":false,\"advanced_orders_events\":[],\"is_marketplace\":false,\"shipping_carrier_code\":\"UPS\",\"node_priority\":[\"STORE-1234\",\"STORE-2345\"],\"restrictions\":[{\"service_level\":\"LTL_INSIDE_THE_DOOR\"},{\"service_level\":\"LTL_TO_THE_DOOR\"},{\"service_level\":\"WHENEVER\"},{\"address_type\":\"STATE\",\"address_value\":\"AP\",\"service_level\":\"ANY\"},{\"address_type\":\"STATE\",\"address_value\":\"AA\",\"service_level\":\"ANY\"},{\"service_level\":\"LTL_ROOM_OF_CHOICE\"},{\"address_type\":\"STATE\",\"address_value\":\"AE\",\"service_level\":\"ANY\"},{\"service_level\":\"LTL_WHITE_GLOVE_ASSEMBLY\"},{\"service_level\":\"LTL_WHITE_GLOVE\"},{\"address_type\":\"POBOX\",\"service_level\":\"ANY\"}],\"event_timestamp\":\"2018-04-17T08:51:37.893Z\"}", FulFillmentProductItem.class);
        System.out.println(fp.getNodePriority());
    }
}
