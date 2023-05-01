package me.kicksquare.mcmbungee.types.bans;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.kicksquare.mcmbungee.MCMBungee;

public class GlobalBansResponseEntry {
    private static final MCMBungee plugin = MCMBungee.getPlugin();

    @JsonProperty("player_uuid")
    public String player_uuid;
    @JsonProperty("ban_reason")
    public BanReason ban_reason;
    @JsonProperty("id")
    public String id;


    public GlobalBansResponseEntry(@JsonProperty("player_uuid") String player_uuid, @JsonProperty("ban_reason") BanReason ban_reason, @JsonProperty("id") String id) {
        this.player_uuid = player_uuid;
        this.ban_reason = ban_reason;
        this.id = id;
    }
}