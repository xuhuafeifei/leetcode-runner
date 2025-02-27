import os
import codecs

def convert_encoding(file_path, from_encoding, to_encoding):
    with codecs.open(file_path, 'r', encoding=from_encoding) as f:
        content = f.read()
    with codecs.open(file_path, 'w', encoding=to_encoding) as f:
        f.write(content)

# 遍历目录下的所有.properties文件
convert_encoding(r"E:\java_code\leetcode-runner\src\main\resources\messages\for_converted.txt", 'utf-8', 'latin-1')
