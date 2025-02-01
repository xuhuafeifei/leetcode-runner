# test.cmd
# 为了便于调试, 可以直接在resource/debug/cpp目录下启动Main.cpp, 测试时直接修改Main.cpp即可
# 而沟通Main.cpp创建的服务的cmd指令基本格式如下
# 如果需要修改, 可参照Operation.java提供的指令修改请求体
curl -v POST http://localhost:8080 process -d "{\"operation\": \"W\", \"gdbCommand\": \"\"}" -H "Content-Type: application/json"