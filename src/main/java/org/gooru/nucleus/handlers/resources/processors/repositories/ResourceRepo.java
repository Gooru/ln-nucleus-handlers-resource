package org.gooru.nucleus.handlers.resources.processors.repositories;

import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;


public interface ResourceRepo {

  MessageResponse createResource();

  MessageResponse updateResource();

  MessageResponse fetchResource();

}
