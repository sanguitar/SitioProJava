package com.example.sitiopro.sync;

import com.fasterxml.jackson.databind.JsonNode;

record SyncSnapshot(long version, JsonNode payload) {
}
