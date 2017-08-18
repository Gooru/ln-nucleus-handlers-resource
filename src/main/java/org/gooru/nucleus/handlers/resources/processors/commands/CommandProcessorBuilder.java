package org.gooru.nucleus.handlers.resources.processors.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.resources.constants.MessageConstants;
import org.gooru.nucleus.handlers.resources.processors.Processor;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 2/1/17.
 */
public enum CommandProcessorBuilder {

    DEFAULT("default") {
        private final Logger LOGGER = LoggerFactory.getLogger(CommandProcessorBuilder.class);
        private final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

        @Override
        public Processor build(ProcessorContext context) {
            return () -> {
                LOGGER.error("Invalid operation type passed in, not able to handle");
                return MessageResponseFactory
                    .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.operation"));
            };
        }
    },
    RESOURCE_DELETE(MessageConstants.MSG_OP_RES_DELETE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new ResourceDeleteProcessor(context);
        }
    },
    RESOURCE_UPDATE(MessageConstants.MSG_OP_RES_UPDATE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new ResourceUpdateProcessor(context);
        }
    },
    RESOURCE_CREATE(MessageConstants.MSG_OP_RES_CREATE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new ResourceCreateProcessor(context);
        }
    },
    RESOURCE_GET(MessageConstants.MSG_OP_RES_GET) {
        @Override
        public Processor build(ProcessorContext context) {
            return new ResourceGetProcessor(context);
        }
    };

    private String name;

    CommandProcessorBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    private static final Map<String, CommandProcessorBuilder> LOOKUP = new HashMap<>();

    static {
        for (CommandProcessorBuilder builder : values()) {
            LOOKUP.put(builder.getName(), builder);
        }
    }

    public static CommandProcessorBuilder lookupBuilder(String name) {
        CommandProcessorBuilder builder = LOOKUP.get(name);
        if (builder == null) {
            return DEFAULT;
        }
        return builder;
    }

    public abstract Processor build(ProcessorContext context);
}
