package com.aelux.susarchy.attachment;

import com.aelux.susarchy.SusArchaeology;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class SusAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SusArchaeology.MOD_ID);

    private static final StreamCodec<RegistryFriendlyByteBuf, Boolean> BOOLEAN_STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeBoolean(value),
            buffer -> buffer.readBoolean()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> SUSPICIOUS =
            ATTACHMENTS.register("suspicious", () -> AttachmentType.builder(() -> Boolean.TRUE)
                    .serialize(Codec.BOOL)
                    .sync(BOOLEAN_STREAM_CODEC)
                    .build());

    private SusAttachments() {
    }
}
