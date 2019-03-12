EncodingConverter
====================
编码转换，如UTF-8转GBK、GBK转UTF-8等

## 参数介绍
```bat
usage: EncodingConverter -ext= [-inputencoding=] [-outputencoding=]  
    -ext use , or ; separate multi exts, eg: java,pas  
    -inputencoding encoding of input file, eg: GBK  
    -outputencoding encoding of output file   
```


## how to use
**打包**
```bat
  mvn clean package
```

**命令**

```bat

java -jar EncodingConverter.jar  "E:\source"  -ext=java  -inputencoding=GBK -outputencoding=UTF-8
```

