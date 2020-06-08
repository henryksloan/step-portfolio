function escape(string) {
    var char_map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;'
    };

    return string.replace(/[&<>]/g, function(ch) {
        return char_map[ch] || ch;
    });
}

function formatComment(comment, nickname, timestamp) {
    out = "<div class=\"comment card\"><h3>"
    out += escape(nickname);
    out += "</h3><p class=\"time-passed\">";
    out += timePassed(timestamp);
    out += "</p><p>"
    out += escape(comment);
    out += "</p></div>";
    return out;
}

function getComments() {
  let comments = document.getElementById('comments-container');
  comments.innerHTML = "";
  let num_comments = parseInt(document.getElementById('num-comments').value);
  let language = document.getElementById('language').value;
  fetch('/data?num-comments=' + num_comments + "&language=" + language).then(response => response.json())
  .then((json) => {
    json.forEach(comment => {
      comments.innerHTML += formatComment(comment.content, comment.nickname, comment.timestamp);
    })
  });
}

function timePassed(date) {
    let seconds = Math.floor((new Date() - date) / 1000);
    let conversions = {
        "year": 60*60*24*30*12,
        "month": 60*60*24*30,
        "day": 60*60*24,
        "hour": 60*60,
        "minute": 60,
        "second": 1
    }

    for (let conversion in conversions) {
        let converted = Math.floor(seconds/conversions[conversion])
        if (converted >= 1 || conversion == "second") {
            return converted + " " + conversion + ((converted === 1) ? "" : "s") + " ago";
        }
    }
}

function deleteComments() {
  fetch('/delete-data', {method: 'POST'}).then(getComments);
}

function nicknamePrompt() {
    let popup_container = document.getElementById("popup-container");
    popup_container.style.display = "block";
    let body = document.getElementsByTagName("body")[0];
    body.classList.add("body-no-scroll");
}

function closePopup() {
    let popup_container = document.getElementById("popup-container");
    popup_container.style.display = "none";
    let body = document.getElementsByTagName("body")[0];
    body.classList.remove("body-no-scroll");
    localStorage.setItem("popup_dismissed", true);
}

$(document).ready(function() {
    fetch('/user').then(response => response.json())
    .then((json) => {
        let login_button = document.getElementById("login-button");
        let nickname = document.getElementById("nickname");

        login_button.href = json.url;
        login_button.innerText = (json.logged_in)
            ? "Logout"
            : "Login";
        nickname.innerHTML = (json.logged_in && json.nickname == "")
            ? "<a class=\"user-button\" onclick=\"nicknamePrompt()\">Set a nickname</a>"
            : json.nickname;

        if (!json.logged_in) {
            create_comment = document.getElementById("create-comment");
            create_comment.style.width = "400px";
            create_comment.innerHTML = "<h3 style=\"margin: 0; cursor: pointer;\" onclick=\"location.href = '" + json.url + "';\">Login to comment</h3>";
        }
        else if (json.nickname == "") {
            create_comment = document.getElementById("create-comment");
            create_comment.style.width = "400px";
            create_comment.innerHTML = "<h3 style=\"margin: 0; cursor: pointer;\" onclick=\"nicknamePrompt()\">Set a nickname to comment</h3>";
            if (!localStorage.getItem("popup_dismissed")) nicknamePrompt();
        }
    });

    $('#submit-comment').prop('disabled',true);
    $('#new-comment').keyup(function() {
        $('#submit-comment').prop('disabled', (this.value == "")); 
    })
})