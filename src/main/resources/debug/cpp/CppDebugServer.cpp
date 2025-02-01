//
// Created by 25080 on 2025/1/31.
//
#include <iostream>
#include <winsock2.h>

#pragma comment(lib, "ws2_32.lib")

std::string handle_client(SOCKET clientSocket) {
    char buffer[4096] = {0};
    int bytesReceived = recv(clientSocket, buffer, sizeof(buffer) - 1, 0);
    std::string body;
    if (bytesReceived > 0) {
        buffer[bytesReceived] = '\0'; // 确保字符串结束
        std::cout << "Received request:\n" << buffer << "\n" << std::endl;

        // 查找请求体开始位置（"\r\n\r\n"之后）
        const char* bodyStart = strstr(buffer, "\r\n\r\n");
        if (bodyStart != nullptr) {
            bodyStart += 4; // 跳过"\r\n\r\n"
            body = bodyStart;
            std::cout << "Request body: " << bodyStart << "\n" << std::endl;
        } else {
            std::cerr << "Could not find the start of the request body.\n" << std::endl;
        }
    } else if (bytesReceived == 0) {
        std::cout << "Connection closed by peer.\n" << std::endl;
    } else {
        std::cerr << "recv failed with error: " << WSAGetLastError() << "\n" << std::endl;
    }

    return body;
}

int main() {
    WSADATA wsaData;
    SOCKET serverSocket, clientSocket;
    sockaddr_in serverAddr, clientAddr{};
    int clientAddrLen = sizeof(clientAddr);

    // 初始化Winsock
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        std::cerr << "WSAStartup failed.\n";
        return 1;
    }

    // 创建套接字
    serverSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (serverSocket == INVALID_SOCKET) {
        std::cerr << "Socket creation failed.\n";
        WSACleanup();
        return 1;
    }

    // 绑定地址和端口
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(8080);
    if (bind(serverSocket, (sockaddr*)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
        std::cerr << "Bind failed.\n";
        closesocket(serverSocket);
        WSACleanup();
        return 1;
    }

    // 监听连接
    if (listen(serverSocket, SOMAXCONN) == SOCKET_ERROR) {
        std::cerr << "Listen failed.\n";
        closesocket(serverSocket);
        WSACleanup();
        return 1;
    }

    std::cout << "Server is running on port 8080...\n" << std::endl;

    // 主循环：持续接受客户端连接
    while (true) {
        // 接受客户端连接
        clientSocket = accept(serverSocket, (sockaddr*)&clientAddr, &clientAddrLen);
        if (clientSocket == INVALID_SOCKET) {
            std::cerr << "Accept failed.\n";
            continue; // 继续等待下一个连接
        }

        auto body = handle_client(clientSocket);
        if (body == "q") {
            break;
        }

        const char* response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nConnection: close\r\n\r\nHello world";
        send(clientSocket, response, strlen(response), 0);
        std::cout << "Response sent.\n" << std::endl;
        // 关闭客户端套接字
        closesocket(clientSocket);
    }

    // 实际上不会到达这里
    closesocket(serverSocket);
    WSACleanup();

    return 0;
}