package com.fengyq.tool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.fengyq.function.io.FileBatchProcessor;
import com.fengyq.function.text.EncodingDetector;
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
    private String exts = "";
    private boolean testEncoding = false;
    private boolean autoAnalyseEncoding = false;
    
    private EncodingDetector detector = new EncodingDetector();
    
    private int total;
    private int processCount;
    private Map<String, Integer> encodingStat = new ConcurrentHashMap<>();
    
    static final List<String> GB_ENCODINGS = Arrays.asList(new String[] {"GB18030", "GBK", "GB2312", "BIG5", "EUC-TW"});
    
    @Override
    public void processFile(File f, int index) {
        total ++;
        try {
            if (testEncoding) {
                String encoding = detector.parseFileEncoding(f);
                encoding = encoding == null ? "unknown" : encoding;
                encodingStat.put(encoding,  encodingStat.computeIfAbsent(encoding, k->0) + 1);
                System.out.println(String.format("文件：%s，encoding=%s", f.getAbsolutePath(), encoding));
                return ;
            }
            String text; 
            if (autoAnalyseEncoding) {
                String encoding = detector.parseFileEncoding(f);
                if ("ASCII".equals(encoding) || inputEncoding.equals(encoding)) {
                    text = FileUtils.readFileToString(f, inputEncoding);
                } else if (GB_ENCODINGS.contains(inputEncoding) && GB_ENCODINGS.contains(encoding)) {
                    text = FileUtils.readFileToString(f, GB_ENCODINGS.get(0));
                } else {
                    System.out.println(String.format("跳过与预期编码不同的文件：%s，encoding=%s", f.getAbsolutePath(), encoding));
                    return ;
                }
            } else {
                text = FileUtils.readFileToString(f, inputEncoding);
            }
            FileUtils.writeStringToFile(f, text, outputEncoding);
            processCount ++;
            System.out.println(String.format("转化文件：%s", f.getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("usage: EncodingConverter filepath [-options]");
            System.out.println("  options:");
            System.out.println("    -exts 文件扩展名过滤，逗号分隔多个， 如: java,pas");
            System.out.println("    -i, --inputencoding  输入文件编码, 如: GBK");
            System.out.println("    -o, --outputencoding 输出文件编码，如：UTF-8 ");
            System.out.println("    -auto 自动分析编码，只对符合目标编码的文件进行转换 ");
            System.out.println("    -t    仅检查文件编码，不做转换 ");
            return ;
        }
        
        String path = "";
        EncodingConverter processor = new EncodingConverter();
        
        RunTimeParamParser paramParser = new RunTimeParamParser(args);
        processor.setInputEncoding(paramParser.getMultkKeyParamValue("inputencoding", "i"));
        processor.setOutputEncoding(paramParser.getMultkKeyParamValue("outputencoding", "o"));
        processor.setExts(paramParser.getParamValue("exts"));
        processor.setAutoAnalyseEncoding(paramParser.hasParam("auto"));
        processor.setTestEncoding(paramParser.hasParam("t"));

        path = paramParser.getDefaultParamValue();
        if (path != null && !"".equals(Strings.commonSuffix(path, File.separator))) {
            path = path.substring(0, path.length() - 1);
        }
        
        // check params
        File rootFile = new File(path);
        if (!rootFile.exists()) {
            System.out.println("传入的路径不存在！" + path);
            return ;
        }
        
        if (StringUtils.isBlank(processor.getInputEncoding())){
            System.out.println(String.format("请指定%s参数", "inputEncoding"));
        } else if (StringUtils.isBlank(processor.getOutputEncoding())){
            System.out.println(String.format("请指定%s参数", "outputEncoding"));
        } else {
            try {
                Charset.forName(processor.getInputEncoding());
            } catch (IllegalCharsetNameException e) {
                System.out.println(String.format("%s参数不合法", "inputEncoding"));
                return ;
            }
            try {
                Charset.forName(processor.getOutputEncoding());
            } catch (IllegalCharsetNameException e) {
                System.out.println(String.format("%s参数不合法", "outputEncoding"));
                return ;
            }
        }
        
        System.out.println(String.format("path=%s, params=%s", path, paramParser.getParams().toString()));

        String[] exts = processor.getExts().split("[,;]");
       
        if (rootFile.isDirectory()) {
            processor.inputRoot = path;
            processor.processDir(path, Arrays.asList(exts));
        } else {
            processor.processFile(rootFile, 0);
        }
        if (processor.testEncoding) {
            System.out.println(String.format("检查完毕!共检测到文件%d个，编码情况=%s", 
                processor.total, processor.encodingStat.toString()));
        } else {
            System.out.println(String.format("执行完毕!共检测到文件%d个，转化文件%d个", 
                processor.total, processor.processCount));
        }
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


    /**
     * @return the testEncoding
     */
    public boolean isTestEncoding() {
        return testEncoding;
    }


    /**
     * @param testEncoding the testEncoding to set
     */
    public void setTestEncoding(boolean testEncoding) {
        this.testEncoding = testEncoding;
    }


    /**
     * @return the autoAnalyseEncoding
     */
    public boolean isAutoAnalyseEncoding() {
        return autoAnalyseEncoding;
    }


    /**
     * @param autoAnalyseEncoding the autoAnalyseEncoding to set
     */
    public void setAutoAnalyseEncoding(boolean autoAnalyseEncoding) {
        this.autoAnalyseEncoding = autoAnalyseEncoding;
    }

    /**
     * @return the exts
     */
    public String getExts() {
        return exts;
    }

    /**
     * @param exts the exts to set
     */
    public void setExts(String exts) {
        this.exts = exts;
    }
    
    /**
     * RunTimeParamParser
     * 
     * @author fengyq
     * @version 1.0
     * @date 2020-04-13
     * 
     */
    static class RunTimeParamParser {
        /** * 参数pattern， --开头或-开头 */
        private static final Pattern KV_PARAM_PATTERN = Pattern.compile("--?([\\w_\\.]+)=(.*)"); 
        
        private Map<String, String> params = new HashMap<String, String>();
        private Map<String, String> fullKeyParams = new HashMap<String, String>();
        private String defaultParam;
        
        public RunTimeParamParser(String[] args) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("-")) {
                    Matcher m = KV_PARAM_PATTERN.matcher(arg);
                    if (m.find()) {
                        params.put(m.group(1).toLowerCase(), m.group(2));
                        if (arg.startsWith("--")) {
                            fullKeyParams.put(m.group(1).toLowerCase(), m.group(2));
                        }
                    } else {
                        params.put(arg.toLowerCase().substring(1), "true");
                    }
                } else {
                    // 无前缀key的参数
                    defaultParam = arg;
                }
            }
        }
        
        /**
         * 获取k-v参数的值
         * @param fullKey 长key，如inputencoding
         * @param shortKeys 短key，如i
         * @return
         */
        public String getMultkKeyParamValue(String fullKey, String shortKey) {
            String value = fullKeyParams.get(fullKey);
            if (value == null) {
                value = params.get(shortKey);
            }
            return value == null ? "" : value;
        }
        
        /**
         * 获取k-v参数的值
         * @param key
         * @return
         */
        public String getParamValue(String key) {
            String value = params.get(key);
            return value == null ? "" : value;
        }
        
        /**
         * 检查是否有参数key，用于boolean参数
         * @param key
         * @return
         */
        public boolean hasParam(String key) {
            return params.containsKey(key);
        }
        
        /**
         * 无key前缀的参数，只能有一个
         * @return
         */
        public String getDefaultParamValue() {
            return defaultParam;
        }

        /**
         * 所有命令行给出的参数
         * @return the params
         */
        public Map<String, String> getParams() {
            return params;
        }
        
    }
    
}
