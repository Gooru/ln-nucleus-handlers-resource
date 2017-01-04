package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("archieved_original_resource")
public class AJEntityArchievedOriginalResource extends Model {

    public static final String INSERT_FROM_ORIGINAL_RESOURCE =
        "INSERT INTO archieved_original_resource(id, title, url, is_remote, http_protocol, http_host, http_port, http_domain, http_path, http_query,"
        + " is_broken, is_iframe_breaker, iframe_breaker_reason, created_at, updated_at, creator_id, modifier_id, publish_date, publish_status,"
        + " subject, language, narration, description, content_subformat, audience, educational_use, metadata, taxonomy, gut_codes, thumbnail,"
        + " is_copyright_owner, copyright_owner, info, visible_on_profile, display_guide, accessibility, is_deleted, editorial_tags, license,"
        + " creator_system) SELECT id, title, url, is_remote, http_protocol, http_host, http_port, http_domain, http_path, http_query, is_broken,"
        + " is_iframe_breaker, iframe_breaker_reason, created_at, updated_at, creator_id, modifier_id, publish_date, publish_status, subject,"
        + " language, narration, description, content_subformat, audience, educational_use, metadata, taxonomy, gut_codes, thumbnail,"
        + " is_copyright_owner, copyright_owner, info, visible_on_profile, display_guide, accessibility, is_deleted, editorial_tags, license,"
        + " creator_system FROM original_resource WHERE id = ?::uuid";
}
