package com.fengyq.tool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.fengyq.function.io.FileBatchProcessor;
import com.google.common.base.Strings;

/**
 * WordProcess
 * @author fengyq
 * @version 1.0
 *
 */
public class EncodingConverter extends FileBatchProcessor {
    
    private String inputEncoding = "";
    private String outputEncoding = "";
    private String ext = "";

    @Override
    public void processFile(File f, int index) {
        try {
            String text = FileUtils.readFileToString(f, inputEncoding);
            FileUtils.writeStringToFile(f, text, outputEncoding);
            System.out.println(String.format("转化文件：%s", f.getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    static final String PARAM_INPUT_ENCODING = "-inputencoding";
    static final String PARAM_OUTPUT_ENCODING = "-outputencoding";
    static final String PARAM_EXT = "-ext";
    
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("usage: EncodingConverter -ext= [-inputencoding=] [-outputencoding=]");
            System.out.println("    -ext use , or ; separate multi exts, eg: java,pas");
            System.out.println("    -inputencoding encoding of input file, eg: GBK");
            System.out.println("    -outputencoding encoding of output file ");
            return ;
        }
        
        String path = "";
        EncodingConverter processor = new EncodingConverter();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                if (arg.toLowerCase().startsWith(PARAM_OUTPUT_ENCODING + "=")) {
                    processor.setOutputEncoding(arg.substring((PARAM_OUTPUT_ENCODING + "=").length()).trim());
                } else if (arg.toLowerCase().startsWith(PARAM_INPUT_ENCODING + "=")) {
                    processor.setInputEncoding(arg.substring((PARAM_INPUT_ENCODING + "=").length()).trim());
                } else if (arg.toLowerCase().startsWith(PARAM_EXT + "=")) {
                    processor.ext = arg.substring((PARAM_EXT + "=").length()).trim();
                }
            } else {
                path = arg;
                if (!"".equals(Strings.commonSuffix(path, File.separator)))
                    path = path.substring(0, path.length() - 1);
            }
        }
        System.out.println(String.format("path=%s, ext=%s, inputEncoding=%s, outputEncoding=%s",
            path, processor.ext, processor.getInputEncoding(), processor.getOutputEncoding()));
        
        File dir = new File(path);
        if (!dir.exists()) {
            System.out.println("传入的路径不存在！" + path);
            return ;
        } else if (!dir.isDirectory()) {
            System.out.println("传入的路径不是目录！" + path);
            return ;
        }
        if (StringUtils.isBlank(processor.getInputEncoding())){
            System.out.println(String.format("请指定%s参数", PARAM_INPUT_ENCODING));
        } else if (StringUtils.isBlank(processor.getOutputEncoding())){
            System.out.println(String.format("请指定%s参数", PARAM_OUTPUT_ENCODING));
        } else {
            try {
                Charset.forName(processor.getInputEncoding());
            } catch (IllegalCharsetNameException e) {
                System.out.println(String.format("%s参数不合法", PARAM_INPUT_ENCODING));
                return ;
            }
            try {
                Charset.forName(processor.getOutputEncoding());
            } catch (IllegalCharsetNameException e) {
                System.out.println(String.format("%s参数不合法", PARAM_OUTPUT_ENCODING));
                return ;
            }
        }

        String[] exts = processor.ext.split("[,;]");
       
        processor.inputRoot = path;
        processor.processDir(path, Arrays.asList(exts));
        
        System.out.println("执行完毕!");
    }


    /**
     * @return the inputEncoding
     */
    public String getInputEncoding() {
        return inputEncoding;
    }


    /**
     * @param inputEncoding the inputEncoding to set
     */
    public void setInputEncoding(String inputEncoding) {
        this.inputEncoding = inputEncoding;
    }


    /**
     * @return the outputEncoding
     */
    public String getOutputEncoding() {
        return outputEncoding;
    }


    /**
     * @param outputEncoding the outputEncoding to set
     */
    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }
    
}
