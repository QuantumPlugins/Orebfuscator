/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.orebfuscator.cache;

import net.minecraft.server.RegionFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;

public class ObfuscatedDataCache
{
    private static final HashMap<File, Reference<RegionFile>> cachedRegionFiles = new HashMap<File, Reference<RegionFile>>();
    
    public static synchronized RegionFile getRegionFile(File folder, int x, int z)
    {
        File path = new File(folder, "region");
        File file = new File(path, "r." + (x >> 5) + "." + (z >> 5) + ".mcr");
        try
        {
            Reference<RegionFile> reference = cachedRegionFiles.get(file);
            
            if (reference != null)
            {
                RegionFile regionFile = (RegionFile) reference.get();
                if (regionFile != null)
                {
                    return regionFile;
                }
            }
            
            if (!path.exists())
            {
                path.mkdirs();
            }
            
            if (cachedRegionFiles.size() >= OrebfuscatorConfig.getMaxLoadedCacheFiles())
            {
                clearCache();
            }
            
            RegionFile regionFile = new RegionFile(file);
            cachedRegionFiles.put(file, new SoftReference<RegionFile>(regionFile));
            return regionFile;
        }
        catch (Exception e)
        {
            try
            {
                file.delete();
            }
            catch (Exception e2)
            {
                Orebfuscator.log(e);
            }
        }
        return null;
    }
    
    public static synchronized void clearCache()
    {
        for (Reference<RegionFile> reference : cachedRegionFiles.values())
        {
            try
            {
                RegionFile regionFile = (RegionFile) reference.get();
                if (regionFile != null)
                    regionFile.c();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        cachedRegionFiles.clear();
    }
    
    public static DataInputStream getInputStream(File folder, int x, int z)
    {
        RegionFile regionFile = getRegionFile(folder, x, z);
        return regionFile.a(x & 0x1F, z & 0x1F);
    }
    
    public static DataOutputStream getOutputStream(File folder, int x, int z)
    {
        RegionFile regionFile = getRegionFile(folder, x, z);
        return regionFile.b(x & 0x1F, z & 0x1F);
    }
    
    public static void ClearCache()
    {
        ObfuscatedDataCache.clearCache();
        try
        {
            DeleteDir(OrebfuscatorConfig.getCacheFolder());
        }
        catch (Exception e)
        {
            Orebfuscator.log(e);
        }
    }
    
    private static void DeleteDir(File dir)
    {
        try
        {
            if (!dir.exists())
                return;
            
            if (dir.isDirectory())
                for (File f : dir.listFiles())
                    DeleteDir(f);
            
            dir.delete();
        }
        catch (Exception e)
        {
            Orebfuscator.log(e);
        }
    }
}