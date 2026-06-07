package com.fudan.lab5.friend;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FriendRequestSchemaTest {
    @Test
    void friendRequestSchemaAllowsMultipleAcceptedHistoryRowsForSamePair() throws IOException {
        String schema = Files.readString(Path.of("..", "sql", "schema.sql"));
        String upgrade = Files.readString(Path.of("..", "sql", "upgrade_friend_requests.sql"));

        assertThat(schema).doesNotContain("uk_friend_requests_pair_status UNIQUE");
        assertThat(upgrade).doesNotContain("uk_friend_requests_pair_status UNIQUE");
    }

    @Test
    void upgradeScriptCreatesReplacementRequesterIndexBeforeDroppingOldUniqueIndex() throws IOException {
        String upgrade = Files.readString(Path.of("..", "sql", "upgrade_friend_requests.sql"));

        assertThat(upgrade.indexOf("CREATE INDEX idx_friend_requests_requester_receiver_status"))
            .isLessThan(upgrade.indexOf("ALTER TABLE friend_requests DROP INDEX uk_friend_requests_pair_status"));
    }
}
