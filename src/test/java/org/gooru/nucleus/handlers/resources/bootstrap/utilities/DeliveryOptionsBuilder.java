package org.gooru.nucleus.handlers.resources.bootstrap.utilities;

import java.util.UUID;

import org.gooru.nucleus.handlers.resources.constants.MessageConstants;

import io.vertx.core.eventbus.DeliveryOptions;

public final class DeliveryOptionsBuilder {

    private DeliveryOptionsBuilder() {
        throw new AssertionError();
    }

    public static DeliveryOptions buildDeliveryOptionsForDeleteResource() {
        String resourceId = UUID.randomUUID().toString();
        return buildDeliveryOptionsForDeleteResource(resourceId);
    }

    public static DeliveryOptions buildDeliveryOptionsForDeleteResource(String resourceId) {
        return new DeliveryOptions().setSendTimeout(TestConstants.TIMEOUT * 1000)
            .addHeader(MessageConstants.MSG_HEADER_OP, MessageConstants.MSG_OP_RES_DELETE)
            .addHeader(TestConstants.ID_RESOURCE, resourceId);
    }

    public static DeliveryOptions buildDeliveryOptionsForCreateResource() {
        return new DeliveryOptions().setSendTimeout(TestConstants.TIMEOUT * 1000)
            .addHeader(MessageConstants.MSG_HEADER_OP, MessageConstants.MSG_OP_RES_CREATE);
    }

    public static DeliveryOptions buildDeliveryOptionsForFetchResource(String resourceId) {
        return new DeliveryOptions().setSendTimeout(TestConstants.TIMEOUT * 1000)
            .addHeader(MessageConstants.MSG_HEADER_OP, MessageConstants.MSG_OP_RES_GET)
            .addHeader(TestConstants.ID_RESOURCE, resourceId);
    }

    public static DeliveryOptions buildDeliveryOptionsForUpdateResource(String resourceId) {
        return new DeliveryOptions().setSendTimeout(TestConstants.TIMEOUT * 1000)
            .addHeader(MessageConstants.MSG_HEADER_OP, MessageConstants.MSG_OP_RES_UPDATE)
            .addHeader(TestConstants.ID_RESOURCE, resourceId);
    }
}