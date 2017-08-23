package com.fengyq.function.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.io.Files;

/**
 * FileBatchProcessor
 * 批量处理文件抽象类
 * @author fengyq
 * @version 1.0
 *
 */
public abstract class FileBatchProcessor {
    
    protected String inputRoot;
    protected String outputRoot;
    protected boolean update = false;
    
    /**
     * 获得输出文件名
     * @param f
     * @return
     * @throws IOException
     */
    protected File getOutputFile(File f) {
        String fileName;
        try {
            fileName = f.getCanonicalPath();
            String outPath = f.getParent() + File.separator;
            if (!Strings.isNullOrEmpty(outputRoot)) {
                outPath = outPath.replace(inputRoot, outputRoot);
            }
            String outfileName = String.format("%s%s.txt", outPath, Files.getNameWithoutExtension(fileName));
            return new File(outfileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 处理文件
     * @param f
     * @param index
     */
    public abstract void processFile(File f, int index);
    
    /**
     * 跳过文件时，可对文件做其他处理或阻止跳过
     * @param f
     * @param index
     * @return 返回true允许跳过，否则不跳过
     */
    protected boolean onUpdate(File f, int index) {
        return true;
    }
    
    /**
     * @param path 文件夹路径
     * @param exts 扫描的文件扩展名，都用小写
     */
    public void processDir(String path, final List<String> exts) {
        File dir = new File(path);
        // create dest dirs
        if (!Strings.isNullOrEmpty(outputRoot)) {
            String outPath = dir.getAbsolutePath().replace(inputRoot, outputRoot);
            File outDir = new File(outPath);
            if (!outDir.exists())
                outDir.mkdirs();
        }
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile()) {
                    String name = pathname.getName();
                    String ext = Files.getFileExtension(name).toLowerCase();
                    return exts.contains(ext);
                }
                return false;
            }
        });
        
        if (files.length > 0) {
            // process files
            int index = 0;
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f.isHidden())
                    continue;
                index++;
                File outFile = getOutputFile(f);
                // update模式如果输出的txt文件时间比源文件晚，说明不用更新
                if (update) {
                    if (outFile.exists()) {
                        if (outFile.lastModified() > f.lastModified()) {
                            if (onUpdate(f, index))
                                continue;
                        }
                    }
                }
                processFile(f, index);
            }
        }
        
        // 子目录
        File[] dirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && !pathname.isHidden();
            }
        });
        for (int i = 0; i < dirs.length; i++) {
            File f = dirs[i];
            try {
                processDir(f.getAbsolutePath(), exts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return the inputRoot
     */
    public String getInputRoot() {
        return inputRoot;
    }

    /**
     * @param inputRoot the inputRoot to set
     */
    public void setInputRoot(String inputRoot) {
        this.inputRoot = inputRoot;
    }

    /**
     * @return the outputRoot
     */
    public String getOutputRoot() {
        return outputRoot;
    }

    /**
     * @param outputRoot the outputRoot to set
     */
    public void setOutputRoot(String outputRoot) {
        this.outputRoot = outputRoot;
    }

    /**
     * @return the update
     */
    public boolean isUpdate() {
        return update;
    }

    /**
     * @param update the update to set
     */
    public void setUpdate(boolean update) {
        this.update = update;
    }

}
