//Establish the WebSocket connection and set up event handlers
var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chat/");
webSocket.onmessage = function (msg) { updateChat(msg); };
webSocket.onclose = function () { alert("WebSocket connection closed") };
webSocket.onopen = setUsername();

//Send message if "Send" is clicked
id("send").addEventListener("click", function () {
    sendMessage(id("message").value);
});

//Send message if enter is pressed in the input field
id("message").addEventListener("keypress", function (e) {
    if (e.keyCode === 13) { sendMessage(e.target.value); }
});

id("addchannel").addEventListener("click", function () {
    newChannel();
});

id("exitchannel").addEventListener("click", function () {
    webSocket.send("ext");
});

function newChannel(){
    var channelName=prompt("Wpisz nazwe kanalu lub q aby wyjsc");
    if(channelName.length>12)
        channelName=channelName.substring(0,11);
    if(channelName=="q")
        return;
    if (channelName != "" && channelName != null) {
        webSocket.send("add"+channelName);
    }
    else{
        newChannel();
    }
}

function channelEnter(channel){
    webSocket.send("ent" + channel);
}

function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+ d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}

function setUsername(){

    var username = getCookie("username");
    if (username != "") {
        alert("Welcome again " + username);
    } else {
        username = prompt("Please enter your name:", "");
        if (username != "" && username != null) {
            setCookie("username", username, 365);
        }
        else{
            setUsername();
        }
    }



    webSocket.send("usr" + username);
}

function changeUsername() {
    var username = prompt("Please enter your name:", "");
    if (username != "" && username != null) {
        setCookie("username", username, 365);
    }
    webSocket.send("usr" + username);
}

//Send a message if it's not empty, then clear the input field
function sendMessage(message) {
    if (message !== "") {
        webSocket.send("userMessage:" + message);
        id("message").value = "";
    }
}

//Send a message if it's not empty, then clear the input field
function sendMessage(message) {
    if (message !== "") {
        webSocket.send("msg" + message);
        id("message").value = "";
    }
}

//Update the chat-panel
function updateChat(msg) {

    var data = JSON.parse(msg.data);
    if(data.userMessage == "TAKEN_USERNAME"){
        alert("nazwa juz zajeta");
        changeUsername();
        return;
    }

    if(data.userMessage != "")
        insert("chat", data.userMessage);

    id("channellist").innerHTML = "";

    data.channellist.forEach(function (channel) {

        var znacznik = document.createElement('button');
        znacznik.onclick = function () {channelEnter(channel);}
        znacznik.style.width="150px";
        var t = document.createTextNode(channel);
        znacznik.appendChild(t);

        var kontener = id("channellist");
        kontener.appendChild(znacznik);
    });



}

//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", message);
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}