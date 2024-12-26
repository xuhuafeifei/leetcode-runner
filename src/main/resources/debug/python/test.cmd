# test.cmd
# 为了便于调试, 可以直接在resource/debug/python目录下启动Main.py, 测试时直接修改Main.py即可
# 而沟通Main.py创建的服务的cmd指令基本格式如下
# 如果需要修改, 可参照Operation.java提供的指令修改请求体
curl -v POST http://localhost:5005/process -d "{\"operation\": \"W\", \"param\": \"\"}" -H "Content-Type: application/json"