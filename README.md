EncodingConverter
====================
编码转换工具，如UTF-8转GBK、GBK转UTF-8等

## 参数介绍
```bat
usage: EncodingConverter filepath [-options]
  options:
    -exts 文件扩展名过滤，逗号分隔多个， 如: java,pas
    -i, --inputencoding  输入文件编码, 如: GBK
    -o, --outputencoding 输出文件编码，如：UTF-8 
    -auto 自动分析编码，只对符合目标编码的文件进行转换 
    -t    仅检查文件编码，不做转换 
```


## how to use
### 打包

```bat
  mvn clean package
```

### 使用

转化文件
```bat
java -jar EncodingConverter.jar  "E:\source"  -exts=java  -i=GBK -o=UTF-8
```
转化文件前自动检测编码，不符合预期编码的文件将跳过
```bat
java -jar EncodingConverter.jar  "E:\source"  -exts=java  -i=GBK -o=UTF-8 -auto
```
检测编码
```bat
java -jar EncodingConverter.jar  "E:\source"  -exts=java  -t
```

