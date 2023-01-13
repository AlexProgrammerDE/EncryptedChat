package net.pistonmaster.encryptedchat.server;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.encryptedchat.crypto.CryptoStorage;
import net.pistonmaster.encryptedchat.data.StorageGroup;
import net.pistonmaster.encryptedchat.data.StorageMember;
import net.pistonmaster.encryptedchat.data.StorageMessage;
import net.pistonmaster.encryptedchat.data.StorageUser;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;

import static net.pistonmaster.encryptedchat.jooq.Tables.*;

@RequiredArgsConstructor
public class ServerStorage {
    private final ServerMain main;
    private final String url = "jdbc:sqlite:server.sqlite";

    public StorageUser generateUser(String username, PublicKey key) {
        try (Connection conn = DriverManager.getConnection(url)) {
            DSLContext ctx = DSL.using(conn, SQLDialect.SQLITE);

            UUID uuid = UUID.randomUUID();

            ctx.insertInto(USERS)
                    .columns(USERS.NAME, USERS.ID, USERS.PUBLIC_KEY)
                    .values(username, uuid.toString(), CryptoStorage.saveKeyToString(key))
                    .execute();

            return new StorageUser(username, uuid, key);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public StorageUser getUser(String username) {
        try (Connection conn = DriverManager.getConnection(url)) {
            DSLContext ctx = DSL.using(conn, SQLDialect.SQLITE);

            Record record = ctx.selectFrom(USERS)
                    .where(USERS.NAME.eq(username))
                    .fetchOne();

            return fillUser(record);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public StorageUser getUser(UUID uuid) {
        try (Connection conn = DriverManager.getConnection(url)) {
            DSLContext ctx = DSL.using(conn, SQLDialect.SQLITE);

            Record record = ctx.selectFrom(USERS)
                    .where(USERS.ID.eq(uuid.toString()))
                    .fetchOne();

            return fillUser(record);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private StorageUser fillUser(Record record) {
        if (record == null) {
            return null;
        }

        return new StorageUser(record.get(USERS.NAME),
                UUID.fromString(record.get(USERS.ID)),
                CryptoStorage.loadPublicKey(record.get(USERS.PUBLIC_KEY)));
    }

    public StorageGroup generateGroup(String groupName, UUID owner, UUID groupKeyId) {
        try (Connection conn = DriverManager.getConnection(url)) {
            DSLContext ctx = DSL.using(conn, SQLDialect.SQLITE);

            UUID uuid = UUID.randomUUID();

            ctx.insertInto(GROUPS)
                    .columns(GROUPS.ID, GROUPS.NAME, GROUPS.KEY_ID)
                    .values(uuid.toString(), groupName, groupKeyId.toString())
                    .execute();

            ctx.insertInto(GROUP_MEMBERS)
                    .columns(GROUP_MEMBERS.GROUP_ID, GROUP_MEMBERS.USER_ID, GROUP_MEMBERS.ADMIN)
                    .values(uuid.toString(), owner.toString(), 1)
                    .execute();

            return new StorageGroup(groupName, uuid, groupKeyId, List.of(new StorageMember(owner, true)), List.of());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public StorageGroup getGroup(String groupName) {
        try (Connection conn = DriverManager.getConnection(url)) {
            DSLContext ctx = DSL.using(conn, SQLDialect.SQLITE);

            Record record = ctx.selectFrom(GROUPS)
                    .where(GROUPS.NAME.equalIgnoreCase(groupName))
                    .fetchOne();

            return fillGroupData(record, ctx);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public StorageGroup getGroup(UUID groupId) {
        try (Connection conn = DriverManager.getConnection(url)) {
            DSLContext ctx = DSL.using(conn, SQLDialect.SQLITE);

            Record record = ctx.selectFrom(GROUPS)
                    .where(GROUPS.ID.eq(groupId.toString()))
                    .fetchOne();

            return fillGroupData(record, ctx);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private StorageGroup fillGroupData(Record record, DSLContext ctx) {
        if (record == null) {
            return null;
        }

        List<StorageMember> members = ctx.selectFrom(GROUP_MEMBERS)
                .where(GROUP_MEMBERS.GROUP_ID.eq(record.get(GROUPS.ID)))
                .fetch()
                .map(r -> new StorageMember(UUID.fromString(r.get(GROUP_MEMBERS.USER_ID)), r.get(GROUP_MEMBERS.ADMIN) == 1));

        List<StorageMessage> messages = ctx.selectFrom(GROUP_MESSAGES)
                .where(GROUP_MESSAGES.GROUP_ID.eq(record.get(GROUPS.ID)))
                .fetch()
                .map(r -> new StorageMessage(UUID.fromString(r.get(GROUP_MESSAGES.USER_ID)), r.get(GROUP_MESSAGES.MESSAGE_ENCRYPTED), r.get(GROUP_MESSAGES.MESSAGE_SIGNATURE)));

        return new StorageGroup(record.get(GROUPS.NAME),
                UUID.fromString(record.get(GROUPS.ID)),
                UUID.fromString(record.get(GROUPS.KEY_ID)),
                members, messages);
    }

    public void addMemberToGroup(StorageGroup group, UUID targetUser) {
        try (Connection conn = DriverManager.getConnection(url)) {
            DSLContext ctx = DSL.using(conn, SQLDialect.SQLITE);

            ctx.insertInto(GROUP_MEMBERS)
                    .columns(GROUP_MEMBERS.GROUP_ID, GROUP_MEMBERS.USER_ID, GROUP_MEMBERS.ADMIN)
                    .values(group.groupId().toString(), targetUser.toString(), 0)
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void storeMessage(StorageGroup group, UUID sender, String messageEncrypted, String messageSignature) {
        try (Connection conn = DriverManager.getConnection(url)) {
            DSLContext ctx = DSL.using(conn, SQLDialect.SQLITE);

            ctx.insertInto(GROUP_MESSAGES)
                    .columns(GROUP_MESSAGES.GROUP_ID, GROUP_MESSAGES.USER_ID, GROUP_MESSAGES.MESSAGE_ENCRYPTED, GROUP_MESSAGES.MESSAGE_SIGNATURE)
                    .values(group.groupId().toString(), sender.toString(), messageEncrypted, messageSignature)
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
