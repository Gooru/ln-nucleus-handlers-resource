package org.gooru.nucleus.handlers.resources.bootstrap.utilities;

import java.util.UUID;

public final class TestConstants {
    public static final int TIMEOUT = 30;
    public static final String ID_RESOURCE = "resourceId";
    public static final String HEADER_TOKEN =
        "Token YW5vbnltb3VzOlRodSBGZWIgMTEgMDc6Mzc6MzIgVVRDIDIwMTY6MTQ1NTE3NjI1MjY0Nw==";

    public static final String EMAIL = "email";
    public static final String EMAIL_DEFAULT_VALUE = "user@example.org";
    public static final String USER_ID_DEFAULT_VALUE = UUID.randomUUID().toString();
    public static final String ANONYMOUS = "anonymous";

    private TestConstants() {
        throw new AssertionError();
    }
}