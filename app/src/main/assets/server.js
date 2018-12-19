'use strict';

const WebSocketServer = require('ws');
const uid = require("uid");
// const express = require("express");
const PORT = 3000;

const clients = {}; // Подключенные клиенты
let counter = 0;

/*

    Будем работать по приведенной ниже  схеме
    1. Необходимо для начала сделать подключение к данному серверу из андроида
    2. Отладить работу сервера
    3. Переписать приложение для андроид под Socket Server

 */


/*
// Создали канал
var a, b;
let struct = new Map(["second channel", new Map([id, a])]);
// Если хотим отправить сообщение по каналу
let array_socket = struct.get("first channel");
array_socket.forEach((val, key, map) => {
    val.send("message");
});

// Добавляем сокет в канал
struct.get("second channel").set(id, b);

// Добавляем канал
struct.set("three channel", new Map([id, a]));

// Удаляем канал
struct.delete("first channel");

// Удаляем сокет из канала
struct.get("second channel").delete(id);
*/

let registry = new Map();
const type = {
    SUB: "sub",
    UNSUB: "unsub",
    MES: "mes"
};

// WebSocket-сервер на порту PORT
const webSocketServer = new WebSocketServer.Server({port: PORT});
webSocketServer.on('connection', function connection(ws) {

    const id = uid(10);
    clients[id] = ws;
    console.log("SocketServer: новое соединение. ID: " + id);
    counter++;
    //for(var key in clients) {
    //    clients[key].send('responseCountConnected: ' + counter.toString());
    //}

    ws.on('message', function incoming(message) {
        console.log('ID: ' + id + ' SocketServer: получено сообщение: ' + message);

        const obj = JSON.parse(message);
        let subscribers = registry.get(obj.channel);
        if (obj.type === type.SUB) {
            if (subscribers !== undefined) { // Просто подписываемся
                subscribers.set(id, clients[id]);
            } else {
                subscribers = new Map(); // Создаем канал и подписываемся
                subscribers.set(id, clients[id]);
                registry.set(obj.channel, subscribers);
            }
            ws.send(JSON.stringify({
                type: type.SUB,
                channel: obj.channel,
                message: "subscribed"
            }));
            return;
        }
        if (obj.type === type.UNSUB) {
            if (subscribers !== undefined) { // Отписываемся
                subscribers.delete(id);
            }
            ws.send(JSON.stringify({
                type: type.UNSUB,
                channel: obj.channel,
                message: "unsubscribed"
            }));
            return;
        }
        if (obj.type === type.MES) {
            if (obj.message === "getCountConnected") {
                ws.send(JSON.stringify({
                    type: type.MES,
                    channel: obj.channel,
                    message: counter.toString()
                }));
                return;
            }
            // Рассылка сообщения подписчикам канала
            if (subscribers !== undefined) {
                subscribers.forEach(((value, key) => {
                    if (value.readyState === 3) {
                        value.close();
                        subscribers.delete(key);
                        delete clients[key];
                    } else {
                        value.send(JSON.stringify({
                            type: type.MES,
                            channel: obj.channel,
                            message: obj.message
                        }));
                    }
                }));
            }
        }
    });

    ws.on('close', function () {
        console.log('SocketServer: соединение закрыто. ID: ' + id + '\n');
        counter--;
        delete clients[id];
    });
});

console.log("\nSocketServer запущен на порту " + PORT + "\n");