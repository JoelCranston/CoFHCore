package cofh.lib.util;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import static cofh.lib.util.constants.NBTTags.TAG_NAME;
import static cofh.lib.util.constants.NBTTags.TAG_UUID;

public class SocialUtils {

    private SocialUtils() {

    }

    private static final String TAG_FRIENDS = "cofh:friends";

    private static FriendData friends(ServerPlayer player) {

        return player.level().getDataStorage().computeIfAbsent(FriendData.TYPE);
    }

    // region FRIEND PASSTHROUGH
    public synchronized static boolean addFriend(ServerPlayer player, GameProfile friend) {

        return friends(player).addFriend(player, friend);
    }

    public synchronized static boolean removeFriend(ServerPlayer player, GameProfile friend) {

        return friends(player).removeFriend(player, friend);
    }

    public synchronized static boolean clearFriendList(ServerPlayer player) {

        return friends(player).clearFriendList(player);
    }

    public synchronized static boolean clearAllFriendLists(ServerPlayer player) {

        return friends(player).clearAllFriendLists(player);
    }

    public static boolean isFriendOrSelf(GameProfile owner, ServerPlayer player) {

        return friends(player).isFriendOrSelf(owner, player);
    }
    // endregion

    // region FRIEND DATA
    private static class FriendData extends SavedData {

        private final Map<String, Set<GameProfile>> friendLists = new TreeMap<>();

        public static final SavedDataType<FriendData> TYPE = new SavedDataType<>(Identifier.parse(TAG_FRIENDS), FriendData::new, CompoundTag.CODEC.xmap(FriendData::new, data -> data.save(new CompoundTag())));

        FriendData() {

        }

        FriendData(CompoundTag nbt) {

            for (String player : nbt.keySet()) {
                ListTag list = nbt.getListOrEmpty(player);
                Set<GameProfile> friendList = new ObjectOpenHashSet<>();
                for (int i = 0; i < list.size(); ++i) {
                    CompoundTag subTag = list.getCompoundOrEmpty(i);
                    friendList.add(new GameProfile(UUID.fromString(subTag.getStringOr(TAG_UUID, "")), subTag.getStringOr(TAG_NAME, "")));
                }
                friendLists.put(player, friendList);
            }
        }

        boolean addFriend(Player player, GameProfile friend) {

            if (player == null || friend == null) {
                return false;
            }
            String playerUUID = player.getGameProfile().id().toString();
            Set<GameProfile> set = friendLists.get(playerUUID);
            if (set == null) {
                set = new ObjectOpenHashSet<>();
            }
            set.add(friend);
            friendLists.put(playerUUID, set);
            this.setDirty();
            return true;
        }

        boolean removeFriend(Player player, GameProfile friend) {

            if (player == null || friend == null) {
                return false;
            }
            String playerUUID = player.getGameProfile().id().toString();
            Set<GameProfile> set = friendLists.get(playerUUID);
            this.setDirty();
            return set != null && set.remove(friend);
        }

        public boolean clearFriendList(Player player) {

            if (player == null) {
                return false;
            }
            friendLists.remove(player.getGameProfile().id().toString());
            this.setDirty();
            return true;
        }

        boolean clearAllFriendLists(Player player) {

            if (!player.permissions().hasPermission(Permissions.COMMANDS_OWNER)) {
                return false;
            }
            friendLists.clear();
            this.setDirty();
            return true;
        }

        boolean isFriendOrSelf(GameProfile owner, Player player) {

            if (owner == null || player == null) {
                return false;
            }
            String friendName = player.getGameProfile().name();
            if (friendName.equals(owner.name())) {
                return true;
            }
            String playerUUID = owner.id().toString();
            Set<GameProfile> set = friendLists.get(playerUUID);
            // Stored profiles carry no properties, and GameProfile equality compares them.
            return set != null && set.contains(new GameProfile(player.getGameProfile().id(), player.getGameProfile().name()));
        }

        public CompoundTag save(CompoundTag nbt) {

            for (Map.Entry<String, Set<GameProfile>> friendList : friendLists.entrySet()) {
                ListTag list = new ListTag();
                for (GameProfile friend : friendList.getValue()) {
                    CompoundTag subTag = new CompoundTag();
                    subTag.putString(TAG_UUID, friend.id().toString());
                    subTag.putString(TAG_NAME, friend.name());
                    list.add(subTag);
                }
                nbt.put(friendList.getKey(), list);
            }
            return nbt;
        }

    }
    // endregion
}
