#!/bin/bash

set -e

echo "==== 检查并安装 gdb ===="
if ! command -v gdb &>/dev/null; then
  echo "未检测到 gdb，开始使用 brew 安装..."
  brew install gdb
else
  echo "已安装 gdb，跳过安装步骤。"
fi

echo "==== 创建 GDB 的代码签名证书 ===="

CERT_NAME="gdb-cert"

# 检查是否已有证书
if security find-certificate -c "$CERT_NAME" &>/dev/null; then
  echo "证书 '$CERT_NAME' 已存在。"
else
  echo "创建新的代码签名证书..."
  cat <<EOF | expect
spawn sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain /tmp/gdbc.cert
expect "Password:"
send -- "\r"
EOF

  sudo security create-keychain -p "" gdb.keychain
  security create-self-signed-cert -n "$CERT_NAME" -s "$CERT_NAME" -k ~/Library/Keychains/login.keychain-db
  security add-trusted-cert -d -r trustRoot -k ~/Library/Keychains/login.keychain-db ~/Library/Keychains/login.keychain-db

  echo "使用以下命令手动创建证书可能更稳定："
  echo "1. 打开钥匙串访问 -> 添加证书 -> 自签名 -> 名称为 'gdb-cert'，代码签名类型 -> 勾选 '始终信任代码签名'"
  echo "2. sudo codesign -fs gdb-cert \$(which gdb)"
  exit 1
fi

echo "==== 使用证书签名 GDB ===="
GDB_PATH=$(which gdb)
sudo codesign -fs "$CERT_NAME" "$GDB_PATH"

echo "==== 授权 GDB 调试权限 ===="
echo "需要打开“系统偏好设置” -> “安全性与隐私” -> “隐私” -> “开发者工具”，添加终端和 gdb。"

echo "==== 所有步骤已完成 ===="
echo "⚠️ 请重启你的电脑以使更改生效！"

