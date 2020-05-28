const http = require('http');
const os = requre('os');

console.log("Kubia server starting");

var handler = function(request, response) {
    console.log("received request from .. " + request.connection.remoteAddress);
    response.writeHead(200);
    response.end("You've hit " + os.hostname() + "\n");
};

var www = http.createServer(handler);
www.listen(8080);
