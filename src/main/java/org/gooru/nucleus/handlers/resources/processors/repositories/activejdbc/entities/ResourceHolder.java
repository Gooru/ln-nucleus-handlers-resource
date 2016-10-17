package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities;

import java.util.Objects;

/**
 * @author ashish on 17/10/16.
 */
public final class ResourceHolder {
    final AJEntityResource resource;
    final AJEntityOriginalResource originalResource;
    final RESOURCE_CATEGORY category;

    public enum RESOURCE_CATEGORY {
        RESOURCE_ORIGINAL,
        RESOURCE_REFERENCE
    }

    public ResourceHolder(AJEntityResource resource) {
        Objects.requireNonNull(resource);
        this.resource = resource;
        this.originalResource = null;
        this.category = RESOURCE_CATEGORY.RESOURCE_REFERENCE;
    }

    public ResourceHolder(AJEntityOriginalResource resource) {
        Objects.requireNonNull(resource);
        this.originalResource = resource;
        this.category = RESOURCE_CATEGORY.RESOURCE_ORIGINAL;
        this.resource = null;
    }

    public AJEntityOriginalResource getOriginalResource() {
        if (category != RESOURCE_CATEGORY.RESOURCE_ORIGINAL) {
            throw new IllegalStateException("Requesting resource reference instead of original resource");
        }
        return this.originalResource;
    }

    public AJEntityResource getResource() {
        if (category != RESOURCE_CATEGORY.RESOURCE_REFERENCE) {
            throw new IllegalStateException("Requesting original resource instead of resource reference");
        }
        return this.resource;
    }

    public RESOURCE_CATEGORY getCategory() {
        return this.category;
    }
}
