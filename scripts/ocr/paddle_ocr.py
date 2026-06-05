#!/usr/bin/env python3
"""
PaddleOCR 智能错题本 - 题目图片识别脚本
用法: python paddle_ocr.py <image_path>

安装依赖: pip install paddlepaddle paddleocr
首次运行会自动下载模型文件。
"""
import sys
import json
import os

# 设置 PaddleOCR 模型目录为用户目录下的 .paddleocr
os.environ.setdefault("PADDLEOCR_HOME", os.path.expanduser("~/.paddleocr"))


def recognize(image_path):
    """对图片进行 OCR 识别，返回文本内容"""
    try:
        from paddleocr import PaddleOCR
    except ImportError:
        print("请先安装 PaddleOCR: pip install paddlepaddle paddleocr", file=sys.stderr)
        return ""

    ocr = PaddleOCR(lang='ch', use_angle_cls=True, show_log=False)
    results = ocr.ocr(image_path)

    if not results or not results[0]:
        return ""

    lines = []
    for line in results[0]:
        text = line[1][0]
        confidence = line[1][1]
        if confidence > 0.6:  # 过滤低置信度结果
            lines.append(text)

    return "\n".join(lines)


def main():
    if len(sys.argv) < 2:
        print("用法: python paddle_ocr.py <image_path>", file=sys.stderr)
        sys.exit(1)

    image_path = sys.argv[1]
    if not os.path.exists(image_path):
        print(f"文件不存在: {image_path}", file=sys.stderr)
        sys.exit(1)

    text = recognize(image_path)
    if text:
        print(text)
    else:
        print("")


if __name__ == "__main__":
    main()
