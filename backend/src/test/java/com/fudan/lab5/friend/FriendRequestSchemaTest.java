package com.fudan.lab5.friend;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FriendRequestSchemaTest {
    @Test
    void friendRequestSchemaAllowsMultipleAcceptedHistoryRowsForSamePair() throws IOException {
        String schema = readSchema();

        assertThat(schema).doesNotContain("uk_friend_requests_pair_status UNIQUE");
    }

    @Test
    void friendRequestSchemaCreatesRequesterReceiverStatusIndex() throws IOException {
        String schema = readSchema();

        assertThat(schema).contains(
            "CREATE INDEX idx_friend_requests_requester_receiver_status ON friend_requests(requester_id, receiver_id, status)"
        );
    }

    private static String readSchema() throws IOException {
        Path schemaFromBackend = Path.of("..", "sql", "schema.sql");
        if (Files.exists(schemaFromBackend)) {
            return Files.readString(schemaFromBackend);
        }
        return Files.readString(Path.of("sql", "schema.sql"));
    }
}
