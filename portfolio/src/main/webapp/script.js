function formatComment(comment, nickname) {
    out = "<div class=\"comment card\"><h3>"
    out += nickname;
    out += "</h3><p>";
    out += comment;
    out += "</p></div>";
    return out;
}

function getComments() {
  let comments = document.getElementById('comments-container');
  comments.innerHTML = "";
  let num_comments = parseInt(document.getElementById('num-comments').value);
  fetch('/data?num-comments=' + num_comments).then(response => response.json())
  .then((json) => {
    json.forEach(comment => {
      comments.innerHTML += formatComment(comment.content, comment.nickname);
    })
  });
}

function deleteComments() {
  fetch('/delete-data', {method: 'POST'}).then(getComments);
}

function nicknamePrompt() {
    // {Popup prompt with form that posts to server}
    // Server should try to register nickname, and succeed iff uuid doesn't already have one
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
            create_comment.innerHTML = "<h3 style=\"margin: 0;\">Login to comment</h3>";
        }
        else if (json.nickname == "") {
            create_comment = document.getElementById("create-comment");
            create_comment.style.width = "400px";
            create_comment.innerHTML = "<h3 style=\"margin: 0;\">Set a nickname to comment</h3>";
        }
    });

    $('#submit-comment').prop('disabled',true);
    $('#new-comment').keyup(function() {
        $('#submit-comment').prop('disabled', (this.value == "")); 
    })
})