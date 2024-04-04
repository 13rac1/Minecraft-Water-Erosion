package com._13rac1.erosion.common;

import java.util.Date;

import javax.annotation.Nonnull;

import net.minecraft.WorldVersion;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.DataVersion;

public class FakeWorldVersion implements WorldVersion {
    static private boolean once = true;

    // init() starts up the Minecraft system, once
    static void init() {
        if (!once) {
            return;
        }
        once = false;

        WorldVersion version = new FakeWorldVersion();
        net.minecraft.SharedConstants.setVersion(version);
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Override
    public DataVersion getDataVersion() {
        return new DataVersion(13);
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getId'");
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public int getProtocolVersion() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProtocolVersion'");
    }

    @Override
    public int getPackVersion(@Nonnull PackType p_265245_) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPackVersion'");
    }

    @Override
    public Date getBuildTime() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBuildTime'");
    }

    @Override
    public boolean isStable() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isStable'");
    }

}
